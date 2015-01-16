package eu.sqooss.plugins.git.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.plugins.updater.git.GitUpdater;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Developer;
import eu.sqooss.service.db.DeveloperAlias;
import eu.sqooss.service.db.ProjectFile;
import eu.sqooss.service.db.ProjectFileState;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.tds.AccessorException;
import eu.sqooss.service.tds.Revision;
import org.slf4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;

public class TestGitUpdater extends TestGitSetup {

    static DBService db;
    static Logger l;
    static GitUpdater updater;
    static StoredProject sp ;
    
    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initTestRepo();

        ConfigurableApplicationContext context = SpringTestApplication.initialiseSpringTestContext();
        db = context.getBean(DBService.class);
        db.startDBSession();
        sp = new StoredProject();
        sp.setName(projectName);
        db.addRecord(sp);
        db.commitDBSession();
    }
    
    @Before
    public void setUp() throws AccessorException, URISyntaxException {
        getGitRepo();
        assertNotNull(git);
        updater = new GitUpdater(db, git, l, sp);
    }

    @Test
    public void testGetAuthor() {
        db.startDBSession();

        //Test a properly formatted name
        Developer d = updater.getAuthor(sp, "Papa Smurf <pm@smurfvillage.com>");
        assertNotNull(d);
        assertEquals("Papa Smurf", d.getName());
        assertNull(d.getUsername());
        assertEquals(1, d.getAliases().size());
        assertTrue(d.getAliases().contains(new DeveloperAlias("pm@smurfvillage.com", d)));

        //A bit of Developer DAO testing
        assertNotNull(Developer.getDeveloperByEmail("pm@smurfvillage.com", sp));
        d.addAlias("pm@smurfvillage.com");
        assertEquals(1, d.getAliases().size());
        
        //Test a non properly formated name
        d = updater.getAuthor(sp, "Gargamel <gar@smurfvillage.(name)>");
        assertNotNull(d);
        assertEquals("Gargamel", d.getUsername());
        assertNull(d.getName());
        assertEquals(1, d.getAliases().size());
        assertTrue(d.getAliases().contains(new DeveloperAlias("gar@smurfvillage.(name)", d)));
        
        //Test a user name only name
        d = updater.getAuthor(sp, "Smurfette");
        assertNotNull(d);
        assertEquals("Smurfette", d.getUsername());
        assertNull(d.getName());
        assertEquals(0, d.getAliases().size());
        
        //Test a non properly formated email
        d = updater.getAuthor(sp, "Clumsy Smurf <smurfvillage.com>");
        assertNotNull(d);
        assertNull(d.getUsername());
        assertEquals("Clumsy Smurf <smurfvillage.com>", d.getName());
        assertEquals(0, d.getAliases().size());
        
        //Test with name being just an email
        d = updater.getAuthor(sp, "chef@smurfvillage.com");
        assertNotNull(d);
        assertNull(d.getUsername());
        assertNull(d.getName());
        assertEquals(1, d.getAliases().size());
       
        db.rollbackDBSession();
    }
   
    @Test
    public void testUpdate() throws Exception {
        File repo = new File(localrepo, Constants.DOT_GIT);
        FileRepository local =  new FileRepository(repo);
        Revision from = git.getFirstRevision();
        Revision to = git.getNextRevision(from);
        Revision upTo = git.newRevision("2ade9340262cb87163b5c5c270268175ff3b3c15");

        while (to.compareTo(upTo) < 0) {
            ArrayList<ProjectFile> foundFiles = new ArrayList<ProjectFile>();
          
            System.err.println("Revision: " + from.getUniqueId());
            updater.updateFromTo(from, to);

            RevWalk rw = new RevWalk(local);
            ObjectId obj = local.resolve(from.getUniqueId());
            RevCommit commit = rw.parseCommit(obj);

            TreeWalk tw = new TreeWalk(local);
            tw.addTree(commit.getTree());
            tw.setRecursive(true);

            db.startDBSession();
            sp = db.attachObjectToDBSession(sp);
            ProjectVersion pv = ProjectVersion.getVersionByRevision(sp, from.getUniqueId());
            assertNotNull(pv);

            //Compare repository files against database files
            while (tw.next()) {
                String path = "/" + tw.getPathString();
                //System.err.println("Tree entry: " + path);
                String basename = eu.sqooss.service.util.FileUtils.basename(path);
                String dirname = eu.sqooss.service.util.FileUtils.dirname(path);
                ProjectFile pf = ProjectFile.findFile(sp.getId(), basename, dirname, pv.getRevisionId());
                testVersionedProjectFile(pf);
                if (!pf.getIsDirectory())
                	foundFiles.add(pf);
            }

            List<ProjectFile> allfiles = pv.allFiles();
            for (ProjectFile pf : allfiles) {
            	if (!foundFiles.contains(pf)) {
            		System.err.println("File " + pf + " not in repository");
            		assertTrue(false);
            	}
            }

            for (ProjectFile pf : foundFiles) {
            	if (!allfiles.contains(pf)) {
            		System.err.println("File " + pf + " not found in allFiles() result");
            		assertTrue(false);
            	}
            }

            db.commitDBSession();
            tw.release();
            rw.release();
            from = to;
            foundFiles.clear();
            to = git.getNextRevision(to);
        }
    }

    //From this point forward, all methods assume an open db session
    public void testVersionedProjectFile(ProjectFile pf) {
    	assertNotNull(pf);
    	//System.err.println("Testing file: " + pf);
    	
    	//Check that each file entry is accompanied with an enclosing directory
    	//entry with an added or modified state
    	ProjectFile dir = pf.getEnclosingDirectory();
    	assertNotNull(dir);
    	assertEquals(pf.getProjectVersion().getRevisionId(), pf.getProjectVersion().getRevisionId());
    	assertFalse(dir.getState().getStatus() == ProjectFileState.STATE_DELETED);
    	
    	if (pf.isAdded()) {
    		//Not much to test...
    		return;
    	}
    	
    	//Check that old and new versions of a file point to the same path
		ProjectFile old = pf.getPreviousFileVersion();
		assertNotNull(old);
		assertEquals(old.getFileName(), pf.getFileName());
		if (old.getIsDirectory() != pf.getIsDirectory()) {
			assertEquals(false, true);
		}
    }
}

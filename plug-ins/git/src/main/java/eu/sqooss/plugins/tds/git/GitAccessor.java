/*
 * Copyright 2010 - Organization for Free and Open Source Software,  
 *                Athens, Greece.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package eu.sqooss.plugins.tds.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.lib.Commit;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.tds.AccessorException;
import eu.sqooss.service.tds.AnnotatedLine;
import eu.sqooss.service.tds.CommitEntry;
import eu.sqooss.service.tds.CommitLog;
import eu.sqooss.service.tds.Diff;
import eu.sqooss.service.tds.InvalidProjectRevisionException;
import eu.sqooss.service.tds.InvalidRepositoryException;
import eu.sqooss.service.tds.PathChangeType;
import eu.sqooss.service.tds.Revision;
import eu.sqooss.service.tds.Revision.Kind;
import eu.sqooss.service.tds.Revision.Status;
import eu.sqooss.service.tds.SCMAccessor;
import eu.sqooss.service.tds.SCMNode;
import eu.sqooss.service.tds.SCMNodeType;

/**
 * An accessor for Git repositories. Encapsulates the functionality provided
 * by the JGit library.
 * 
 * @author Georgios Gousios - <gousiosg@gmail.com>
 */
public class GitAccessor implements SCMAccessor {
    public static String ACCESSOR_NAME = "GitAccessor";
    private static List<URI> supportedSchemes;
    
    private URI uri;
    private String projectname;
    private Repository git = null;
    private Logger logger = null;
    
    static {
        supportedSchemes = new ArrayList<URI>();
        supportedSchemes.add(URI.create("git-file://www.sqo-oss.org"));
    }
    
	@Override
	public String getName() {
		return ACCESSOR_NAME;
	}

	@Override
	public List<URI> getSupportedURLSchemes() {
		return supportedSchemes;
	}

	@Override
	public void init(URI dataURL, String projectName) 
	throws AccessorException {

        doInit(dataURL, projectName);
	    this.logger = AlitheiaCore.getInstance().getLogManager().createLogger(Logger.NAME_SQOOSS_TDS);
        if (logger != null) {
            logger.info("Created SCMAccessor for " + uri.toASCIIString());
        } else 
            throw new AccessorException(this.getClass(), "Could not instantiate a logger");
	}
	
	public void testInit(URI dataURL, String projectName) 
    throws AccessorException {
	    doInit(dataURL, projectName);
	}
	
	private void doInit(URI dataURL, String projectName) 
    throws AccessorException {
	    this.uri = dataURL;
	    this.projectname = projectName;
	    try {
            git = new Repository(toGitRepo(uri));
        } catch (IOException e) {
            throw new AccessorException(this.getClass(), 
                    "Cannot initialise accessor for URL " + uri.toASCIIString());
        }
	}
	
	private File toGitRepo(URI url) {
	    File f = new File(url.getPath(), Constants.DOT_GIT);
	    return f;
	}
	
    public Revision newRevision(Date d) {return null;}
    
    public Revision newRevision(String uniqueId) {
        
        if (uniqueId == null || uniqueId.equals("")) {
            if (logger != null)
                logger.error("Cannot create new revision with null or empty" +
                    " revisionid");
            return null;
        }
        
        try {
            Commit obj = git.mapCommit(uniqueId);
            Revision r = new GitRevision(uniqueId, obj.getAuthor().getWhen(), 
                    Status.RESOLVED, Kind.FROM_REVISION);
            return r;
        } catch (IOException e) {
            if (logger != null)
                logger.error("Cannot resove revision " + uniqueId + ":" + 
                        e.getMessage());
            return null;
        }
    }

    public Revision getHeadRevision()
        throws InvalidRepositoryException {return null;}
    
    public Revision getFirstRevision() 
        throws InvalidRepositoryException {return null;}
    
    public Revision getNextRevision(Revision r)
        throws InvalidProjectRevisionException {return null;}
    
    public Revision getPreviousRevision(Revision r)
        throws InvalidProjectRevisionException {return null;}
    
    public boolean isValidRevision(Revision r) {return false;}
    
    public void getCheckout(String repoPath, Revision revision, File localPath)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException,
               FileNotFoundException {return;}

    public void updateCheckout(String repoPath, Revision src,
        Revision dst, File localPath)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException,
               FileNotFoundException {return;}

    public void getFile(String repoPath, Revision revision, File localPath)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException,
               FileNotFoundException {return;}

    public void getFile(String repoPath, Revision revision, OutputStream stream)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException,
               FileNotFoundException {return;}

    public CommitLog getCommitLog(Revision r)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException {return null;}
    
    public CommitLog getCommitLog(Revision r1, Revision r2)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException {return null;}

    public CommitLog getCommitLog(String repoPath, Revision r1, Revision r2)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException {return null;}

    public CommitEntry getCommitLog(String repoPath, Revision r)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException {return null;}

    public Diff getDiff(String repoPath, Revision r1, Revision r2)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException,
               FileNotFoundException {return null;}

    public Diff getChange(String repoPath, Revision r)
        throws InvalidProjectRevisionException,
               InvalidRepositoryException,
               FileNotFoundException {return null;}

    public SCMNodeType getNodeType(String repoPath, Revision r)
        throws InvalidRepositoryException {return null;}

    public String getSubProjectPath() throws InvalidRepositoryException 
        {return null;}
    
    public List<SCMNode> listDirectory(SCMNode dir)
        throws InvalidRepositoryException,
        InvalidProjectRevisionException  {return null;}
    
    public SCMNode getNode(String path, Revision r) 
        throws  InvalidRepositoryException,
                InvalidProjectRevisionException {return null;}
    
    public PathChangeType getNodeChangeType(SCMNode s) 
        throws InvalidRepositoryException, 
               InvalidProjectRevisionException {return null;}
    
    public List<AnnotatedLine> getNodeAnnotations(SCMNode s) {return null;}
}

// vi: ai nosi sw=4 ts=4 expandtab

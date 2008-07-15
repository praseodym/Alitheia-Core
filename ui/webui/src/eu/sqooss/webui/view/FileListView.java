/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008-2008 by Sebastian Kuegler <sebas@kde.org>
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

package eu.sqooss.webui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import eu.sqooss.webui.ListView;
import eu.sqooss.webui.datatype.File;

public class FileListView extends ListView {
    public static int FILES     = 2;
    public static int FOLDERS   = 4;

    // Contains the list of files that can be presented by this view
    private List<File> files = new ArrayList<File>();

    // Contains the Id of the selected project (if any)
    private Long projectId;

    // Contains the Id of the selected project's version (if any)
    private Long versionId;

    private int type = 0;

    private String status = "";

    /**
     * Instantiates a new <code>FileListView</code> object and initializes it
     * with the given list of project files.
     */
    public FileListView (List<File> filesList, int type) {
        this.type = type;
        for (File nextFile : filesList)
            addFile(nextFile);
    }

    /**
     * Instantiates a new <code>FileListView</code> object and initializes it
     * with the given list of project files.
     */
    public FileListView (SortedMap<Long, File> filesList, int type) {
        this.type = type;
        for (File nextFile : filesList.values())
            addFile(nextFile);
    }

    /**
     * Return the number of files that are stored in this object.
     *
     * @return The number of files.
     */
    public Integer size() {
        return files.size();
    }

    /**
     * Adds a single file to the stored files list. If the file object is
     * <code>null</code> or doesn't match the selected type, then it won't be
     * added.
     *
     * @param file the file object
     */
    public void addFile(File file) {
        if (file != null) {
            if (file.getIsDirectory()) {
                if (type == FOLDERS)
                    files.add(file);
            }
            else {
                if (type == FILES)
                    files.add(file);
            }
        }
    }

    /**
     * Initializes this object with a new list of files. If the list object
     * is <code>null</code>, then it will be skipped.
     *
     * @param files the new files list
     */
    public void setFiles(List<File> filesList) {
        if (filesList != null)
            for (File nextFile : filesList)
                addFile(nextFile);
    }

    /**
     * Gets the Id of the project that is associated with this view.
     *
     * @return The project Id, or <code>null</code> when none is associated.
     */
    public Long getProjectId() {
        return projectId;
    }

    /**
     * Sets the Id of the project that is associated with this view.
     *
     * @param projectId the project Id
     */
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * Gets the Id of the project's version that is associated with this view.
     *
     * @return The version Id, or <code>null</code> when none is associated.
     */
    public Long getVersionId() {
        return versionId;
    }

    /**
     * Sets the Id of the project's version that is associated with this view.
     *
     * @param versionId the version Id
     */
    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // TODO: This method can filter out some of the files from the given list,
    // by using the specified set of filters.
    public List<File> filterFiles (List<File> filesList) {
        List<File> result = new ArrayList<File>();
        if (filesList != null)
            result = filesList;
        return result;
    }

    /* (non-Javadoc)
     * @see eu.sqooss.webui.ListView#getHtml(long)
     */
    public String getHtml(long in) {
        StringBuffer html = new StringBuffer();
        int numFiles = 0;
        int numFolders = 0;
        if (size() > 0) {
            html.append(sp(in++) + "<ul>\n");
            // Display all folders
            if (type == FOLDERS)
                for (File nextFile : this.files) {
                    if (nextFile.getIsDirectory()) {
                        numFolders++;
                        nextFile.setSettings(settings);
                        html.append((nextFile != null)
                                ? sp(in) + "<li>" 
                                        + nextFile.getHtml(this.versionId)
                                        + sp(in) + "</li>\n"
                                : "");
                    }
                }
            // Display all files
            if (type == FILES)
                for (File nextFile : this.files) {
                    if (nextFile.getIsDirectory() == false) {
                        numFiles++;
                        nextFile.setSettings(settings);
                        html.append((nextFile != null)
                                ? sp(in) + "<li>" 
                                        + nextFile.getHtml(this.versionId)
                                        + sp(in) + "</li>\n"
                                : "");
                    }
                }
            html.append(sp(--in) + "</ul>\n");
        }
        else {
            if (type == (FOLDERS | FILES))
                html.append("<i>This folder contains no sub-folders nor files.</i>");
            else if (type == FOLDERS)
                html.append("<i>This folder contains no sub-folder.</i>");
            else if (type == FILES)
                html.append("<i>This folder contains no files.</i>");
        }
        // Construct the status line's messages
        if (type == (FOLDERS | FILES))
            setStatus(sp(in) + "Total: "
                    + ((numFolders > 0)
                            ? ((numFolders > 1)
                                    ? numFolders + " folders"
                                    : "one folder")
                            : "")
                    + (((numFolders > 0) && (numFiles > 0)) ? " and " : "")
                    + ((numFiles > 0)
                            ? ((numFiles > 1)
                                    ? numFiles + " files"
                                    : "one file")
                            : "")
                    + " found");
        else if (type == FOLDERS)
            setStatus(sp(in) + "Total: "
                    + ((numFolders > 0)
                            ? ((numFolders > 1)
                                    ? numFolders + " folders"
                                    : "one folder")
                            : "zero folders")
                    + " found");
        else if (type == FILES)
            setStatus(sp(in) + "Total: "
                    + ((numFiles > 0)
                            ? ((numFiles > 1)
                                    ? numFiles + " files"
                                    : "one file")
                            : "zero files")
                    + " found");
        return html.toString();
    }

}
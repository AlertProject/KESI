/*
 * Copyright (C) 2011-2013 GSyC/LibreSoft, Universidad Rey Juan Carlos
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Authors: Santiago Dueñas <sduenas@libresoft.es>
 *          Luis Cañas Díaz <lcanas@bitergia.com>
 *
 */

package eu.alertproject.kesi.sources;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.PreferencesError;
import eu.alertproject.kesi.PreferencesManager;
import eu.alertproject.kesi.events.EventGenerator;
import eu.alertproject.kesi.extractors.ExtractionManager;
import eu.alertproject.kesi.jobs.CommandRunner;
import eu.alertproject.kesi.jobs.Job;
import eu.alertproject.kesi.model.IssueTracker;
import eu.alertproject.kesi.model.Repository;
import eu.alertproject.kesi.model.StructuredKnowledgeSource;

/**
 * Singleton implementation based on enum types. See Joshua Bloch's
 * conference in the Google I/O 2008 <a href=
 * "http://sites.google.com/site/io/effective-java-reloaded/effective_java_reloaded.pdf"
 * ></a>
 * 
 * TODO: addNodeChangeListener for changes in the repositories
 */
public enum SourcesManager {
    INSTANCE;

    public static final String GIT = "git";
    public static final String SVN = "svn";
    public static final String BUGZILLA = "bg";
    public static final String JIRA = "jira";

    private static Logger logger = Logger.getLogger(SourcesManager.class);

    private final HashMap<String, StructuredKnowledgeSource> sources = new HashMap<String, StructuredKnowledgeSource>();

    public void setUp() throws SourcesManagerError {
        boolean success;
        String srcPath = PreferencesManager.INSTANCE.getSourcesDownloadPath();
        File dir = new File(srcPath);

        if (!dir.exists()) {
            success = dir.mkdirs();

            if (!success) {
                String msg = String
                        .format("Error creating sources path %s. Export to files disabled.",
                                srcPath);
                logger.error(msg);
                throw new SourcesManagerError(msg);
            }
        }

        importSources();
        scheduleSources();
    }

    public void importSources() throws SourcesManagerError {
        ArrayList<StructuredKnowledgeSource> srcs;

        logger.debug("Importing sources from preferences");

        try {
            srcs = PreferencesManager.INSTANCE.getSources();
        } catch (PreferencesError e) {
            String msg = String.format(
                    "Importing sources from preferences. Error: %s",
                    e.getMessage());
            throw new SourcesManagerError(msg);
        }

        for (StructuredKnowledgeSource source : srcs) {
            String uri = source.getURI().toASCIIString();

            if (sources.containsKey(uri)) {
                String msg = String.format(
                        "Duplicated source URI %s in preferences file", uri);
                throw new SourcesManagerError(msg);
            }

            sources.put(uri, source);
            logger.debug(String.format("Source %s of type %s imported", uri,
                    source.getClass().toString()));
        }

        logger.debug("Preferences sources imported");
    }

    public void scheduleSources() throws SourcesManagerError {
        for (StructuredKnowledgeSource source : sources.values()) {
            if (source.getSetup() == Job.PUBLISH) {
                EventGenerator.INSTANCE.generate(source, new Timestamp(0));
            } else {
                ExtractionManager.INSTANCE.extract(source);
            }
        }
    }

    public ArrayList<StructuredKnowledgeSource> getSourcesSCM() {
        ArrayList<StructuredKnowledgeSource> repositories = new ArrayList<StructuredKnowledgeSource>();

        for (StructuredKnowledgeSource source : sources.values()) {
            if (source instanceof Repository) {
                repositories.add(source);
            }
        }

        return repositories;
    }

    public ArrayList<StructuredKnowledgeSource> getSourcesITS() {
        ArrayList<StructuredKnowledgeSource> trackers = new ArrayList<StructuredKnowledgeSource>();

        for (StructuredKnowledgeSource source : sources.values()) {
            if (source instanceof IssueTracker) {
                trackers.add(source);
            }
        }

        return trackers;
    }

    public StructuredKnowledgeSource getSource(String uri)
            throws SourcesManagerError {
        if (sources.containsKey(uri)) {
            return sources.get(uri);
        } else {
            String msg = String.format("Source with URI %s not found", uri);
            throw new SourcesManagerError(msg);
        }
    }

    public void downloadSource(String uri) throws SourcesManagerError {
        StructuredKnowledgeSource source = getSource(uri);
        String dir = getDownloadSourcePath(source);
        String type = source.getType();

        if (type.equals(GIT)) {
            File file = new File(dir + "/" + ".git");

            if (file.isDirectory()) {
                updateSource(uri, type, dir);
            } else {
                cloneSource(uri, type, dir);
            }
        }
    }

    public String getDownloadSourcePath(String uri) throws SourcesManagerError {
        StructuredKnowledgeSource source = getSource(uri);
        return getDownloadSourcePath(source);
    }

    public String getDownloadSourcePath(StructuredKnowledgeSource source) {
        return PreferencesManager.INSTANCE.getSourcesDownloadPath()
                + source.getId();
    }

    private void cloneSource(String uri, String type, String dir)
            throws SourcesManagerError {
        if (type.equals(GIT)) {
            int result;

            String[] cmd = { GIT, "clone", uri, dir };
            CommandRunner tr = new CommandRunner();

            result = tr.run(GIT, cmd, null);

            if (result != 0) {
                String msg = String.format("Error clonning git repository %s",
                        uri);
                throw new SourcesManagerError(msg);
            } else {
                logger.debug(String.format("Source %s cloned", uri));
            }
        }
    }

    private void updateSource(String uri, String type, String dir)
            throws SourcesManagerError {
        if (type.equals(GIT)) {
            int result;

            String[] cmd = { GIT, "pull" };
            CommandRunner tr = new CommandRunner();

            result = tr.run(GIT, cmd, dir);

            if (result != 0) {
                String msg = String.format("Error pulling git repository %s",
                        uri);
                throw new SourcesManagerError(msg);
            } else {
                logger.debug(String.format("Source %s updated", uri));
            }
        }
    }

}

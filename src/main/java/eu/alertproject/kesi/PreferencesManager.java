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
 *
 */

package eu.alertproject.kesi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import eu.alertproject.kesi.database.Database;
import eu.alertproject.kesi.jobs.ExtractionJob;
import eu.alertproject.kesi.model.Bugzilla;
import eu.alertproject.kesi.model.IssueTracker;
import eu.alertproject.kesi.model.Jira;
import eu.alertproject.kesi.model.Repository;
import eu.alertproject.kesi.model.StructuredKnowledgeSource;
import eu.alertproject.kesi.sources.SourcesManager;

/**
 * Singleton implementation based on enum types. See Joshua Bloch's
 * conference in the Google I/O 2008 <a href=
 * "http://sites.google.com/site/io/effective-java-reloaded/effective_java_reloaded.pdf"
 * ></a>
 **/

public enum PreferencesManager {
    INSTANCE;

    /*
     * Preference keys
     */
    public static final String PREF_DB_USERNAME = "username";
    public static final String PREF_DB_PASSWORD = "password";
    public static final String PREF_DB_HOST = "host";
    public static final String PREF_DB_PORT = "port";
    public static final String PREF_DB_DBMS = "dbms";
    public static final String PREF_DB_DATABASE_ITS = "database.its";
    public static final String PREF_DB_DATABASE_SCM = "database.scm";
    public static final String PREF_LOGGER_FILE_PATH = "logpath";
    public static final String PREF_LOGGER_LEVEL = "level";
    public static final String PREF_PUBLISHER_URL = "url";
    public static final String PREF_PUBLISHER_LIMIT = "setMessagesLimit";
    public static final String PREF_PUBLISHER_MAX_MESSAGES = "maxMessages";
    public static final String PREF_PUBLISHER_EVENTS_PATH = "eventsPath";
    public static final String PREF_PUBLISHER_DEBUG = "debug";
    public static final String PREF_SENSOR_USERNAME = "username";
    public static final String PREF_SENSOR_PASSWORD = "password";
    public static final String PREF_SENSOR_PROTOCOL = "protocol";
    public static final String PREF_SENSOR_HOST = "host";
    public static final String PREF_SENSOR_PORT = "port";
    public static final String PREF_SENSOR_EMAIL_FOLDER = "folder";
    public static final String PREF_SENSOR_TIMEOUT = "timeout";
    public static final String PREF_SENSOR_POLL_INTERVAL = "polling";
    public static final String PREF_SOURCES_DOWNLOAD_PATH = "sourcesPath";
    public static final String PREF_SOURCES_URI = "uri";
    public static final String PREF_SOURCES_TYPE = "type";
    public static final String PREF_SOURCES_USER = "user";
    public static final String PREF_SOURCES_PASSWORD = "password";
    public static final String PREF_SOURCES_ON_START = "onStart";
    public static final String PREF_SOURCES_LAST_SENT = "lastSent";

    /*
     * Startup values
     */
    public static final String EXTRACT = "extract";
    public static final String PUBLISH = "publish";
    public static final String EXTRACT_AND_PUBLISH = "both";

    /*
     * Logger levels
     */
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";
    public static final String FATAL = "FATAL";

    /*
     * Default values
     */
    public static final String DEF_VALUE = null;
    public static final String DEF_DB_USERNAME = "root";
    public static final String DEF_DB_PASSWORD = "";
    public static final String DEF_DB_HOST = "localhost";
    public static final String DEF_DB_PORT = "3306";
    public static final String DEF_DB_DBMS = "mysql";
    public static final String DEF_DB_DATABASE_ITS = "its";
    public static final String DEF_DB_DATABASE_SCM = "scm";
    public static final String DEF_LOGGER_FILE_PATH = "/tmp/kesi.log";
    public static final String DEF_LOGGER_LEVEL = "info";
    public static final String DEF_PUBLISHER_URL = "failover://tcp://localhost:61616";
    public static final Boolean DEF_PUBLISHER_LIMIT = false;
    public static final int DEF_PUBLISHER_MAX_MESSAGES = 50000;
    public static final Boolean DEF_PUBLISHER_DEBUG = false;
    public static final String DEF_SENSOR_PROTOCOL = "imaps";
    public static final String DEF_SENSOR_EMAIL_FOLDER = "Inbox";
    public static final int DEF_SENSOR_TIMEOUT = 5000;
    public static final int DEF_SENSOR_POLL_INTERVAL = 10000;
    public static final String DEF_SOURCES_DOWNLOAD_PATH = "/tmp/";
    public static final String DEF_SOURCES_LAST_SENT = "0001-01-01 00:00:00";

    /*
     * Preferences node roots names
     */
    private static final String DATABASE_NODE_ROOT = "database";
    private static final String LOGGER_NODE_ROOT = "logger";
    private static final String PUBlISHER_NODE_ROOT = "publisher";
    private static final String SENSOR_NODE_ROOT = "sensor";
    private static final String SOURCES_NODE_ROOT = "sources";
    public static final String SOURCES_SCM_NODE_ROOT = "scm";
    public static final String SOURCES_ITS_NODE_ROOT = "its";

    private Preferences prefs;
    private Preferences db;
    private Preferences logger;
    private Preferences publisher;
    private Preferences sensor;
    private Preferences sources;

    public void setUpPreferences() {
        prefs = Preferences.userNodeForPackage(KESI.class);
        loadComponentsPreferences();
    }

    public void setUpPreferences(String filename) throws PreferencesError {
        File backup;

        prefs = Preferences.userNodeForPackage(KESI.class);
        backup = savePreferences();

        try {
            prefs.removeNode();
        } catch (BackingStoreException e) {
            String msg = String.format("Unable to clear the configuration",
                    e.getMessage());
            throw new PreferencesError(msg);
        }

        try {
            loadPreferences(filename);
        } catch (PreferencesError e) {
            String temp = backup.getAbsolutePath();

            try {
                importPreferences(temp);
            } catch (IOException e1) {
                String msg = String.format(
                        "%s. Error restoring %s backup config file. %s",
                        e.getMessage(), temp, e1.getMessage());
                throw new PreferencesError(msg);
            } catch (InvalidPreferencesFormatException e1) {
                String msg = String.format(
                        "%s. Error restoring %s backup config file. %s",
                        e.getMessage(), temp, e1.getMessage());
                throw new PreferencesError(msg);
            }

            throw e;
        }

        loadComponentsPreferences();
    }

    private void loadComponentsPreferences() {
        loadDatabasePreferences();
        loadLoggerPreferences();
        loadSourcesPreferences();
        loadSensorPreferences();
        loadPublisherPreferences();
    }

    private File savePreferences() throws PreferencesError {
        try {
            File temp;
            FileOutputStream fos;

            temp = File.createTempFile("kesi-prefs-", ".tmp");
            temp.setReadable(true, true);
            temp.setWritable(true, true);
            temp.setExecutable(false);

            fos = new FileOutputStream(temp);
            prefs.exportSubtree(fos);

            return temp;
        } catch (IOException e) {
            String msg = String.format(
                    "Error creating preferences backup file. %s",
                    e.getMessage());
            throw new PreferencesError(msg);
        } catch (BackingStoreException e) {
            String msg = String.format("Error backing preferences. %s",
                    e.getMessage());
            throw new PreferencesError(msg);
        }
    }

    private void loadPreferences(String filename) throws PreferencesError {
        try {
            importPreferences(filename);
        } catch (IOException e) {
            String msg = String.format("Error opening %s config file. %s",
                    filename, e.getMessage());
            throw new PreferencesError(msg);
        } catch (InvalidPreferencesFormatException e) {
            String msg = String.format("Error in config file format. %s",
                    e.getMessage());
            throw new PreferencesError(msg);
        }
    }

    private void importPreferences(String filename) throws IOException,
            InvalidPreferencesFormatException {
        FileInputStream fis = new FileInputStream(filename);
        Preferences.importPreferences(fis);
        fis.close();
        prefs = Preferences.userNodeForPackage(KESI.class);
    }

    private void loadDatabasePreferences() {
        db = prefs.node(DATABASE_NODE_ROOT);
    }

    private void loadLoggerPreferences() {
        logger = prefs.node(LOGGER_NODE_ROOT);
    }

    private void loadSourcesPreferences() {
        sources = prefs.node(SOURCES_NODE_ROOT);
    }

    private void loadSensorPreferences() {
        sensor = prefs.node(SENSOR_NODE_ROOT);
    }

    private void loadPublisherPreferences() {
        publisher = prefs.node(PUBlISHER_NODE_ROOT);
    }

    /*
     * Database preferences getters
     */
    public String getDatabaseUsername() {
        return db.get(PREF_DB_USERNAME, DEF_DB_USERNAME);
    }

    public String getDatabasePassword() {
        return db.get(PREF_DB_PASSWORD, DEF_DB_PASSWORD);
    }

    public String getDatabaseHost() {
        return db.get(PREF_DB_HOST, DEF_DB_HOST);
    }

    public String getDatabasePort() {
        return db.get(PREF_DB_PORT, DEF_DB_PORT);
    }

    public String getDatabaseNameITS() {
        return db.get(PREF_DB_DATABASE_ITS, DEF_DB_DATABASE_ITS);
    }

    public String getDatabaseNameSCM() {
        return db.get(PREF_DB_DATABASE_SCM, DEF_DB_DATABASE_SCM);
    }

    /*
     * Logger preferences getters
     */
    public String getLoggerFilePath() {
        return logger.get(PREF_LOGGER_FILE_PATH, DEF_LOGGER_FILE_PATH);
    }

    public String getLoggerLevel() throws PreferencesError {
        String level = logger.get(PREF_LOGGER_LEVEL, DEF_LOGGER_LEVEL);

        if (!checkLoggerLevel(level)) {
            String msg = String.format("Invalid %s level for logger levels.",
                    level);
            throw new PreferencesError(msg);
        }

        return level;
    }

    /*
     * Event publisher preferences getters
     */
    public String getPublisherURL() {
        return publisher.get(PREF_PUBLISHER_URL, DEF_VALUE);
    }

    public Boolean getPublisherLimit() {
        return publisher.getBoolean(PREF_PUBLISHER_LIMIT, DEF_PUBLISHER_LIMIT);
    }

    public int getPublisherMaxMessages() {
        return publisher.getInt(PREF_PUBLISHER_MAX_MESSAGES,
                DEF_PUBLISHER_MAX_MESSAGES);
    }

    public String getPublisherEventsFilePath() {
        return publisher.get(PREF_PUBLISHER_EVENTS_PATH, DEF_VALUE);
    }

    public Boolean getPublisherDebugMode() {
        return publisher.getBoolean(PREF_PUBLISHER_DEBUG, DEF_PUBLISHER_DEBUG);
    }

    /*
     * Sensor preferences getters
     */
    public String getSensorUser() {
        return sensor.get(PREF_SENSOR_USERNAME, DEF_VALUE);
    }

    public String getSensorPassword() {
        return sensor.get(PREF_SENSOR_PASSWORD, DEF_VALUE);
    }

    public String getSensorHost() {
        return sensor.get(PREF_SENSOR_HOST, DEF_VALUE);
    }

    public String getSensorProtocol() {
        return sensor.get(PREF_SENSOR_PROTOCOL, DEF_SENSOR_PROTOCOL);
    }

    public String getSensorEmailFolder() {
        return sensor.get(PREF_SENSOR_EMAIL_FOLDER, DEF_SENSOR_EMAIL_FOLDER);
    }

    public int getSensorTimeout() {
        return sensor.getInt(PREF_SENSOR_TIMEOUT, DEF_SENSOR_TIMEOUT);
    }

    public int getSensorPollInterval() {
        return sensor.getInt(PREF_SENSOR_POLL_INTERVAL,
                DEF_SENSOR_POLL_INTERVAL);
    }

    /*
     * Knowledge Sources preferences getters
     */
    public ArrayList<StructuredKnowledgeSource> getSources()
            throws PreferencesError {
        ArrayList<StructuredKnowledgeSource> sources = new ArrayList<StructuredKnowledgeSource>();

        sources.addAll(getSourcesITS());
        sources.addAll(getSourcesSCM());

        return sources;
    }

    public ArrayList<IssueTracker> getSourcesITS() throws PreferencesError {
        ArrayList<IssueTracker> trackers;
        Preferences node;
        String[] ids;

        try {
            trackers = new ArrayList<IssueTracker>();

            if (!sources.nodeExists(SOURCES_ITS_NODE_ROOT)) {
                return trackers; /* Empty list */
            }

            node = sources.node(SOURCES_ITS_NODE_ROOT);
            ids = node.childrenNames();
        } catch (BackingStoreException e) {
            String msg = "Error parsing knowledge sources. " + e.getMessage();
            throw new PreferencesError(msg);
        }

        for (String srcID : ids) {
            StructuredKnowledgeSource src = parseSource(node, srcID);
            trackers.add((IssueTracker) src);
        }

        return trackers;
    }

    public ArrayList<Repository> getSourcesSCM() throws PreferencesError {
        ArrayList<Repository> repositories;
        Preferences node;
        String[] ids;

        try {
            repositories = new ArrayList<Repository>();

            if (!sources.nodeExists(SOURCES_SCM_NODE_ROOT)) {
                return repositories; /* Empty list */
            }

            node = sources.node(SOURCES_SCM_NODE_ROOT);
            ids = node.childrenNames();
        } catch (BackingStoreException e) {
            String msg = "Error parsing knowledge sources. " + e.getMessage();
            throw new PreferencesError(msg);
        }

        for (String srcID : ids) {
            StructuredKnowledgeSource src = parseSource(node, srcID);
            repositories.add((Repository) src);
        }

        return repositories;
    }

    public void updateSource(StructuredKnowledgeSource src)
            throws PreferencesError {
        Preferences sourceNode;

        if (src instanceof IssueTracker) {
            sourceNode = sources.node(SOURCES_ITS_NODE_ROOT);
        } else {
            sourceNode = sources.node(SOURCES_SCM_NODE_ROOT);
        }

        sourceNode = sourceNode.node(src.getId());
        sourceNode.put(PREF_SOURCES_LAST_SENT,
                Database.dateToString(src.getDate()));

        try {
            sourceNode.sync();
        } catch (BackingStoreException e) {
            String msg = "Error updating source. " + e.getMessage();
            throw new PreferencesError(msg);
        }
    }

    public String getSourcesDownloadPath() {
        return sources.get(PREF_SOURCES_DOWNLOAD_PATH,
                DEF_SOURCES_DOWNLOAD_PATH);
    }

    /*
     * Logger preferences private methods
     */
    private boolean checkLoggerLevel(String level) {
        String aux = level.toUpperCase();

        return aux.equals(DEBUG) || aux.equals(INFO) || aux.equals(WARN)
                || aux.equals(ERROR) || aux.equals(FATAL);
    }

    /*
     * Sources preferences private methods
     */
    private StructuredKnowledgeSource parseSource(Preferences prefs, String id)
            throws PreferencesError {
        Preferences node;
        String type;
        URI uri;
        StructuredKnowledgeSource source;

        node = prefs.node(id);
        type = getSourceType(node, id);
        uri = getSourceURI(node, id);

        if (type.equals(SourcesManager.BUGZILLA)) {
            source = new Bugzilla(id, uri);
        } else if (type.equals(SourcesManager.JIRA)) {
            source = new Jira(id, uri);
        } else {
            source = new Repository(id, uri, type);
        }

        source.setUser(getSourceUser(node, id));
        source.setPassword(getSourcePassword(node, id));
        source.setSetup(getSourceOnStart(node, id));
        source.setDate(getSourceLastSent(node, id));

        return source;
    }

    private URI getSourceURI(Preferences prefs, String id)
            throws PreferencesError {
        String rawURI = prefs.get(PREF_SOURCES_URI, DEF_VALUE);

        if (rawURI.equals(DEF_VALUE)) {
            String msg = String.format("Missing value for %s key in %s source",
                    PREF_SOURCES_URI, id);
            throw new PreferencesError(msg);
        }

        try {
            return new URI(rawURI);
        } catch (URISyntaxException e) {
            String msg = String.format("Invalid uri %s in %s source", rawURI,
                    id);
            throw new PreferencesError(msg);
        }
    }

    private String getSourceType(Preferences prefs, String id)
            throws PreferencesError {
        String type = prefs.get(PREF_SOURCES_TYPE, DEF_VALUE);

        if (type.equals(DEF_VALUE)) {
            String msg = String.format("Missing value for %s key in %s source",
                    PREF_SOURCES_TYPE, id);
            throw new PreferencesError(msg);
        }

        return type;
    }

    private String getSourceUser(Preferences prefs, String id) {
        return prefs.get(PREF_SOURCES_USER, DEF_VALUE);
    }

    private String getSourcePassword(Preferences prefs, String id) {
        return prefs.get(PREF_SOURCES_PASSWORD, DEF_VALUE);
    }

    private int getSourceOnStart(Preferences prefs, String id)
            throws PreferencesError {
        String onStart = prefs.get(PREF_SOURCES_ON_START, DEF_VALUE);

        try {
            return convertToOnStartJobTypes(onStart);
        } catch (PreferencesError e) {
            String msg = String.format("Invalid onStart value % in %s source ",
                    onStart, id);
            throw new PreferencesError(msg);
        }
    }

    private Date getSourceLastSent(Preferences prefs, String id)
            throws PreferencesError {
        String date = prefs.get(PREF_SOURCES_LAST_SENT, DEF_SOURCES_LAST_SENT);

        try {
            return Database.stringToDate(date);
        } catch (ParseException e) {
            String msg = String.format(
                    "Invalid last sent date %s in %s source", date, id);
            throw new PreferencesError(msg);
        }
    }

    private int convertToOnStartJobTypes(String onStart)
            throws PreferencesError {
        if (onStart == null) {
            return ExtractionJob.EXTRACT_AND_PUBLISH;
        }

        if (!checkOnStartValue(onStart)) {
            String msg = String.format("Invalid onStart value %", onStart);
            throw new PreferencesError(msg);
        }

        if (onStart.equals(PreferencesManager.EXTRACT)) {
            return ExtractionJob.EXTRACT;
        } else if (onStart.equals(PreferencesManager.PUBLISH)) {
            return ExtractionJob.PUBLISH;
        } else {
            return ExtractionJob.EXTRACT_AND_PUBLISH;
        }
    }

    private boolean checkOnStartValue(String onStart) {
        String aux = onStart.toLowerCase();

        return aux.equals(EXTRACT) || aux.equals(PUBLISH)
                || aux.equals(EXTRACT_AND_PUBLISH);
    }

}

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
 */

package eu.alertproject.kesi.database;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.events.EventSummary;
import eu.alertproject.kesi.model.Action;
import eu.alertproject.kesi.model.Commit;
import eu.alertproject.kesi.model.File;
import eu.alertproject.kesi.model.Function;
import eu.alertproject.kesi.model.Module;
import eu.alertproject.kesi.model.Person;

public class SCMRetrieval extends Database {
    /* Event summary types */
    private static final String EVENT_TYPE_COMMIT = "commit";

    /* Keys for actions */
    private static final String SCM_ADD_ACTION = "A";
    private static final String SCM_DELETE_ACTION = "D";
    private static final String SCM_MODIFY_ACTION = "M";
    private static final String SCM_COPY_ACTION = "C";
    private static final String SCM_MOVE_ACTION = "V";
    private static final String SCM_REPLACE_ACTION = "R";

    /* Event summary row fields */
    private static final String SCM_EVENTS_ID = "event_id";
    private static final String SCM_EVENTS_COMMIT_ID = "commit_id";
    private static final String SCM_EVENTS_DATE = "date";
    private static final String SCM_EVENTS_TYPE = "commit";

    /* Commit row fields */
    private static final String SCM_COMMIT_MSG = "message";
    private static final String SCM_COMMIT_DATE = "date";
    private static final String SCM_COMMIT_REVISION = "rev";
    private static final String SCM_COMMIT_REPO = "repository_id";
    private static final String SCM_AUTHOR_ID = "author_id";
    private static final String SCM_COMMITTER_ID = "committer_id";
    private static final String SCM_ACTION_TYPE = "type";

    /* Person row fields */
    private static final String SCM_PERSON_NAME = "name";
    private static final String SCM_PERSON_EMAIL = "email";

    /* Repository row fields */
    private static final String SCM_REPOSITORY_URI = "uri";

    /* File row fields */
    private static final String SCM_FILE_ID = "id";
    private static final String SCM_FILE_PATH = "file_path";

    /* Branch row fields */
    private static final String SCM_BRANCH_NAME = "name";

    /* Module and function row fields */
    private static final String SCM_MODULE_ID = "id";
    private static final String SCM_MODULE_NAME = "name";
    private static final String SCM_FUNCTION_HEADER = "header";
    private static final String SCM_SRC_START_LINE = "start_line";
    private static final String SCM_SRC_END_LINE = "end_line";

    /* SCM generic queries */
    private static final String SCM_QUERY_EVENTS = "SELECT log.id event_id, log.id commit_id, log.date date, 'commit' "
            + "FROM scmlog log, repositories r WHERE log.repository_id = r.id "
            + "AND r.uri = ? AND log.date > ? ORDER BY log.date";
    private static final String SCM_QUERY_COMMIT = "SELECT rev, date, message,"
            + "author_id, committer_id, repository_id "
            + "FROM scmlog WHERE id = ?";
    private static final String SCM_QUERY_PERSON = "SELECT name, email "
            + "FROM people WHERE id = ?";
    private static final String SCM_QUERY_REPOSITORY = "SELECT uri FROM repositories WHERE id = ?";
    private static final String SCM_QUERY_FILE = "SELECT f.id id "
            + "FROM files f, actions a WHERE f.id = a.file_id AND a.commit_id = ?";
    private static final String SCM_QUERY_FILE_PATH = "SELECT file_path "
            + "FROM file_links WHERE file_id = ? AND commit_id <= ? ORDER BY commit_id DESC LIMIT 1";
    private static final String SCM_QUERY_ACTION = "SELECT id, branch_id, type "
            + "FROM actions WHERE file_id = ? AND commit_id = ?";
    private static final String SCM_QUERY_BRANCH = "SELECT name FROM branches, "
            + "actions WHERE branches.id = actions.branch_id AND commit_id = ?";
    private static final String SCM_QUERY_MODULES = "SELECT id, name, start_line, end_line "
            + "FROM modules_src WHERE file_id = ? AND commit_id = ?";
    private static final String SCM_QUERY_FUNCTIONS = "SELECT header, start_line, end_line "
            + "FROM functions_src WHERE module_id= ?";

    private static Logger scmDBLogger = Logger.getLogger(SCMRetrieval.class);

    /* Person cache */
    private final HashMap<Integer, Person> people;
    /* Repositories cache */
    private final HashMap<Integer, URI> repositories;

    public SCMRetrieval(String driver, String username, String password,
            String host, String port, String database)
            throws DriverNotSupportedError, DatabaseConnectionError {
        super(driver, username, password, host, port, database);
        super.logger = scmDBLogger;
        people = new HashMap<Integer, Person>();
        repositories = new HashMap<Integer, URI>();
    }

    public ArrayList<EventSummary> getEventsSummary(String repositoryURL,
            Timestamp lastSent) throws DatabaseExtractionError {
        ArrayList<EventSummary> events = new ArrayList<EventSummary>();

        try {
            PreparedStatement stmt;
            ResultSet rs;

            stmt = prepareStatement(SCM_QUERY_EVENTS);
            stmt.setString(1, repositoryURL);
            stmt.setString(2, Database.dateToString(lastSent));
            rs = executeQuery(stmt);

            while (rs.next()) {
                EventSummary summary = createEventSummary(rs);
                events.add(summary);
            }

            stmt.close();

            return events;
        } catch (SQLException e) {
            String msg = "Error getting summary of issues. " + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseExtractionError(msg);
        }
    }

    public Commit getCommitFromSummary(EventSummary summary)
            throws DatabaseExtractionError {
        int commitID;
        int type;
        Commit commit;

        commitID = summary.getEventKey();
        type = summary.getType();

        if (type == EventSummary.COMMIT_NEW) {
            commit = getEventCommitNew(commitID);
        } else {
            String msg = "Invalid event type " + type;
            throw new DatabaseExtractionError(msg);
        }

        return commit;
    }

    protected Commit getEventCommitNew(int commitID)
            throws DatabaseExtractionError {
        try {
            int repoID;
            String message;
            String revision;
            Timestamp date;
            int authorID;
            int committerID;
            URI repoURI;
            Commit commit;
            Person committer;
            Person author;
            ArrayList<File> files;
            PreparedStatement stmt;
            ResultSet rs;

            stmt = prepareStatement(SCM_QUERY_COMMIT);
            stmt.setInt(1, commitID);
            rs = executeQuery(stmt);
            rs.first();

            repoID = rs.getInt(SCM_COMMIT_REPO);
            message = rs.getString(SCM_COMMIT_MSG);
            revision = rs.getString(SCM_COMMIT_REVISION);
            date = rs.getTimestamp(SCM_COMMIT_DATE);

            authorID = rs.getInt(SCM_AUTHOR_ID);
            author = (rs.wasNull() ? null : getPerson(authorID));

            committerID = rs.getInt(SCM_COMMITTER_ID);
            committer = getPerson(committerID);

            commit = new Commit(message, date.toString(), revision, author,
                    committer, commitID);

            repoURI = getRepositoryURI(repoID);
            commit.setRepositoryURI(repoURI);

            files = getFiles(commitID);
            for (File f : files) {
                commit.addFile(f);
            }

            stmt.close();

            return commit;
        } catch (SQLException e) {
            String msg = "Error getting commit " + commitID + "."
                    + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseExtractionError(msg);
        }
    }

    protected Person getPerson(int userID) throws SQLException {
        String name;
        String email;
        String id;
        Person person;
        PreparedStatement stmt;
        ResultSet rs;

        if (people.containsKey(userID)) {
            return people.get(userID);
        }

        stmt = prepareStatement(SCM_QUERY_PERSON);
        stmt.setInt(1, userID);
        rs = executeQuery(stmt);
        rs.first();

        name = rs.getString(SCM_PERSON_NAME);
        email = rs.getString(SCM_PERSON_EMAIL);

        stmt.close();

        id = (email == null ? name : email);

        person = new Person(name, email, id);

        people.put(userID, person);

        return person;
    }

    protected URI getRepositoryURI(int repoID) throws SQLException {
        URI uri;
        PreparedStatement stmt;
        ResultSet rs;

        if (repositories.containsKey(repoID)) {
            return repositories.get(repoID);
        }

        stmt = prepareStatement(SCM_QUERY_REPOSITORY);
        stmt.setInt(1, repoID);
        rs = executeQuery(stmt);
        rs.first();

        try {
            uri = new URI(rs.getString(SCM_REPOSITORY_URI));
        } catch (URISyntaxException e) {
            logger.error("Error converting URI for repository " + repoID
                    + ". Set to null.", e);
            uri = null;
        }

        stmt.close();

        repositories.put(repoID, uri);

        return uri;
    }

    protected ArrayList<File> getFiles(int commitID) throws SQLException {
        ArrayList<File> files;
        PreparedStatement stmt;
        ResultSet rs;

        files = new ArrayList<File>();

        stmt = prepareStatement(SCM_QUERY_FILE);
        stmt.setInt(1, commitID);
        rs = executeQuery(stmt);

        while (rs.next()) {
            int fileID = rs.getInt(SCM_FILE_ID);
            File file = getFile(fileID, commitID);

            files.add(file);
        }

        stmt.close();

        return files;
    }

    protected File getFile(int fileID, int commitID) throws SQLException {
        String action;
        String branch;
        String filePath;
        File file;
        ArrayList<Module> modules;

        filePath = getFilePath(fileID, commitID);

        file = new File(filePath);

        action = getAction(fileID, commitID);
        branch = getBranch(commitID);

        file.setSimpleAction(action);
        file.setBranch(branch);

        modules = getModules(fileID, commitID);

        for (Module module : modules) {
            file.addModule(module);
        }

        return file;
    }

    protected String getFilePath(int fileID, int commitID) throws SQLException {
        String filePath;
        PreparedStatement stmt;
        ResultSet rs;

        stmt = prepareStatement(SCM_QUERY_FILE_PATH);
        stmt.setInt(1, fileID);
        stmt.setInt(2, commitID);
        rs = executeQuery(stmt);
        rs.first();

        filePath = rs.getString(SCM_FILE_PATH);
        stmt.close();

        return filePath;
    }

    protected String getAction(int fileID, int commitID) throws SQLException {
        String actionType;
        String action;
        PreparedStatement stmt;
        ResultSet rs;

        stmt = prepareStatement(SCM_QUERY_ACTION);
        stmt.setInt(1, fileID);
        stmt.setInt(2, commitID);
        rs = executeQuery(stmt);
        rs.first();

        actionType = rs.getString(SCM_ACTION_TYPE);

        stmt.close();

        if (actionType.equals(SCM_ADD_ACTION)) {
            action = Action.ADD;
        } else if (actionType.equals(SCM_COPY_ACTION)) {
            action = Action.COPY;
        } else if (actionType.equals(SCM_DELETE_ACTION)) {
            action = Action.DELETE;
        } else if (actionType.equals(SCM_MODIFY_ACTION)) {
            action = Action.MODIFY;
        } else if (actionType.equals(SCM_MOVE_ACTION)) {
            action = Action.MOVE;
        } else if (actionType.equals(SCM_REPLACE_ACTION)) {
            action = Action.REPLACE;
        } else {
            action = "Unknown";
        }

        return action;
    }

    protected String getBranch(int commitID) throws SQLException {
        String branch;
        PreparedStatement stmt;
        ResultSet rs;

        stmt = prepareStatement(SCM_QUERY_BRANCH);
        stmt.setInt(1, commitID);
        rs = executeQuery(stmt);
        rs.first();

        branch = rs.getString(SCM_BRANCH_NAME);

        stmt.close();

        return branch;
    }

    protected ArrayList<Module> getModules(int fileID, int commitID)
            throws SQLException {
        ArrayList<Module> modules;
        PreparedStatement stmt;
        ResultSet rs;

        stmt = prepareStatement(SCM_QUERY_MODULES);
        stmt.setInt(1, fileID);
        stmt.setInt(2, commitID);
        rs = executeQuery(stmt);

        modules = new ArrayList<Module>();

        while (rs.next()) {
            int moduleID = rs.getInt(SCM_MODULE_ID);
            String name = rs.getString(SCM_MODULE_NAME);
            int startLine = rs.getInt(SCM_SRC_START_LINE);
            int endLine = rs.getInt(SCM_SRC_END_LINE);
            ArrayList<Function> functions = getFunctions(moduleID);
            Module module = new Module(name, startLine, endLine);

            for (Function function : functions) {
                module.addFunction(function);
            }

            modules.add(module);
        }

        stmt.close();

        return modules;
    }

    protected ArrayList<Function> getFunctions(int moduleID)
            throws SQLException {
        ArrayList<Function> functions;
        PreparedStatement stmt;
        ResultSet rs;

        stmt = prepareStatement(SCM_QUERY_FUNCTIONS);
        stmt.setInt(1, moduleID);
        rs = executeQuery(stmt);

        functions = new ArrayList<Function>();

        while (rs.next()) {
            String header = rs.getString(SCM_FUNCTION_HEADER);
            int startLine = rs.getInt(SCM_SRC_START_LINE);
            int endLine = rs.getInt(SCM_SRC_END_LINE);

            functions.add(new Function(header, startLine, endLine));
        }

        stmt.close();

        return functions;
    }

    private EventSummary createEventSummary(ResultSet rs)
            throws DatabaseExtractionError {
        int eventID;
        int commitID;
        int eventType;
        String type;
        Timestamp date;
        EventSummary summary;

        try {
            eventID = rs.getInt(SCM_EVENTS_ID);
            commitID = rs.getInt(SCM_EVENTS_COMMIT_ID);
            type = rs.getString(SCM_EVENTS_TYPE);
            date = rs.getTimestamp(SCM_EVENTS_DATE);
        } catch (SQLException e) {
            String msg = "Error getting summary of events. " + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseExtractionError(msg);
        }

        if (type.equals(EVENT_TYPE_COMMIT)) {
            eventType = EventSummary.COMMIT_NEW;
        } else {
            String msg = "Invalid type of event " + type;
            throw new DatabaseExtractionError(msg);
        }

        summary = new EventSummary(eventID, eventType, date);
        summary.setEventKey(commitID);

        return summary;
    }

}

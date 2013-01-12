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

package eu.alertproject.kesi.database;

import eu.alertproject.kesi.PreferencesManager;
import eu.alertproject.kesi.model.Bugzilla;
import eu.alertproject.kesi.model.Jira;
import eu.alertproject.kesi.model.Repository;
import eu.alertproject.kesi.model.StructuredKnowledgeSource;

public class DatabaseFactory {

    public Database createDatabase(StructuredKnowledgeSource source)
            throws DriverNotSupportedError, DatabaseConnectionError,
            DatabaseNotSupportedError {
        Database extractor;

        String username = PreferencesManager.INSTANCE.getDatabaseUsername();
        String password = PreferencesManager.INSTANCE.getDatabasePassword();
        String host = PreferencesManager.INSTANCE.getDatabaseHost();
        String port = PreferencesManager.INSTANCE.getDatabasePort();

        if (source instanceof Repository) {
            String database = PreferencesManager.INSTANCE.getDatabaseNameSCM();

            extractor = new SCMRetrieval(PreferencesManager.DEF_DB_DBMS,
                    username, password, host, port, database);
        } else if (source instanceof Bugzilla) {
            String database = PreferencesManager.INSTANCE.getDatabaseNameITS();

            extractor = new BugzillaRetrieval(PreferencesManager.DEF_DB_DBMS,
                    username, password, host, port, database);
        } else if (source instanceof Jira) {
            String database = PreferencesManager.INSTANCE.getDatabaseNameITS();

            extractor = new JiraRetrieval(PreferencesManager.DEF_DB_DBMS,
                    username, password, host, port, database);
        } else {
            throw new DatabaseNotSupportedError("Database " + source.getType()
                    + " not supported");
        }

        return extractor;
    }

}

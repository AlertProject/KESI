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

package eu.alertproject.kesi.extractors;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.PreferencesManager;
import eu.alertproject.kesi.jobs.ExtractionJob;
import eu.alertproject.kesi.jobs.Queue;

public class ITSExtractor extends Extractor {
    /* Tool for extracting data from ITSs */
    private static final String ITS_EXTRACTOR = "bicho";

    /* Supported ITSs */
    public static final String ITS_BUGZILLA = "bg";
    public static final String ITS_JIRA = "jira";
    public static final String ITS_GITHUB = "github";

    private static List<String> SUPPORTED_ITS = Arrays.asList(ITS_BUGZILLA,
            ITS_JIRA, ITS_GITHUB);

    /* Logger for ITS classes */
    private static final Logger itsLogger = Logger
            .getLogger(ITSExtractor.class);

    public ITSExtractor(Queue<ExtractionJob> queue) {
        super(ITS_EXTRACTOR, queue);
        super.logger = itsLogger;
    }

    @Override
    // FIXME!!!!!!!!!!!!!!!!!!
    public String[] getCommandExtractor(String url, String type) {
        if (!SUPPORTED_ITS.contains(type)) {
            System.err.println("Invalid type");
        }

        String dbUsername = PreferencesManager.INSTANCE.getDatabaseUsername();
        String dbPassword = PreferencesManager.INSTANCE.getDatabasePassword();
        String dbHost = PreferencesManager.INSTANCE.getDatabaseHost();
        String dbPort = PreferencesManager.INSTANCE.getDatabasePort();
        String database = PreferencesManager.INSTANCE.getDatabaseNameITS();

        String[] cmd = { ITS_EXTRACTOR, "-o", "db", "-b", type,
                "--db-user-out", dbUsername, "--db-password-out", dbPassword,
                "--db-hostname-out", dbHost, "--db-port-out", dbPort,
                "--db-database-out", database, "-d", "1", "-u", url };
        return cmd;
    }

    @Override
    // FIXME!!!!!!!!!!!!!!!!!!
    public String[] getCommandExtractor(String url, String type, String user,
            String password) {
        if (!SUPPORTED_ITS.contains(type)) {
            System.err.println("Invalid type");
        }

        String dbUsername = PreferencesManager.INSTANCE.getDatabaseUsername();
        String dbPassword = PreferencesManager.INSTANCE.getDatabasePassword();
        String dbHost = PreferencesManager.INSTANCE.getDatabaseHost();
        String dbPort = PreferencesManager.INSTANCE.getDatabasePort();
        String database = PreferencesManager.INSTANCE.getDatabaseNameITS();

        String[] cmd = { ITS_EXTRACTOR, "-o", "db", "-b", type,
                "--db-user-out", dbUsername, "--db-password-out", dbPassword,
                "--db-hostname-out", dbHost, "--db-port-out", dbPort,
                "--db-database-out", database, "--backend-user", user,
                "--backend-password", password, "-d", "1", "-u", url };
        return cmd;
    }

}

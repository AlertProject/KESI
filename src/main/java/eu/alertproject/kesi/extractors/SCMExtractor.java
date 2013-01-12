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

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.PreferencesManager;
import eu.alertproject.kesi.database.Database;
import eu.alertproject.kesi.jobs.ExtractionJob;
import eu.alertproject.kesi.jobs.Queue;
import eu.alertproject.kesi.sources.SourcesManager;
import eu.alertproject.kesi.sources.SourcesManagerError;

public class SCMExtractor extends Extractor {
    /* Tool for extracting data from SCMs */
    private static final String SCM_EXTRACTOR = "cvsanaly2";

    /* Logger for SCM classes */
    private static final Logger scmLogger = Logger
            .getLogger(SCMExtractor.class);

    public SCMExtractor(Queue<ExtractionJob> queue) {
        super(SCM_EXTRACTOR, queue);
        super.logger = scmLogger;
    }

    @Override
    public String[] getCommandExtractor(String url, String type) {
        String username = PreferencesManager.INSTANCE.getDatabaseUsername();
        String password = PreferencesManager.INSTANCE.getDatabasePassword();
        String host = PreferencesManager.INSTANCE.getDatabaseHost();
        String database = PreferencesManager.INSTANCE.getDatabaseNameSCM();
        String repo;

        if (type.equals("git")) {
            try {
                repo = SourcesManager.INSTANCE.getDownloadSourcePath(url);
            } catch (SourcesManagerError e) {
                repo = null;
            }
        } else {
            repo = url;
        }

        String[] cmd = { SCM_EXTRACTOR, "-u", username, "-p", password, "-d",
                database, "-H", host, "--extensions", "Metrics",
                "--metrics-all", repo };
        super.logger.debug("Command: " + cmd.toString());

        return cmd;
    }

    @Override
    public String[] getCommandExtractor(String url, String type, String user,
            String password) {
        return getCommandExtractor(url, type);
    }

    @Override
    public Timestamp getLastSent() {
        // FIXME: date
        Date date;
        try {
            date = Database.stringToDate("0001-01-01 00:00:01");
        } catch (ParseException e) {
            // FIXME
            date = null;
        }
        return dateToTimestamp(date);
    }

}

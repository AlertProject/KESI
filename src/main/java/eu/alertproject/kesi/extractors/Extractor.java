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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.events.EventGenerator;
import eu.alertproject.kesi.jobs.ExtractionJob;
import eu.alertproject.kesi.jobs.Queue;
import eu.alertproject.kesi.jobs.CommandRunner;
import eu.alertproject.kesi.model.StructuredKnowledgeSource;
import eu.alertproject.kesi.sources.SourcesManager;
import eu.alertproject.kesi.sources.SourcesManagerError;

public abstract class Extractor extends Thread {
    private final String extractor;
    private final Queue<ExtractionJob> queue;

    protected Logger logger = null;

    public Extractor(String extractor, Queue<ExtractionJob> queue) {
        this.extractor = extractor;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            ExtractionJob job;

            while (true) {
                StructuredKnowledgeSource source;
                Timestamp lastSent;

                job = getNextJob();

                try {
                    runJob(job);
                } catch (ExtractionError e) {
                    logger.error("Error running job. Skipping.");
                    continue;
                }

                source = job.getSource();
                lastSent = dateToTimestamp(source.getDate());

                generateEvents(source, lastSent);
            }
        } catch (InterruptedException e) {
            logger.error("", e);
            Thread.currentThread().interrupt();
        }
    }

    public abstract String[] getCommandExtractor(String url, String type);

    public abstract String[] getCommandExtractor(String url, String type,
            String user, String password);

    private ExtractionJob getNextJob() throws InterruptedException {
        return queue.take();
    }

    private void runJob(ExtractionJob job) throws ExtractionError {
        int result;
        String[] cmd;
        String msg;
        CommandRunner tr;
        String url;
        StructuredKnowledgeSource source;

        source = job.getSource();
        url = source.getURI().toASCIIString();

        try {
            SourcesManager.INSTANCE.downloadSource(url);
        } catch (SourcesManagerError e) {
            logger.error("Downloading source", e);
            throw new ExtractionError(e.getMessage());
        }

        msg = "job-" + job.getID() + "(" + url + ") - ";
        logger.info(msg + "SCHEDULED to run");

        if ((source.getUser() != null) && (source.getPassword() != null)) {
            cmd = getCommandExtractor(url, source.getType(), source.getUser(),
                    source.getPassword());
        } else {
            cmd = getCommandExtractor(url, source.getType());
        }

        // FIXME: security issue (user and password)
        // logger.debug(msg + "Command: " + cmd.toString());

        logger.info(msg + "RUNNING");
        tr = new CommandRunner();
        result = tr.run(extractor, cmd, null);

        if (result == 0) {
            logger.info(msg + " FINISHED. Result: " + result);
        } else {
            msg = msg + "FAILURED. Result: " + result;
            logger.error(msg);
            throw new ExtractionError(msg);
        }
    }

    private void generateEvents(StructuredKnowledgeSource source,
            Timestamp fromDate) {
        EventGenerator.INSTANCE.generate(source, fromDate);
    }

    protected Timestamp stringToTimestamp(String d) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd H:m:s");
            Date auxTime = formatter.parse(d);
            return new Timestamp(auxTime.getTime());
        } catch (ParseException e) {
            // FIXME: manage this exception
            logger.error("Error parsing date", e);
            return null;
        }
    }

    protected Timestamp dateToTimestamp(Date d) {
        return new Timestamp(d.getTime());
    }

}

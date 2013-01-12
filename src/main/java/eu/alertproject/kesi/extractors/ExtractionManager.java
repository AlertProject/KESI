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

package eu.alertproject.kesi.extractors;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.jobs.ExtractionJob;
import eu.alertproject.kesi.jobs.Queue;
import eu.alertproject.kesi.model.Repository;
import eu.alertproject.kesi.model.StructuredKnowledgeSource;

public enum ExtractionManager {
    INSTANCE;

    private static Logger logger = Logger.getLogger(ExtractionManager.class);

    private boolean initialized = false;

    /* Job queues */
    private Queue<ExtractionJob> scmJobs;
    private Queue<ExtractionJob> itsJobs;

    /* Extractors */
    private ITSExtractor its;
    private SCMExtractor scm;

    public void start() {
        if (initialized) {
            logger.error("Extraction manager is already running");
            return;
        }

        itsJobs = new Queue<ExtractionJob>();
        scmJobs = new Queue<ExtractionJob>();

        its = new ITSExtractor(itsJobs);
        its.start();

        scm = new SCMExtractor(scmJobs);
        scm.start();

        initialized = true;
    }

    public void stop() {
        if (!initialized) {
            logger.error("Extraction manager is not running");
            return;
        }

        try {
            its.join();
            scm.join();
        } catch (InterruptedException e) {
            logger.error("Unexpected interruption", e);
            throw new RuntimeException(e);
        }

        initialized = false;
    }

    public void extract(StructuredKnowledgeSource source) {
        ExtractionJob job;

        if (!initialized) {
            logger.error("Extraction manager is not running");
            return;
        }

        job = new ExtractionJob(source);

        try {
            if (source instanceof Repository) {
                scmJobs.put(job);
            } else {
                itsJobs.put(job);
            }
        } catch (InterruptedException e) {
            logger.error("Error scheduling sourc esextraction", e);
            throw new RuntimeException(e);
        }
    }

}

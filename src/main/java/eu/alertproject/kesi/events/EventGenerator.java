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

package eu.alertproject.kesi.events;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.database.Database;
import eu.alertproject.kesi.database.DatabaseConnectionError;
import eu.alertproject.kesi.database.DatabaseExtractionError;
import eu.alertproject.kesi.database.DatabaseFactory;
import eu.alertproject.kesi.database.DatabaseNotSupportedError;
import eu.alertproject.kesi.database.DriverNotSupportedError;
import eu.alertproject.kesi.database.EventSet;
import eu.alertproject.kesi.database.EventSetFactory;
import eu.alertproject.kesi.jobs.EventJob;
import eu.alertproject.kesi.jobs.Queue;
import eu.alertproject.kesi.model.StructuredKnowledgeSource;
import eu.alertproject.kesi.publisher.EventPublisher;

public enum EventGenerator {
    INSTANCE;

    private static Logger logger = Logger.getLogger(EventGenerator.class);

    private boolean initialized = false;
    private Queue<EventJob> queue;
    private EventGeneratorThread generator;

    public void start() {
        if (initialized) {
            logger.error("Event generator is already running");
            return;
        }

        queue = new Queue<EventJob>();
        generator = new EventGeneratorThread(queue);
        generator.start();

        initialized = true;
    }

    public void stop() {
        if (!initialized) {
            logger.error("Extractor generator is not running");
            return;
        }

        try {
            generator.join();
        } catch (InterruptedException e) {
            logger.error("Unexpected error stopping EventGenerator thread", e);
            throw new RuntimeException(e);
        }
    }

    public void generate(StructuredKnowledgeSource source, Timestamp fromDate) {
        if (!initialized) {
            logger.error("Extractor generator is not running");
            return;
        }

        EventJob job = new EventJob(source, fromDate);

        try {
            queue.put(job);
        } catch (InterruptedException e) {
            logger.error("Unexpected error in event jobs queue", e);
        }
    }

    private class EventGeneratorThread extends Thread {
        private final DatabaseFactory factory;
        private final Queue<EventJob> queue;

        public EventGeneratorThread(Queue<EventJob> queue) {
            this.queue = queue;
            factory = new DatabaseFactory();
        }

        @Override
        public void run() {
            EventJob job;

            while (true) {
                try {
                    job = queue.take();
                } catch (InterruptedException e) {
                    logger.error("Unexpected error in jobs queue", e);
                    continue;
                }

                generateAndPublish(job);
            }
        }

        private void generateAndPublish(EventJob job) {
            EventSet iterator;
            Event event;
            int numEvents = 0;

            try {
                iterator = generateEvents(job);
            } catch (EventGeneratorError e) {
                String msg = "Error iterator for events. URL: "
                        + job.getSource().getURI().toASCIIString() + "Cause: "
                        + e.getMessage() + ". Ignoring events.";
                logger.error(msg, e);
                return;
            }

            while (iterator.hasNext()) {
                event = iterator.next();

                try {
                    String eventName = event.getEventName();
                    /* Publish event */
                    EventPublisher.INSTANCE.publish(event);
                    logger.info(eventName + " sent");
                    logger.debug(event.getContent());
                    ++numEvents;
                } catch (Exception e) {
                    logger.error("Error publishing message", e);
                }
            }

            logger.info(numEvents + " messages published");
        }

        private EventSet generateEvents(EventJob job)
                throws EventGeneratorError {
            StructuredKnowledgeSource source;
            Database connection;
            EventSetFactory factory;

            source = job.getSource();
            connection = getDatabaseConnection(source);
            factory = new EventSetFactory(connection);

            try {
                return factory.getEventSet(source.getURI().toASCIIString(),
                        job.getFromDate());
            } catch (DatabaseExtractionError e) {
                throw new EventGeneratorError(e.getMessage());
            } catch (DatabaseNotSupportedError e) {
                throw new EventGeneratorError(e.getMessage());
            }
        }

        private Database getDatabaseConnection(StructuredKnowledgeSource source)
                throws EventGeneratorError {
            try {
                return factory.createDatabase(source);
            } catch (DatabaseNotSupportedError e) {
                logger.error(e.getMessage(), e);
                throw new EventGeneratorError("Error connecting to database");
            } catch (DriverNotSupportedError e) {
                logger.error(e.getMessage(), e);
                throw new EventGeneratorError("Error connecting to database");
            } catch (DatabaseConnectionError e) {
                logger.error(e.getMessage(), e);
                throw new EventGeneratorError("Error connecting to database");
            }
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

    }

}

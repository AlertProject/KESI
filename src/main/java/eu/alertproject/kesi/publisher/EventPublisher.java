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

package eu.alertproject.kesi.publisher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.PreferencesError;
import eu.alertproject.kesi.PreferencesManager;
import eu.alertproject.kesi.database.Database;
import eu.alertproject.kesi.events.Event;
import eu.alertproject.kesi.events.EventFactory;
import eu.alertproject.kesi.jobs.Queue;
import eu.alertproject.kesi.model.StructuredKnowledgeSource;
import eu.alertproject.kesi.sources.SourcesManager;
import eu.alertproject.kesi.sources.SourcesManagerError;

/**
 * Singleton implementation based on enum types. See Joshua Bloch's
 * conference in the Google I/O 2008 <a href=
 * "http://sites.google.com/site/io/effective-java-reloaded/effective_java_reloaded.pdf"
 * ></a>
 */
public enum EventPublisher {
    INSTANCE;

    private static Logger logger = Logger.getLogger(EventPublisher.class);

    private Queue<PublicationJob> queue;
    private Publisher publisher;

    public void start() {
        int maxMsgs;
        String url;
        String eventsPath;
        Boolean msgsLimit;
        Boolean debug;

        if (publisher != null) {
            return;
        }

        msgsLimit = PreferencesManager.INSTANCE.getPublisherLimit();

        if (msgsLimit) {
            maxMsgs = PreferencesManager.INSTANCE.getPublisherMaxMessages();
        } else {
            maxMsgs = Publisher.NO_LIMIT;
        }

        url = PreferencesManager.INSTANCE.getPublisherURL();
        eventsPath = PreferencesManager.INSTANCE.getPublisherEventsFilePath();
        debug = PreferencesManager.INSTANCE.getPublisherDebugMode();

        queue = new Queue<PublicationJob>();

        publisher = new Publisher(queue, url, maxMsgs);

        if (eventsPath != null) {
            setExport(eventsPath);
        }

        if (debug) {
            publisher.enableDebugMode();
        }

        publisher.start();
    }

    public void stop() {
        try {
            publisher.join();
        } catch (InterruptedException e) {
            logger.error("Unexpected error stopping EventPublisher thread", e);
            throw new RuntimeException(e);
        }
    }

    public void publish(Event event) {
        PublicationJob job = new PublicationJob(event);

        try {
            queue.put(job);
        } catch (InterruptedException e) {
            logger.error("Unexpected error in event jobs queue", e);
        }
    }

    private void setExport(String eventsPath) {
        boolean success;
        File dir;

        dir = new File(eventsPath);

        if (dir.exists()) {
            publisher.enableExportToFiles(eventsPath);
        } else {
            success = dir.mkdirs();

            if (success) {
                publisher.enableExportToFiles(eventsPath);
            } else {
                String msg = String
                        .format("Error creating events path %s. Export to files disabled.",
                                eventsPath);
                logger.error(msg);
                publisher.disableExportToFiles();
            }
        }
    }

    private class Publisher extends Thread {
        private static final int NO_LIMIT = -1;

        private Context jndiContext;
        private TopicConnection topicConnection;
        private TopicSession topicSession;
        private Topic topicCommitNew;
        private Topic topicIssueNew;
        private Topic topicIssueUpdate;
        private final Queue<PublicationJob> queue;
        private final String url;
        private String eventsPath;
        private boolean debug;
        private boolean export;
        private int limit;
        private int count;

        public Publisher(Queue<PublicationJob> queue, String url, int limit) {
            this.queue = queue;
            this.url = url;
            this.export = false;
            this.debug = false;
            this.limit = limit;
            this.count = 0;
        }

        @Override
        public void run() {
            try {
                setUp();
            } catch (JMSException e) {
                logger.error("Unexpected error in EventPublisher thread", e);
                throw new RuntimeException(e);
            } catch (NamingException e) {
                logger.error("Unexpected error in EventPublisher thread", e);
                throw new RuntimeException(e);
            }

            while (true) {
                Event event;
                PublicationJob job;

                try {
                    job = queue.take();
                } catch (InterruptedException e) {
                    logger.error("Unexpected error in jobs queue", e);
                    continue;
                }

                if (limit == 0) {
                    logger.error("Message not send due to MAX MESSAGES limit reached");
                    continue;
                }

                event = job.getEvent();

                if (debug) {
                    sendFakeEvent(event);
                } else {
                    try {
                        sendEvent(event);
                    } catch (JMSException e) {
                        logger.error(
                                "Unexpected error sending event. Ignoring it.",
                                e);
                        continue;
                    } catch (NamingException e) {
                        logger.error("JNDI API lookup failed. Ignoring event.",
                                e);
                        continue;
                    }
                }

                try {
                    StructuredKnowledgeSource source = SourcesManager.INSTANCE
                            .getSource(event.getSourceURI());
                    Date date = Database.stringToDate(event.getEventDate());

                    source.setDate(date);
                    PreferencesManager.INSTANCE.updateSource(source);
                } catch (SourcesManagerError e) {
                    logger.error("Updating source", e);
                } catch (ParseException e) {
                    logger.error("Updating source", e);
                } catch (PreferencesError e) {
                    logger.error("Updating source", e);
                }
            }
        }

        public void enableDebugMode() {
            this.debug = true;
        }

        public void disableDebugMode() {
            this.debug = false;
        }

        public void enableExportToFiles(String eventsPath) {
            this.export = true;
            this.eventsPath = eventsPath;
        }

        public void disableExportToFiles() {
            this.export = false;
        }

        private void setUp() throws JMSException, NamingException {
            TopicConnectionFactory topicConnectionFactory;

            if (debug) {
                return;
            }

            Properties env = new Properties();
            env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            env.setProperty(Context.PROVIDER_URL, url);
            env.setProperty("topic." + EventFactory.EVENT_SCM_NEW,
                    EventFactory.EVENT_SCM_NEW);
            env.setProperty("topic." + EventFactory.EVENT_ITS_NEW,
                    EventFactory.EVENT_ITS_NEW);
            env.setProperty("topic." + EventFactory.EVENT_ITS_UPDATE,
                    EventFactory.EVENT_ITS_UPDATE);

            jndiContext = new InitialContext(env);

            topicConnectionFactory = (TopicConnectionFactory) jndiContext
                    .lookup("TopicConnectionFactory");
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicSession = topicConnection.createTopicSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            topicCommitNew = (Topic) jndiContext
                    .lookup(EventFactory.EVENT_SCM_NEW);
            topicIssueNew = (Topic) jndiContext
                    .lookup(EventFactory.EVENT_ITS_NEW);
            topicIssueUpdate = (Topic) jndiContext
                    .lookup(EventFactory.EVENT_ITS_UPDATE);
        }

        private TopicPublisher createPublisher(String topicName)
                throws JMSException, NamingException {
            Topic topic;

            if (topicName.equals(EventFactory.EVENT_SCM_NEW)) {
                topic = topicCommitNew;
            } else if (topicName.equals(EventFactory.EVENT_ITS_NEW)) {
                topic = topicIssueNew;
            } else if (topicName.equals(EventFactory.EVENT_ITS_UPDATE)) {
                topic = topicIssueUpdate;
            } else {
                topic = null; // FIXME: raise exception
            }

            return topicSession.createPublisher(topic);
        }

        private void sendEvent(Event event) throws JMSException,
                NamingException {
            TopicPublisher topicPublisher;
            TextMessage message;
            String topicName;
            String content;

            ++count;
            topicName = event.getEventName();
            content = event.toMessage(Integer.toString(count));

            topicPublisher = createPublisher(topicName);

            message = topicSession.createTextMessage();
            message.setText(content);
            topicPublisher.publish(message);

            logger.info(topicName + " event sent. SeqNum: " + count);

            if (limit != NO_LIMIT) {
                --limit;
            }

            if (export) {
                writeToFile(event.getEventDate(), event.getEventID(),
                        event.getEventName(), content);
            }
        }

        private void sendFakeEvent(Event event) {
            String content;

            ++count;
            content = event.toMessage(Integer.toString(count));

            if (limit != NO_LIMIT) {
                --limit;
            }

            if (export) {
                writeToFile(event.getEventDate(), event.getEventID(),
                        event.getEventName(), content);
            }
        }

        private void writeToFile(String date, String id, String name,
                String content) {
            String filename = eventsPath + date + "_" + id + "_" + name;

            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(
                        filename), "UTF-8");
                BufferedWriter fout = new BufferedWriter(writer);
                fout.write(content);
                fout.close();
            } catch (IOException e) {
                logger.error("Error writing event to file", e);
            }
        }

    }

}

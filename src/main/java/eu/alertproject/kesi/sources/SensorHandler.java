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

package eu.alertproject.kesi.sources;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.PreferencesManager;
import eu.alertproject.kesi.extractors.ExtractionManager;
import eu.alertproject.kesi.model.StructuredKnowledgeSource;

public class SensorHandler extends Thread {
    /* Mail prorperties keys */
    private static final String MAIL_PROTOCOL_KEY = "mail.store.protocol";
    private static final String IMAP_CONN_TIMEOUT_KEY = "mail.imap.connectiontimeout";
    private static final String IMAP_TIMEOUT_KEY = "mail.imap.timeout";

    /*
     * Patterns for checking whether an event is received from SCM or
     * ITS repositories TODO: add support for JIRA and GitHub
     */
    private static final String BUGZILLA_PATTERN = "\\[Bug ([0-9]+)\\] .+";
    private static final String SCM_PATTERN = "([0-9]+) (.+)";

    static Logger logger = Logger.getLogger(SensorHandler.class);

    public SensorHandler() {
    }

    @Override
    public void run() {
        Store store;

        try {
            store = connect();
        } catch (MessagingException e) {
            logger.error("Unable to connect with sensor's provider");
            throw new RuntimeException(e);
        }

        try {
            int wait;
            Folder folder;

            folder = store.getFolder(PreferencesManager.INSTANCE
                    .getSensorEmailFolder());
            folder.open(Folder.READ_WRITE);

            wait = PreferencesManager.INSTANCE.getSensorPollInterval();

            while (true) {
                String url;
                int nmsg;

                /*
                 * Due to a weird behavior we need to get first the
                 * total number of messages and after that, we can
                 * retrieve the real number of new or unread messages.
                 */
                folder.getMessageCount();
                nmsg = folder.getNewMessageCount()
                        + folder.getUnreadMessageCount();

                if (nmsg == 0) {
                    logger.info("No messages. Waiting for new messages.");
                    sleep(wait);
                    continue;
                }

                logger.info(nmsg + " new messages. Handling...");

                Message messages[] = folder.search(new FlagTerm(new Flags(
                        Flags.Flag.SEEN), false));

                for (Message message : messages) {
                    try {
                        url = parseURLFromMessage(message);
                        scheduleExtraction(url);
                    } catch (SensorHandlerError e) {
                        logger.error(e);
                        logger.error("Ignoring message.");
                        continue;
                    } catch (SourcesManagerError e) {
                        logger.error(e);
                        continue;
                    }

                    message.setFlag(Flags.Flag.SEEN, true);
                }

                sleep(wait);
            }
        } catch (MessagingException e) {
            logger.error("Error in sensor's provider connection");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error("Unexpected error in sensor thread");
            throw new RuntimeException(e);
        }
    }

    private Store connect() throws MessagingException {
        Properties props;
        String username;
        String password;
        String host;
        Session session;
        Store store;

        props = new Properties();
        props.put(MAIL_PROTOCOL_KEY,
                PreferencesManager.INSTANCE.getSensorProtocol());
        props.put(IMAP_CONN_TIMEOUT_KEY,
                PreferencesManager.INSTANCE.getSensorTimeout());
        props.put(IMAP_TIMEOUT_KEY,
                PreferencesManager.INSTANCE.getSensorTimeout());

        username = PreferencesManager.INSTANCE.getSensorUser();
        password = PreferencesManager.INSTANCE.getSensorPassword();
        host = PreferencesManager.INSTANCE.getSensorHost();

        session = Session.getDefaultInstance(props, null);
        store = session.getStore("imaps");
        store.connect(host, username, password);

        return store;
    }

    private String convertStreamToString(InputStream is) {
        /*
         * Method taken Pavel Repin's solution to from
         * http://stackoverflow
         * .com/questions/309424/read-convert-an-inputstream
         * -to-a-string
         */
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    private String parseURLFromMessage(Message message)
            throws SensorHandlerError {
        try {
            String subject;
            Pattern pattern;
            Matcher matcher;
            String url = null;

            subject = message.getSubject();
            pattern = Pattern.compile(BUGZILLA_PATTERN);
            matcher = pattern.matcher(subject);

            if (matcher.find()) {
                /* Extracts data from bugzilla mail */
                try {
                    Object content;
                    String body = new String();

                    content = message.getContent();

                    if (content instanceof String) {
                        body = (String) content;
                    } else if (content instanceof Multipart) {
                        Multipart mp = (Multipart) content;

                        for (int i = 0; i < mp.getCount(); i++) {
                            BodyPart bp = mp.getBodyPart(i);
                            body = body.concat(convertStreamToString(bp
                                    .getInputStream()));
                        }
                    } else {
                        String msg = "Error in body content for message "
                                + message.getMessageNumber();
                        throw new SensorHandlerError(msg);
                    }

                    url = body.substring(0, body.indexOf('\n'));
                } catch (MalformedURLException e) {
                    String msg = "Error parsing the URL. " + e.getMessage();
                    logger.error(msg, e);
                    throw new SensorHandlerError(msg);
                } catch (IOException e) {
                    String msg = "Error parsing the message. " + e.getMessage();
                    logger.error(msg, e);
                    throw new SensorHandlerError(msg);
                }
            } else {
                pattern = Pattern.compile(SCM_PATTERN);
                matcher = pattern.matcher(subject);

                if (matcher.find()) {
                    /* Extracts data from SCMs */
                    url = matcher.group(2);
                }
            }

            if (url != null) {
                logger.info("Message " + message.getMessageNumber()
                        + " matchs.");
                return url;
            } else {
                String msg = "Error guessing the type of repository for message "
                        + message.getMessageNumber()
                        + ".Subject didn't match any pattern.";
                throw new SensorHandlerError(msg);
            }
        } catch (MessagingException e) {
            String msg = "Error parsing message. " + e.getMessage();
            throw new SensorHandlerError(msg);
        }
    }

    private void scheduleExtraction(String url) throws SourcesManagerError {
        StructuredKnowledgeSource source = SourcesManager.INSTANCE
                .getSource(url);
        ExtractionManager.INSTANCE.extract(source);
        logger.info("New source scheduled");
    }

}
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

package eu.alertproject.kesi;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import eu.alertproject.kesi.events.EventGenerator;
import eu.alertproject.kesi.extractors.ExtractionManager;
import eu.alertproject.kesi.publisher.EventPublisher;
import eu.alertproject.kesi.sources.SensorHandler;
import eu.alertproject.kesi.sources.SourcesManager;
import eu.alertproject.kesi.sources.SourcesManagerError;

public class KESI {
    /* Logger properties names */
    private static final String LOGGER_ROOT = "log4j.rootLogger";
    private static final String LOGGER_APPENDER_TYPE = "log4j.appender.KESIAppender";
    private static final String LOGGER_APPENDER_FILE = LOGGER_APPENDER_TYPE
            + ".File";
    private static final String LOGGER_APPENDER_LAYOUT = LOGGER_APPENDER_TYPE
            + ".layout";
    private static final String LOGGER_APPENDER_TEMPLATE = LOGGER_APPENDER_LAYOUT
            + ".ConversionPattern";

    /* Logger properties values */
    private static final String LOGGER_APPENDER_VAL = "KESIAppender";
    private static final String LOGGER_APPENDER_TYPE_VAL = "org.apache.log4j.FileAppender";
    private static final String LOGGER_APPENDER_LAYOUT_VAL = "org.apache.log4j.PatternLayout";
    private static final String LOGGER_APPENDER_TEMPLATE_VAL = "%d [%t] %-5p %c - %m%n";

    /* Logger for KESI class */
    private static Logger logger = Logger.getLogger(KESI.class);

    public void run() {
        SensorHandler handler;

        logger.info("Starting KESI component");

        EventPublisher.INSTANCE.start();
        EventGenerator.INSTANCE.start();
        ExtractionManager.INSTANCE.start();

        try {
            SourcesManager.INSTANCE.setUp();
        } catch (SourcesManagerError e) {
            logger.error("Error starting Sources Manager", e);
            throw new RuntimeException(e);
        }

        handler = new SensorHandler();
        handler.start();

        System.out.println("Running KESI component...");
        logger.info("Subcomponents started. Running KESI component.");

        try {
            handler.join();

            ExtractionManager.INSTANCE.stop();
            EventGenerator.INSTANCE.stop();
            EventPublisher.INSTANCE.stop();

            /* Something went really wrong if all threads are dead */
            logger.error("Unexpected KESI behaviour");
            throw new RuntimeException("Unexpected KESI behaviour");
        } catch (InterruptedException e) {
            logger.error("Unexpected interruption", e);
            throw new RuntimeException(e);
        }
    }

    private static void setLogger() {
        Properties props;
        String filepath;
        String level;

        filepath = PreferencesManager.INSTANCE.getLoggerFilePath();
        level = null;

        try {
            level = PreferencesManager.INSTANCE.getLoggerLevel();
        } catch (PreferencesError e) {
            System.err.printf(e.getMessage());
            System.exit(-1);
        }

        props = new Properties();
        props.setProperty(LOGGER_ROOT, level + ", " + LOGGER_APPENDER_VAL);
        props.setProperty(LOGGER_APPENDER_TYPE, LOGGER_APPENDER_TYPE_VAL);
        props.setProperty(LOGGER_APPENDER_FILE, filepath);
        props.setProperty(LOGGER_APPENDER_LAYOUT, LOGGER_APPENDER_LAYOUT_VAL);
        props.setProperty(LOGGER_APPENDER_TEMPLATE,
                LOGGER_APPENDER_TEMPLATE_VAL);
        PropertyConfigurator.configure(props);
    }

    public static void main(String[] args) {
        KESI kesi;

        if (args.length == 0) {
            PreferencesManager.INSTANCE.setUpPreferences();
        } else if (args.length == 1) {
            try {
                PreferencesManager.INSTANCE.setUpPreferences(args[0]);
            } catch (PreferencesError e) {
                System.err.printf(e.getMessage());
                System.exit(-1);
            }
        } else {
            System.err.println("Invalid number of arguments.");
            System.exit(-1);
        }

        setLogger();

        kesi = new KESI();
        kesi.run();
    }

}

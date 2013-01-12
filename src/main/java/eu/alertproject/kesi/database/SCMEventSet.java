/*
 * Copyright (C) 2012-2013 GSyC/LibreSoft, Universidad Rey Juan Carlos
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import eu.alertproject.kesi.events.Event;
import eu.alertproject.kesi.events.EventSummary;
import eu.alertproject.kesi.model.Commit;

public class SCMEventSet implements EventSet {

    private static Logger logger = Logger.getLogger(SCMEventSet.class);

    private final SCMRetrieval conn;
    private final Timestamp lastSent;
    private final ArrayList<EventSummary> events;

    public SCMEventSet(SCMRetrieval connection, String url, Timestamp lastSent)
            throws DatabaseExtractionError {
        this.conn = connection;
        this.lastSent = lastSent;
        this.events = this.conn.getEventsSummary(url, lastSent);
    }

    @Override
    public boolean hasNext() {
        return !events.isEmpty();
    }

    @Override
    public Event next() {
        Commit commit;

        try {
            EventSummary summary = events.remove(0);
            commit = conn.getCommitFromSummary(summary);
        } catch (DatabaseExtractionError e) {
            String msg = "Error getting issues. " + e.getMessage();
            logger.error(msg, e);
            throw new NoSuchElementException(msg);
        }

        try {
            return commit.toEvent();
        } catch (JAXBException e) {
            String msg = "Error marshaling commit " + commit.getRevisionTag()
                    + " to XML.";
            logger.error(msg, e);
            throw new NoSuchElementException(msg);
        }

    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}

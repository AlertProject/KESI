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

public class EventSummary {
    public static final int ISSUE_NEW = 0;
    public static final int ISSUE_CHANGE = 1;
    public static final int ISSUE_COMMENT = 2;
    public static final int COMMIT_NEW = 3;

    private int eventID;
    private int type;
    private int eventKey;
    private Timestamp date;

    public EventSummary(int eventID, int type, Timestamp date) {
        this.eventID = eventID;
        this.type = type;
        this.date = date;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public int getEventKey() {
        return eventKey;
    }

    public void setEventKey(int eventKey) {
        this.eventKey = eventKey;
    }

}

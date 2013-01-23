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
 *          Luis Cañas Díaz <lcanas@bitergia.com>
 *
 */

package eu.alertproject.kesi.events;

import java.util.UUID;

public abstract class Event {
    private static final String EVENT_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\""
            + "            xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\""
            + "            xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
            + "  <s:Header></s:Header>"
            + "  <s:Body>"
            + "    <wsnt:Notify>"
            + "      <wsnt:NotificationMessage>"
            + "        <wsnt:Topic></wsnt:Topic>"
            + "        <wsnt:ProducerReference>"
            + "          <wsa:Address>http://www.alert-project.eu/kesi</wsa:Address>"
            + "        </wsnt:ProducerReference>"
            + "        <wsnt:Message>"
            + "          <ns1:event xmlns:ns1=\"http://www.alert-project.eu/\""
            + "                     xmlns:o=\"http://www.alert-project.eu/ontoevents-mdservice\""
            + "                     xmlns:r=\"http://www.alert-project.eu/rawevents-forum\""
            + "                     xmlns:r1=\"http://www.alert-project.eu/rawevents-mailinglist\""
            + "                     xmlns:r2=\"http://www.alert-project.eu/rawevents-wiki\""
            + "                     xmlns:s=\"http://www.alert-project.eu/strevents-kesi\""
            + "                     xmlns:s1=\"http://www.alert-project.eu/strevents-keui\""
            + "                     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "                     xsi:schemaLocation=\"http://www.alert-project.eu/alert-root.xsd\">"
            + "            <ns1:head>"
            + "              <ns1:sender>KESI</ns1:sender>"
            + "              <ns1:timestamp>%s</ns1:timestamp>"
            + "              <ns1:sequencenumber>%s</ns1:sequencenumber>"
            + "            </ns1:head>"
            + "            <ns1:payload>"
            + "              <ns1:meta>"
            + "                <ns1:startTime>10010</ns1:startTime><ns1:endTime>10010</ns1:endTime>"
            + "                <ns1:eventName>%s</ns1:eventName>"
            + "                <ns1:eventId>%s</ns1:eventId>"
            + "                <ns1:eventType>request</ns1:eventType>"
            + "              </ns1:meta>"
            + "              <ns1:eventData>%s</ns1:eventData>"
            + "            </ns1:payload>"
            + "          </ns1:event>"
            + "        </wsnt:Message>"
            + "      </wsnt:NotificationMessage>"
            + "    </wsnt:Notify>" + "  </s:Body>" + "</s:Envelope>";

    protected String eventID;
    protected String eventName;
    protected String eventDate;
    protected String content;
    protected String sourceURI;

    public Event() {
        this.eventID = generateEventID();
    }

    public String getEventID() {
        return eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public String toMessage(String sequenceNumber) {
        return String.format(EVENT_TEMPLATE, System.currentTimeMillis(),
                sequenceNumber, this.eventName, this.eventID, this.content);
    }

    public static String generateEventID() {
        return UUID.randomUUID().toString();
    }

}

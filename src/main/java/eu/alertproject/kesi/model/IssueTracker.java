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

package eu.alertproject.kesi.model;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;

public class IssueTracker extends StructuredKnowledgeSource {

    private URI uri;
    private String type;

    public IssueTracker() {
    }

    public IssueTracker(String id, URI uri, String type) {
        this.setId(id);
        this.uri = uri;
        this.type = type;
    }

    @Override
    @XmlElement(name = "s:issueTrackerType")
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String t) {
        this.type = t;
    }

    @Override
    @XmlElement(name = "s:issueTrackerURL")
    public URI getURI() {
        return this.uri;
    }

    @Override
    public void setURI(URI uri) {
        this.uri = uri;
    }

    public static IssueTracker sourceToIssueTracker(
            StructuredKnowledgeSource source) {
        IssueTracker tracker;

        tracker = new IssueTracker(source.getId(), source.getURI(),
                source.getType());
        tracker.setUser(source.getUser());
        tracker.setPassword(source.getPassword());
        tracker.setSetup(source.getSetup());
        tracker.setDate(source.getDate());

        return tracker;
    }

}

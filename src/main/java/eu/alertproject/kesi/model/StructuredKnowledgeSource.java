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
import java.util.Date;

import javax.xml.bind.annotation.XmlTransient;

import eu.alertproject.kesi.jobs.Job;

@XmlTransient
public class StructuredKnowledgeSource extends Entity {
    private String id;
    private URI uri;
    private String type;
    private String user;
    private String password;
    private int setup;
    private Date date;

    public StructuredKnowledgeSource() {
    }

    public StructuredKnowledgeSource(String id, URI uri, String type) {
        this.id = id;
        this.uri = uri;
        this.type = type;
        this.user = null;
        this.password = null;
        this.setup = Job.EXTRACT_AND_PUBLISH;
        this.date = null;
    }

    @XmlTransient
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URI getURI() {
        return uri;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlTransient
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @XmlTransient
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlTransient
    public int getSetup() {
        return setup;
    }

    public void setSetup(int setup) {
        this.setup = setup;
    }

    public synchronized Date getDate() {
        return date;
    }

    public synchronized void setDate(Date date) {
        this.date = date;
    }

}

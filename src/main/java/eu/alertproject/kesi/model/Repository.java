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

public class Repository extends StructuredKnowledgeSource {

    private URI uri;
    private String type;

    public Repository() {
    }

    public Repository(String id, URI uri, String type) {
        this.setId(id);
        this.uri = uri;
        this.type = type;
    }

    @Override
    @XmlElement(name = "s:commitRepositoryURI")
    public URI getURI() {
        return this.uri;
    }

    @Override
    @XmlElement(name = "s:commitRepositoryType")
    public String getType() {
        return this.type;
    }

    public static Repository sourceToRepository(StructuredKnowledgeSource source) {
        Repository repo;

        repo = new Repository(source.getId(), source.getURI(), source.getType());
        repo.setUser(source.getUser());
        repo.setPassword(source.getPassword());
        repo.setSetup(source.getSetup());
        repo.setDate(source.getDate());

        return repo;
    }

}

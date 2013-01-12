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

import javax.xml.bind.annotation.XmlElement;

public class Person extends Entity {
    private String name;
    private String email;
    private String userID;

    public Person() {
    }

    public Person(String name, String email, String userID) {
        this.name = name;
        this.email = email;
        this.userID = userID;
    }

    @XmlElement(name = "s:name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "s:email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @XmlElement(name = "s:id")
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}

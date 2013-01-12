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

public class Activity extends Entity {
    private String who;
    private String when;
    private String what;
    private String oldValue;
    private String newValue;

    public Activity() {
    }

    public Activity(String who, String when, String what, String oldValue,
            String newValue) {
        this.who = who;
        this.when = when;
        this.what = what;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @XmlElement(name = "s:activityWho")
    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    @XmlElement(name = "s:activityWhen")
    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    @XmlElement(name = "s:activityWhat")
    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    @XmlElement(name = "s:activityRemoved")
    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    @XmlElement(name = "s:activityAdded")
    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

}

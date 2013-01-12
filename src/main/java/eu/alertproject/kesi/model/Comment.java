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

public class Comment extends Entity {
    private String comment;
    private Person commentor; /* poster of the comment */
    private String date;
    private String id;

    public Comment() {
    }

    public Comment(String comment, Person commentor, String date) {
        this.comment = comment;
        this.commentor = commentor;
        this.date = date;
    }

    @XmlElement(name = "s:commentText")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @XmlElement(name = "s:commentPerson")
    public Person getCommentor() {
        return commentor;
    }

    public void setCommentor(Person commentor) {
        this.commentor = commentor;
    }

    @XmlElement(name = "s:commentDate")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @XmlElement(name = "s:commentID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}

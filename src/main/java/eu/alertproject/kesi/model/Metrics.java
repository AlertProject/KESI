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

public class Metrics extends Entity {
    private String lang;
    private int sloc;
    private int loc;
    private int ncomment;
    private int lcomment;
    private int lblank;

    public Metrics() {
        lang = null;
        sloc = -1;
        loc = -1;
        ncomment = -1;
        lcomment = -1;
        lblank = -1;
    }

    @XmlElement
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @XmlElement
    public int getSLOC() {
        return sloc;
    }

    public void setSLOC(int sloc) {
        this.sloc = sloc;
    }

    @XmlElement
    public int getLOC() {
        return loc;
    }

    public void setLOC(int loc) {
        this.loc = loc;
    }

    @XmlElement
    public int getNumOfComments() {
        return ncomment;
    }

    public void setNumOfComments(int ncomment) {
        this.ncomment = ncomment;
    }

    @XmlElement
    public int getLinesOfComments() {
        return lcomment;
    }

    public void setLinesOfComments(int lcomment) {
        this.lcomment = lcomment;
    }

    @XmlElement
    public int getBlankLines() {
        return lblank;
    }

    public void setBlankLines(int lblank) {
        this.lblank = lblank;
    }

}

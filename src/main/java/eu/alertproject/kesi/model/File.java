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
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class File extends Entity {

    private String fileID;
    private URI filePath;
    private Metrics metrics;
    private ArrayList<Module> modules;
    private String branch;
    private Action action;
    private String simpleaction;

    public File() {
    }

    public File(String fileID) {
        this.fileID = fileID;
        this.metrics = null;
        this.modules = new ArrayList<Module>();
    }

    @XmlElement(name = "s:fileName")
    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    @XmlElement()
    public URI getFilePath() {
        return filePath;
    }

    public void setFilePath(URI filePath) {
        this.filePath = filePath;
    }

    @XmlElement()
    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    @XmlElement(name = "s:fileModules")
    public ArrayList<Module> getModules() {
        return modules;
    }

    public void addModule(Module module) {
        modules.add(module);
    }

    @XmlElement(name = "s:fileBranch")
    public String getBranch() {
        return this.branch;
    }

    public void setBranch(String b) {
        this.branch = b;
    }

    @XmlElement(name = "s:fileAction")
    public Action getAction() {
        return this.action;
    }

    public void setAction(Action act) {
        this.action = act;
    }

    @XmlElement(name = "s:fileAction")
    public String getSimpleAction() {
        return this.simpleaction;
    }

    public void setSimpleAction(String sa) {
        this.simpleaction = sa;
    }

}

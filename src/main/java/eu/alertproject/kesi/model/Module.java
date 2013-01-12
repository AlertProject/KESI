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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class Module extends Entity {
    private String name;
    private int startLine;
    private int endLine;
    private ArrayList<Function> functions;

    public Module() {
    }

    public Module(String name, int startLine, int endLine) {
        this.name = name;
        this.startLine = startLine;
        this.endLine = endLine;
        this.functions = new ArrayList<Function>();
    }

    @XmlElement(name = "s:moduleName")
    public String getName() {
        return name;
    }

    @XmlElement(name = "s:moduleStartLine")
    public int getStartLine() {
        return startLine;
    }

    @XmlElement(name = "s:moduleEndLine")
    public int getEndLine() {
        return endLine;
    }

    @XmlElement(name = "s:moduleMethods")
    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

}

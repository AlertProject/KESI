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

package eu.alertproject.kesi.model;

import javax.xml.bind.annotation.XmlElement;

public class Product extends Entity {
    private String productid;
    private String componentid;
    private String version;

    public Product() {
    }

    public Product(String productid, String componentid, String version) {
        this.productid = productid;
        this.componentid = componentid;
        this.version = version;
    }

    @XmlElement(name = "s:productId")
    public String getId() {
        return productid;
    }

    public void setId(String id) {
        this.productid = id;
    }

    @XmlElement(name = "s:productComponentId")
    public String getComponentId() {
        return componentid;
    }

    public void setComponentId(String cid) {
        this.componentid = cid;
    }

    @XmlElement(name = "s:productVersion")
    public String getVersion() {
        return version;
    }

    public void setVersion(String ver) {
        this.version = ver;
    }

}

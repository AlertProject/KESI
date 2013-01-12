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
 *
 */

package eu.alertproject.kesi.jobs;

import eu.alertproject.kesi.model.StructuredKnowledgeSource;

public class ExtractionJob implements Job {
    private static int id;
    private final StructuredKnowledgeSource source;

    public ExtractionJob(StructuredKnowledgeSource source) {
        ++id;
        this.source = source;
    }

    @Override
    public String getID() {
        return String.valueOf(id);
    }

    public StructuredKnowledgeSource getSource() {
        return source;
    }

}

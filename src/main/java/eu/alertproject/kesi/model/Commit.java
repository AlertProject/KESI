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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import eu.alertproject.kesi.events.Event;
import eu.alertproject.kesi.events.EventFactory;

import java.net.URI;

@XmlSeeAlso({ Action.class, Add.class, Copy.class, Delete.class, Modify.class,
        Move.class, Rename.class, Replace.class, Module.class, Function.class })
@XmlRootElement(name = "s:commit")
public class Commit extends Entity {
    private Repository repository;
    private String commitMessage;
    private String commitDate;
    private String revisionTag;
    private Person author;
    private Person committer;
    // private ArrayList<Action> actions;
    private ArrayList<File> files;
    private URI repositoryURI;
    private int commitID;

    public Commit() {
    }

    public Commit(String commitMessage, String commitDate, String revisionTag,
            Person author, Person committer, int commitID) {
        this.commitMessage = commitMessage;
        this.commitDate = commitDate;
        this.revisionTag = revisionTag;
        this.author = author;
        this.committer = committer;
        this.repository = null;
        this.commitID = commitID;
        this.files = new ArrayList<File>();
    }

    @XmlElement(name = "s:commitRepository")
    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    @XmlElement(name = "s:commitRepositoryUri")
    public URI getRepositoryURI() {
        return this.repositoryURI;
    }

    public void setRepositoryURI(URI repositoryURI) {
        this.repositoryURI = repositoryURI;
    }

    @XmlElement(name = "s:commitMessageLog")
    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    @XmlElement(name = "s:commitDate")
    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    @XmlElement(name = "s:commitRevisionTag")
    public String getRevisionTag() {
        return revisionTag;
    }

    public void setRevisionTag(String revisionTag) {
        this.revisionTag = revisionTag;
    }

    @XmlElement(name = "s:commitAuthor")
    public Person getAuthor() {
        return author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }

    @XmlElement(name = "s:commitCommitter")
    public Person getCommitter() {
        return committer;
    }

    public void setCommitter(Person committer) {
        this.committer = committer;
    }

    @XmlElement(name = "s:commitFile")
    public ArrayList<File> getFiles() {
        return files;
    }

    public void addFile(File f) {
        files.add(f);
    }

    public String toXML() throws JAXBException {
        String xml;
        JAXBContext context = JAXBContext.newInstance(Commit.class);
        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(this, stream);
        xml = stream.toString();
        xml = xml
                .replace(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>",
                        "");

        return xml;
    }

    public Event toEvent() throws JAXBException {
        EventFactory eventFactory = new EventFactory();
        Event event = eventFactory.createCommitNewEvent();
        event.setEventDate(commitDate);
        event.setContent(toXML());
        return event;
    }

}

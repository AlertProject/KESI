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
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import eu.alertproject.kesi.events.Event;
import eu.alertproject.kesi.events.EventFactory;

@XmlSeeAlso({ StructuredKnowledgeSource.class, IssueTracker.class })
@XmlRootElement(name = "s:issue")
public class Issue extends Entity {
    /* Severity values */
    public static final String BLOCKER = "Blocker";
    public static final String CRITICAL = "Critical";
    public static final String MAJOR = "Major";
    public static final String NORMAL = "Normal";
    public static final String MINOR = "Minor";
    public static final String TRIVIAL = "Trivial";
    public static final String FEATURE = "Feature";

    /* State values */
    public static final String ASSIGNED = "Assigned";
    public static final String OPEN = "Open";
    public static final String RESOLVED = "Resolved";
    public static final String VERIFIED = "Verified";
    public static final String CLOSED = "Closed";

    /* Resolution values */
    public static final String DUPLICATED = "Duplicated";
    public static final String FIXED = "Fixed";
    public static final String INVALID = "Invalid";
    public static final String LATER = "Later";
    public static final String REMIND = "Remind";
    public static final String THIRD_PARTY = "ThirdParty";
    public static final String WONT_FIX = "WontFix";
    public static final String WORKS_FOR_ME = "WorksForMe";

    /* Priority values */
    public static final String LOWEST = "1";
    public static final String LOW = "2";
    public static final String MEDIUM = "3";
    public static final String HIGH = "4";
    public static final String HIGHEST = "5";

    /* Common values */
    public static final String NONE = "None";
    public static final String UNKNOWN = "Unknown";

    private IssueTracker issueTracker;
    private String issueID;
    private URI issueURL;
    private String summary;
    private String description;
    private Date dateOpened;
    private Date lastModified;
    private Person assignedTo;
    private Person reporter;
    private String severity;
    private String state;
    private String resolution;
    private String priority;
    private ArrayList<Comment> comments;
    private ArrayList<Attachment> attachments;
    private Product product;
    private ComputerSystem computerSystem;
    private ArrayList<Activity> activities;
    private ArrayList<Person> ccPeople;
    private Milestone milestone;
    private String eventType;
    private String date;

    public Issue() {
    }

    public Issue(String issueID) {
        this.issueID = issueID;
        this.comments = new ArrayList<Comment>();
        this.attachments = new ArrayList<Attachment>();
        this.activities = new ArrayList<Activity>();
        this.ccPeople = new ArrayList<Person>();
    }

    @XmlElement(name = "s:issueTracker")
    public IssueTracker getIssueTracker() {
        return issueTracker;
    }

    public void setIssueTracker(IssueTracker issueTracker) {
        this.issueTracker = issueTracker;
    }

    @XmlElement(name = "s:issueId")
    public String getIssueID() {
        return issueID;
    }

    public void setIssueID(String issueID) {
        this.issueID = issueID;
    }

    @XmlElement(name = "s:issueUrl")
    public URI getIssueURL() {
        return issueURL;
    }

    public void setIssueURL(URI issueURL) {
        this.issueURL = issueURL;
    }

    @XmlElement(name = "s:issueSummary")
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @XmlElement(name = "s:issueDescription")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "s:issueDateOpened")
    public Date getDateOpened() {
        return dateOpened;
    }

    public void setDateOpened(Date dateOpened) {
        this.dateOpened = dateOpened;
    }

    @XmlElement(name = "s:issueLastModified")
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @XmlElement(name = "s:issueAuthor")
    public Person getReporter() {
        return reporter;
    }

    public void setReporter(Person reporter) {
        this.reporter = reporter;
    }

    @XmlElement(name = "s:issueCCPerson")
    public ArrayList<Person> getCCPeople() {
        return ccPeople;
    }

    public void addCCPerson(Person p) {
        ccPeople.add(p);
    }

    public boolean isEnhancement() {
        return severity.equals(FEATURE);
    }

    public boolean isBug() {
        return !isEnhancement();
    }

    @XmlElement(name = "s:issueAssignedTo")
    public Person getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Person assignedTo) {
        this.assignedTo = assignedTo;
    }

    @XmlElement(name = "s:issueSeverity")
    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @XmlElement(name = "s:issueStatus")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @XmlElement(name = "s:issueResolution")
    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @XmlElement(name = "s:issueComment")
    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    @XmlElement(name = "s:issueAttachment")
    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }

    @XmlElement(name = "s:issuePriority")
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @XmlElement(name = "s:issueProduct")
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @XmlElement(name = "s:issueComputerSystem")
    public ComputerSystem getComputerSystem() {
        return computerSystem;
    }

    public void setComputerSystem(ComputerSystem cp) {
        this.computerSystem = cp;
    }

    @XmlElement(name = "s:issueActivity")
    public ArrayList<Activity> getActivity() {
        return activities;
    }

    public void addActivity(Activity act) {
        activities.add(act);
    }

    @XmlElement(name = "s:issueMilestone")
    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public void setIssueNew(String date) {
        this.eventType = EventFactory.EVENT_ITS_NEW;
        this.date = date;
    }

    public void setIssueUpdate(String date) {
        this.eventType = EventFactory.EVENT_ITS_UPDATE;
        this.date = date;
    }

    public String getEventType() {
        return eventType;
    }

    public String toXML() throws JAXBException {
        String xml;
        JAXBContext context = JAXBContext.newInstance(Issue.class);
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
        Event event;
        EventFactory eventFactory;

        eventFactory = new EventFactory();

        if (eventType.equals(EventFactory.EVENT_ITS_NEW)) {
            event = eventFactory.createIssueNewEvent();
        } else if (eventType.equals(EventFactory.EVENT_ITS_UPDATE)) {
            event = eventFactory.createIssueUpdateEvent();
        } else {
            // FIXME:
            return null;
        }

        event.setContent(toXML());
        event.setEventDate(date);

        return event;
    }

}

/*
 * Copyright 2012 Cedric Dandoy
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.bug4j.server.jsp;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.bug4j.gwt.user.client.data.Session;
import org.bug4j.server.store.HitsCallback;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.UserEx;
import org.bug4j.server.store.jdbc.BugCallback;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.bug4j.server.jsp.ImportExportConstants.*;

public class Exporter {

    private final DateFormat _dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
    private final String[][] NL = {
            {"\r", "\r"},
            {"\n", "\n"},
            {"\t", "\t"},
    };
    final AggregateTranslator _unicodeEscaper = new AggregateTranslator(
            new LookupTranslator(NL),
            UnicodeEscaper.outsideOf(32, 0x7f)
    );
    private final Store _store;

    private interface Streamer {
        void stream(XMLStreamWriter xmlStreamWriter) throws XMLStreamException;
    }

    public Exporter(Store store) {
        _store = store;
    }

    private void writeString(XMLStreamWriter xmlStreamWriter, String qName, String value) throws XMLStreamException {
        if (value != null) {
            final String escapedValue = StringEscapeUtils.escapeXml(value);
            xmlStreamWriter.writeAttribute(qName, escapedValue);
        }
    }

    private static void writeLong(XMLStreamWriter xmlStreamWriter, String qName, Long value) throws XMLStreamException {
        if (value != null) {
            xmlStreamWriter.writeAttribute(qName, value.toString());
        }
    }

    private static void writeInteger(XMLStreamWriter xmlStreamWriter, String qName, Integer value) throws XMLStreamException {
        if (value != null) {
            xmlStreamWriter.writeAttribute(qName, value.toString());
        }
    }

    private static void writeBoolean(XMLStreamWriter xmlStreamWriter, String qName, Boolean value) throws XMLStreamException {
        if (Boolean.TRUE.equals(value)) {
            xmlStreamWriter.writeAttribute(qName, "true");
        }
    }

    private void writeDate(XMLStreamWriter xmlStreamWriter, String qName, Long value) throws XMLStreamException {
        if (value != null) {
            xmlStreamWriter.writeAttribute(qName, _dateFormat.format(new Date(value)));
        }
    }

    public void exportAll(OutputStream outputStream) throws XMLStreamException, IOException {
        withXMLStreamWriter(outputStream, new Streamer() {
            @Override
            public void stream(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
                exportAll(xmlStreamWriter);
            }
        });
    }


    public void exportApplicationBugs(OutputStream outputStream, final String app) throws XMLStreamException, IOException {
        withXMLStreamWriter(outputStream, new Streamer() {
            @Override
            public void stream(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
                exportApplicationBugs(xmlStreamWriter, app);
            }
        });
    }

    private void withXMLStreamWriter(OutputStream outputStream, Streamer streamer) throws XMLStreamException, IOException {
        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        try {
            final XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);
            try {
                final IndentingXMLStreamWriter indentingXMLStreamWriter = new IndentingXMLStreamWriter(xmlStreamWriter);
                try {
                    streamer.stream(indentingXMLStreamWriter);
                } finally {
                    indentingXMLStreamWriter.close();
                }
            } finally {
                xmlStreamWriter.close();
            }
        } finally {
            bufferedOutputStream.close();
        }
    }

    private void exportAll(final XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("bug4j");

        final List<UserEx> users = _store.getUserExes();
        exportUsers(xmlStreamWriter, users);

        final List<String> applications = _store.getApplications();
        exportApplications(xmlStreamWriter, applications);

        xmlStreamWriter.writeEndElement();
    }

    private void exportApplications(XMLStreamWriter xmlStreamWriter, List<String> applications) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("apps");
        for (String application : applications) {
            exportApplication(xmlStreamWriter, application);
        }
        xmlStreamWriter.writeEndElement();
    }

    private void exportApplication(XMLStreamWriter xmlStreamWriter, String application) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("app");
        xmlStreamWriter.writeAttribute("name", application);
        exportPackages(xmlStreamWriter, application);
        exportSessions(xmlStreamWriter, application);
        exportBugs(xmlStreamWriter, application);
        xmlStreamWriter.writeEndElement();
    }

    private void exportSessions(XMLStreamWriter xmlStreamWriter, String application) throws XMLStreamException {
        final List<Session> sessions = _store.getSessions(application);
        if (!sessions.isEmpty()) {
            xmlStreamWriter.writeStartElement("sessions");
            for (Session session : sessions) {
                xmlStreamWriter.writeStartElement("session");

                final long sessionId = session.getSessionId();
                final String version = session.getVersion();
                final long firstHit = session.getFirstHit();
                final String hostName = session.getHostName();
                final Long dateBuilt = session.getDateBuilt();
                final boolean devBuild = session.isDevBuild();
                final Integer buildNumber = session.getBuildNumber();

                writeLong(xmlStreamWriter, NAME_SESSION_ID, sessionId);
                writeString(xmlStreamWriter, NAME_SESSION_APP_VER, version);
                writeDate(xmlStreamWriter, NAME_SESSION_FIRST_HIT, firstHit);
                writeString(xmlStreamWriter, NAME_SESSION_HOST_NAME, hostName);
                writeDate(xmlStreamWriter, NAME_BUILD_DATE, dateBuilt);
                writeBoolean(xmlStreamWriter, NAME_DEV_BUILD, devBuild);
                writeInteger(xmlStreamWriter, NAME_BUILD_NUMBER, buildNumber);

                xmlStreamWriter.writeEndElement();
            }
            xmlStreamWriter.writeEndElement();
        }
    }

    private void exportPackages(XMLStreamWriter xmlStreamWriter, String application) throws XMLStreamException {
        final List<String> packages = _store.getPackages(application);
        if (!packages.isEmpty()) {
            xmlStreamWriter.writeStartElement("packages");
            for (String pkg : packages) {
                xmlStreamWriter.writeStartElement("package");
                xmlStreamWriter.writeAttribute("name", _unicodeEscaper.translate(pkg));
                xmlStreamWriter.writeEndElement();
            }
            xmlStreamWriter.writeEndElement();
        }
    }

    private void exportUsers(XMLStreamWriter xmlStreamWriter, List<UserEx> users) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("users");
        for (UserEx user : users) {
            exportUser(xmlStreamWriter, user);
        }
        xmlStreamWriter.writeEndElement();
    }

    private void exportUser(XMLStreamWriter xmlStreamWriter, UserEx user) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("user");

        final String userName = user.getUserName();
        final String escapedUserName = StringEscapeUtils.escapeXml(userName);
        xmlStreamWriter.writeAttribute("userName", escapedUserName);

        final String password = user.getPassword();
        final String escapedPassword = StringEscapeUtils.escapeXml(password);
        xmlStreamWriter.writeAttribute("password", escapedPassword);

        final String email = user.getEmail();
        final String escapedEmail = StringEscapeUtils.escapeXml(email);
        if (email != null) {
            xmlStreamWriter.writeAttribute("email", escapedEmail);
        }

        if (user.isAdmin()) {
            xmlStreamWriter.writeAttribute("admin", "true");
        }

        if (user.isBuiltIn()) {
            xmlStreamWriter.writeAttribute("external", "true");
        }

        if (!user.isEnabled()) {
            xmlStreamWriter.writeAttribute("disabled", "true");
        }

        final Long lastSignedIn = user.getLastSignedIn();
        if (lastSignedIn != null) {
            final String formatedDate = _dateFormat.format(lastSignedIn);
            xmlStreamWriter.writeAttribute("lastSignedIn", formatedDate);
        }

        xmlStreamWriter.writeEndElement();
    }

    private void exportApplicationBugs(final XMLStreamWriter xmlStreamWriter, String app) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("bugs");
        xmlStreamWriter.writeAttribute("app", app);
        exportBugs(xmlStreamWriter, app);
        xmlStreamWriter.writeEndElement();
    }

    private void exportBugs(final XMLStreamWriter xmlStreamWriter, String app) {
        _store.fetchBugs(app, new BugCallback() {
            @Override
            public void bug(String app, long bugId, String title) throws Exception {
                xmlStreamWriter.writeStartElement("bug");
                xmlStreamWriter.writeAttribute("id", Long.toString(bugId));
                xmlStreamWriter.writeAttribute("title", title);
                xmlStreamWriter.writeStartElement("hits");
                _store.fetchHits(bugId, new HitsCallback() {
                    @Override
                    public void hit(long hitId, long sessionId, String appVer, long dateReported, Long dateBuilt, boolean devBuild, Integer buildNumber, String user, String message, String stack) throws Exception {
                        xmlStreamWriter.writeStartElement("hit");
                        xmlStreamWriter.writeAttribute(NAME_HIT_ID, Long.toString(hitId));
                        xmlStreamWriter.writeAttribute(NAME_SESSION_ID, Long.toString(sessionId));
                        xmlStreamWriter.writeAttribute(NAME_DATE_REPORTED, _dateFormat.format(new Date(dateReported)));
                        if (user != null) {
                            xmlStreamWriter.writeAttribute(NAME_USER, user);
                        }
                        if (message != null) {
                            xmlStreamWriter.writeAttribute(NAME_MESSAGE, message);
                        }
                        if (stack != null) {
                            xmlStreamWriter.writeCharacters("\n");
                            xmlStreamWriter.writeCData(_unicodeEscaper.translate(stack));
                            xmlStreamWriter.writeCharacters("\n");
                        }
                        xmlStreamWriter.writeEndElement();
                    }
                });
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndElement();
            }
        });
    }
}

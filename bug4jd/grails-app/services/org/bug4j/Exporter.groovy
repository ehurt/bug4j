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





package org.bug4j;


import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.text.translate.AggregateTranslator
import org.apache.commons.lang3.text.translate.LookupTranslator
import org.apache.commons.lang3.text.translate.UnicodeEscaper

import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter

public class Exporter {

    private final DateFormat _dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
    private final String[][] NL = [
            ['\r', '\r'],
            ['\n', '\n'],
            ['\t', '\t'],
    ];
    final AggregateTranslator _unicodeEscaper = new AggregateTranslator(
            new LookupTranslator(NL),
            UnicodeEscaper.outsideOf(32, 0x7f)
    );

    private interface Streamer {
        void stream(XMLStreamWriter xmlStreamWriter) throws XMLStreamException;
    }

    public Exporter() {
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

    private void writeDate(XMLStreamWriter xmlStreamWriter, String qName, Date value) throws XMLStreamException {
        if (value != null) {
            xmlStreamWriter.writeAttribute(qName, _dateFormat.format(value));
        }
    }

    private void writeTimestamp(XMLStreamWriter xmlStreamWriter, String qName, Timestamp timestamp) throws XMLStreamException {
        if (timestamp != null) {
            xmlStreamWriter.writeAttribute(qName, _dateFormat.format(timestamp));
        }
    }

    public void exportAll(OutputStream outputStream, final Collection<User> users, final Collection<App> apps) throws XMLStreamException, IOException {
        withXMLStreamWriter(outputStream, new Streamer() {
            @Override
            public void stream(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
                exportAll(xmlStreamWriter, users, apps);
            }
        });
    }

    public void exportAppBugs(OutputStream outputStream, final App app) throws XMLStreamException, IOException {
        withXMLStreamWriter(outputStream, new Streamer() {
            @Override
            public void stream(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
                exportAppBugs(xmlStreamWriter, app);
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

    private void exportAll(final XMLStreamWriter xmlStreamWriter, Collection<User> users, Collection<App> apps) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("bug4j");

        exportUsers(xmlStreamWriter, users);

        exportApps(xmlStreamWriter, apps);

        xmlStreamWriter.writeEndElement();
    }

    private void exportApps(XMLStreamWriter xmlStreamWriter, Collection<App> apps) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("apps");
        for (App app : apps) {
            exportApp(xmlStreamWriter, app);
        }
        xmlStreamWriter.writeEndElement();
    }

    private void exportApp(XMLStreamWriter xmlStreamWriter, App app) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("app");
        xmlStreamWriter.writeAttribute("name", app.getLabel());
        xmlStreamWriter.writeAttribute("code", app.getCode());
        exportPackages(xmlStreamWriter, app);
        exportSessions(xmlStreamWriter, app);
        App.withTransaction { // Need a transaction for derby to read the stack traces
            exportBugs(xmlStreamWriter, app);
        }
        xmlStreamWriter.writeEndElement();
    }

    private void exportSessions(XMLStreamWriter xmlStreamWriter, App app) throws XMLStreamException {
        final Set<ClientSession> sessions = app.getClientSessions();
        if (!sessions.isEmpty()) {
            xmlStreamWriter.writeStartElement("sessions");
            for (ClientSession session : sessions) {
                xmlStreamWriter.writeStartElement("session");

                final long sessionId = session.id
                final String version = session.appVersion
                final Timestamp firstHit = session.firstHit
                final String hostName = session.hostName
                final Timestamp dateBuilt = session.dateBuilt
                final boolean devBuild = session.devBuild
                final Integer buildNumber = session.buildNumber

                writeLong(xmlStreamWriter, "sessionId", sessionId)
                writeString(xmlStreamWriter, "appVer", version)
                writeTimestamp(xmlStreamWriter, "firstHit", firstHit)
                writeString(xmlStreamWriter, "hostName", hostName)
                writeTimestamp(xmlStreamWriter, "buildDate", dateBuilt)
                writeBoolean(xmlStreamWriter, "devBuild", devBuild)
                writeInteger(xmlStreamWriter, "buildNumber", buildNumber)

                xmlStreamWriter.writeEndElement();
            }
            xmlStreamWriter.writeEndElement();
        }
    }

    private void exportPackages(XMLStreamWriter xmlStreamWriter, App app) throws XMLStreamException {
        final Set<AppPackages> appPackages = app.getAppPackages();
        if (!appPackages.isEmpty()) {
            xmlStreamWriter.writeStartElement("packages");
            for (AppPackages appPackage : appPackages) {
                xmlStreamWriter.writeStartElement("package");
                final String packageName = appPackage.getPackageName();
                xmlStreamWriter.writeAttribute("name", _unicodeEscaper.translate(packageName));
                xmlStreamWriter.writeEndElement();
            }
            xmlStreamWriter.writeEndElement();
        }
    }

    private void exportUsers(XMLStreamWriter xmlStreamWriter, Collection<User> users) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("users");
        for (User user : users) {
            exportUser(xmlStreamWriter, user);
        }
        xmlStreamWriter.writeEndElement();
    }

    private void exportUser(XMLStreamWriter xmlStreamWriter, User user) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("user");

        final String userName = user.getUsername();
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

        if (!user.isEnabled()) {
            xmlStreamWriter.writeAttribute("disabled", "true");
        }

        final Date lastSignedIn = user.getLastSignedIn();
        if (lastSignedIn != null) {
            writeDate(xmlStreamWriter, "lastSignedIn", lastSignedIn);
        }

        xmlStreamWriter.writeEndElement();
    }

    private void exportAppBugs(final XMLStreamWriter xmlStreamWriter, App app) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("bugs");
        xmlStreamWriter.writeAttribute("app", app.getCode());
        exportBugs(xmlStreamWriter, app);
        xmlStreamWriter.writeEndElement();
    }

    private void exportBugs(final XMLStreamWriter xmlStreamWriter, App app) throws XMLStreamException {
        final Set<Bug> bugs = app.getBugs();
        for (Bug bug : bugs) {
            xmlStreamWriter.writeStartElement("bug");
            xmlStreamWriter.writeAttribute("id", bug.id as String);
            xmlStreamWriter.writeAttribute("title", bug.title);
            xmlStreamWriter.writeStartElement("hits");
            for (Hit hit : bug.hits) {
                xmlStreamWriter.writeStartElement("hit");
                xmlStreamWriter.writeAttribute("id", hit.id as String);
                final String stackString = hit.stack?.stackText?.readStackString()

                if (hit.clientSession != null) {
                    xmlStreamWriter.writeAttribute("sessionId", hit.clientSession.id.toString());
                }
                xmlStreamWriter.writeAttribute("date", _dateFormat.format(hit.dateReported));
                if (hit.reportedBy != null) {
                    xmlStreamWriter.writeAttribute("user", hit.reportedBy);
                }
                if (hit.message != null) {
                    xmlStreamWriter.writeAttribute("message", hit.message);
                }
                if (hit.remoteAddr) {
                    xmlStreamWriter.writeAttribute("remoteAddr", hit.remoteAddr);
                }
                if (stackString != null) {
                    xmlStreamWriter.writeCharacters("\n");
                    xmlStreamWriter.writeCData(_unicodeEscaper.translate(stackString));
                    xmlStreamWriter.writeCharacters("\n");
                }
                xmlStreamWriter.writeEndElement();
            }
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeEndElement();
        }
    }
}

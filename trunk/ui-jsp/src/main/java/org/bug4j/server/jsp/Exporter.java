/*
 * Copyright 2011 Cedric Dandoy
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
import org.bug4j.gwt.admin.client.data.User;
import org.bug4j.server.store.HitsCallback;
import org.bug4j.server.store.Store;
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

        final List<User> users = _store.getUsers();
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
        exportBugs(xmlStreamWriter, application);
        xmlStreamWriter.writeEndElement();
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

    private void exportUsers(XMLStreamWriter xmlStreamWriter, List<User> users) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("users");
        for (User user : users) {
            exportUser(xmlStreamWriter, user);
        }
        xmlStreamWriter.writeEndElement();
    }

    private void exportUser(XMLStreamWriter xmlStreamWriter, User user) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("user");

        final String userName = user.getUserName();
        final String escapedUserName = StringEscapeUtils.escapeXml(userName);
        xmlStreamWriter.writeAttribute("userName", escapedUserName);

        final String email = user.getEmail();
        final String escapedEmail = StringEscapeUtils.escapeXml(email);
        if (email != null) {
            xmlStreamWriter.writeAttribute("email", escapedEmail);
        }

        if (user.isAdmin()) {
            xmlStreamWriter.writeAttribute("admin", "true");
        }

        if (!user.isBuiltIn()) {
            xmlStreamWriter.writeAttribute("builtin", "false");
        }

        if (!user.isEnabled()) {
            xmlStreamWriter.writeAttribute("enabled", "false");
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
                    public void hit(long hitId, String appVer, long dateReported, String user, String message, String stack) throws Exception {
                        xmlStreamWriter.writeStartElement("hit");
                        xmlStreamWriter.writeAttribute("id", Long.toString(hitId));
                        xmlStreamWriter.writeAttribute("date", _dateFormat.format(new Date(dateReported)));
                        xmlStreamWriter.writeAttribute("appVer", appVer);
                        if (user != null) {
                            xmlStreamWriter.writeAttribute("user", user);
                        }
                        if (message != null) {
                            xmlStreamWriter.writeAttribute("message", message);
                        }
                        xmlStreamWriter.writeCharacters("\n");
                        xmlStreamWriter.writeCData(_unicodeEscaper.translate(stack));
                        xmlStreamWriter.writeCharacters("\n");
                        xmlStreamWriter.writeEndElement();
                    }
                });
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndElement();
            }
        });
    }
}
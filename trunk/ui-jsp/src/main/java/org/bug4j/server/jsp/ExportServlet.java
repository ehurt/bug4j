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

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.log4j.Logger;
import org.bug4j.server.gwt.client.data.Bug;
import org.bug4j.server.gwt.client.data.Filter;
import org.bug4j.server.gwt.client.data.Hit;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 */
public class ExportServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ExportServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    private void doit(HttpServletRequest request, HttpServletResponse response) {

        try {
            response.setHeader("Expires", "-1");
            response.setHeader("Cache-Control", "private, max-age=0");
            response.setHeader("Content-Type", "text/xml");
            response.setHeader("Content-Disposition", "attachment; filename=\"bugs.zip\"");
            final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
            final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            final ServletOutputStream outputStream = response.getOutputStream();
            final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            zipOutputStream.putNextEntry(new ZipEntry("bugs.xml"));
            try {
                final CloseShieldOutputStream closeShieldOutputStream = new CloseShieldOutputStream(zipOutputStream);
                final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(closeShieldOutputStream);
                final XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(bufferedOutputStream);

                try {
                    final Store store = StoreFactory.getStore();
                    final String app = request.getParameter("a");
                    final List<Bug> bugs = store.getBugs(app, new Filter(), 0, Integer.MAX_VALUE, "");
                    xmlStreamWriter.writeStartElement("bugs");
                    for (Bug bug : bugs) {
                        final long bugId = bug.getId();
                        final String bugTitle = bug.getTitle();
                        xmlStreamWriter.writeStartElement("bug");
                        xmlStreamWriter.writeAttribute("id", Long.toString(bugId));
                        xmlStreamWriter.writeAttribute("title", bugTitle);

                        final List<Hit> hits = store.getHits(bugId, 0, Integer.MAX_VALUE, "");
                        xmlStreamWriter.writeStartElement("hits");
                        for (Hit hit : hits) {
                            final long hitId = hit.getId();
                            final long dateReported = hit.getDateReported();
                            final String stack = store.getStack(hitId);
                            xmlStreamWriter.writeStartElement("hit");
                            xmlStreamWriter.writeAttribute("id", Long.toString(hitId));
                            xmlStreamWriter.writeAttribute("date", dateFormat.format(new Date(dateReported)));
                            xmlStreamWriter.writeCData(stack);
                            xmlStreamWriter.writeEndElement();
                        }
                        xmlStreamWriter.writeEndElement();
                        xmlStreamWriter.writeEndElement();
                    }
                    xmlStreamWriter.writeEndElement();
                } finally {
                    xmlStreamWriter.close();
                }
            } catch (XMLStreamException e) {
                response.sendError(500);
            } finally {
                zipOutputStream.closeEntry();
                zipOutputStream.close();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to export", e);
            throw new IllegalStateException("Failed to export", e);
        }
    }
}

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

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
    <bugs app="appname">
        <bug id="1" title="FileNotFoundException at Bug4jTest.java:38">
            <hits>
                <hit id="1" date="Monday, August 15, 2011 7:47:49 AM CDT" user="dandoy">
                <![CDATA[...
                ]]>
            </hit>
        </bug>
    </bugs>
 */
public abstract class Importer {
    private static final Logger LOGGER = Logger.getLogger(Importer.class);

    private final DateFormat _dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
    private String _app;
    private int _bugId;
    private String _title;
    private boolean _inHit;
    private long _hitId;
    private Date _date;
    private String _appVer;
    private String _user;
    private String _message;

    public Importer() {
    }

    public void importFile(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        final SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.parse(inputStream, new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if ("bugs".equals(qName)) {
                    startBugs(attributes);
                } else if ("bug".equals(qName)) {
                    startBug(attributes);
                } else if ("hits".equals(qName)) {
                    startHits();
                } else if ("hit".equals(qName)) {
                    startHit(attributes);
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if ("bugs".equals(qName)) {
                    endBugs();
                } else if ("bug".equals(qName)) {
                    endBug();
                } else if ("hits".equals(qName)) {
                    endHits();
                } else if ("hit".equals(qName)) {
                    endHit();
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                whenCharacters(ch, start, length);
            }
        });
    }

    private void startBugs(Attributes attributes) {
        _app = attributes.getValue("app");
        whenApp(_app);
    }

    protected void endBugs() {
        _app = null;
    }

    protected void startBug(Attributes attributes) {
        _bugId = Integer.parseInt(attributes.getValue("id"));
        _title = attributes.getValue("title");
        whenBug(_app, _bugId);
    }

    protected void endBug() {
        _bugId = 0;
        _title = null;
    }

    protected void startHits() {
    }

    protected void endHits() {
    }

    protected void startHit(Attributes attributes) {
        final String hitIdValue = attributes.getValue("id");
        try {
            _hitId = Long.parseLong(hitIdValue);
            final String dateValue = attributes.getValue("date");
            try {
                _date = _dateFormat.parse(dateValue);
                _appVer = attributes.getValue("appVer");
                _user = attributes.getValue("user");
                _message = attributes.getValue("message");
                _inHit = true;
            } catch (ParseException e) {
                LOGGER.error("Failed to parse the date '" + dateValue + "'");
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Failed to parse the hit id='" + hitIdValue + "'");
        }
    }

    protected void endHit() {
        _inHit = false;
    }

    protected void whenCharacters(char[] ch, int start, int length) {
        if (_inHit) {
            final String stack = new String(ch, start, length);
            final long dateReported = _date.getTime();
            whenHit(_app, _bugId, _title, _hitId, dateReported, _appVer, _user, _message, stack);
        }
    }

    protected void whenApp(String app) {
    }

    protected void whenBug(String app, int bugId) {
    }

    protected abstract void whenHit(String app, int bugId, String title, long hitId, long dateReported, String appVer, String user, String message, String stack);
}

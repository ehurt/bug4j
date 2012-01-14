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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
@SuppressWarnings({"UnusedParameters"})
public abstract class Importer {
    private static final Logger LOGGER = Logger.getLogger(Importer.class);

    protected final DateFormat _dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
    protected String _appName;
    protected long _bugId;
    protected String _title;
    protected StringBuilder _stack;
    protected long _hitId;
    protected Long _dateReported;
    protected String _appVer;
    protected String _user;
    protected String _message;
    protected ArrayList<String> _packages;
    private Long _sessionId;
    private Long _buildDate;
    private boolean _devBuild;
    private Integer _buildNumber;
    private Map<Long, Long> _sessionMap = new HashMap<Long, Long>();

    public Importer() {
    }

    public void importFile(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        final SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.parse(inputStream, new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if ("bug4j".equals(qName)) {
                } else if ("users".equals(qName)) {
                    startUsers();
                } else if ("user".equals(qName)) {
                    startUser(attributes);
                } else if ("apps".equals(qName)) {
                    startApps();
                } else if ("app".equals(qName)) {
                    startApp(attributes);
                } else if ("packages".equals(qName)) {
                    startPackages();
                } else if ("package".equals(qName)) {
                    startPackage(attributes);
                } else if ("bugs".equals(qName)) {
                    startBugs();
                } else if ("bug".equals(qName)) {
                    startBug(attributes);
                } else if ("hits".equals(qName)) {
                    startHits();
                } else if ("hit".equals(qName)) {
                    startHit(attributes);
                } else if ("session".equals(qName)) {
                    startSession(attributes);
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if ("bug4j".equals(qName)) {
                } else if ("users".equals(qName)) {
                    endUsers();
                } else if ("apps".equals(qName)) {
                    endApps();
                } else if ("app".equals(qName)) {
                    endApp();
                } else if ("packages".equals(qName)) {
                    endPackages();
                } else if ("bugs".equals(qName)) {
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

    protected void whenCharacters(char[] ch, int start, int length) {
        if (_stack != null) {
            final String stack = new String(ch, start, length);
            _stack.append(stack);
        }
    }

    protected void startUsers() {
    }

    protected void startUser(Attributes attributes) {
        final String userName = attributes.getValue("userName");
        final String password = attributes.getValue("password");
        final String email = attributes.getValue("email");
        final boolean admin = Boolean.parseBoolean(attributes.getValue("admin"));
        final boolean external = Boolean.parseBoolean(attributes.getValue("external"));
        final boolean disabled = Boolean.parseBoolean(attributes.getValue("disabled"));
        whenUser(userName, password, email, admin, external, disabled);
    }

    protected void whenUser(String userName, String password, String email, boolean admin, boolean external, boolean disabled) {
    }

    protected void endUsers() {
    }

    protected void startApps() {
    }

    protected void startApp(Attributes attributes) {
        _appName = attributes.getValue("name");
        whenApp(_appName);
    }

    protected void whenApp(String appName) {
    }

    protected void startPackages() {
        _packages = new ArrayList<String>();
    }

    protected void startPackage(Attributes attributes) {
        final String name = attributes.getValue("name");
        whenPackage(name);
    }

    protected void whenPackage(String packageName) {
        _packages.add(packageName);
    }

    protected void endPackages() {
        _packages = null;
    }

    protected void startBugs() {
    }

    protected void startBug(Attributes attributes) {
        _bugId = Long.parseLong(attributes.getValue("id"));
        _title = attributes.getValue("title");
        whenBug(_bugId, _title);
    }

    protected void whenBug(long bugId, String title) {
    }

    protected void endBug() {
        _bugId = 0;
        _title = null;
    }

    protected void startHits() {
    }

    protected void startHit(Attributes attributes) {
        final String hitIdValue = attributes.getValue(ImportExportConstants.NAME_HIT_ID);
        try {
            _hitId = Long.parseLong(hitIdValue);
            final String sessionIdValue = attributes.getValue(ImportExportConstants.NAME_SESSION_ID);
            try {
                if (sessionIdValue != null) {
                    _sessionId = Long.parseLong(sessionIdValue);
                    _dateReported = getDateAsLong(attributes, ImportExportConstants.NAME_DATE_REPORTED);
                    _appVer = attributes.getValue(ImportExportConstants.NAME_APP_VER);
                    _user = attributes.getValue(ImportExportConstants.NAME_USER);
                    _message = attributes.getValue(ImportExportConstants.NAME_MESSAGE);
                    _stack = new StringBuilder();
                    _buildDate = getDateAsLong(attributes, ImportExportConstants.NAME_BUILD_DATE);
                    _devBuild = Boolean.parseBoolean(attributes.getValue(ImportExportConstants.NAME_DEV_BUILD));

                    {
                        final String buildNumberValue = attributes.getValue(ImportExportConstants.NAME_BUILD_NUMBER);
                        if (buildNumberValue != null) {
                            try {
                                _buildNumber = Integer.parseInt(buildNumberValue);
                            } catch (NumberFormatException e) {
                                LOGGER.error("Failed to parse the build number '" + buildNumberValue + "'");
                            }
                        }
                    }
                } else {
                    LOGGER.error("Missing session id for hit id='" + hitIdValue + "'");
                }

            } catch (NumberFormatException e) {
                LOGGER.error("Failed to parse session of hit id='" + hitIdValue + "'");
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Failed to parse the hit id='" + hitIdValue + "'");
        }
    }

    protected void endHit() {
        final String stack = _stack.toString();
        if (_dateReported != null) {
            final Long newSessionId = _sessionMap.get(_sessionId);
            whenHit(_appName, newSessionId, _bugId, _title, _hitId, _dateReported, _appVer, _user, _message, stack, _buildDate, _devBuild, _buildNumber);
        } else {
            LOGGER.error("Bug " + _bugId + " is missing a reported date");
        }

        _hitId = 0;
        _sessionId = null;
        _dateReported = null;
        _appVer = null;
        _user = null;
        _message = null;
        _stack = null;
        _buildDate = null;
        _devBuild = false;
        _buildNumber = null;
    }

    protected void endHits() {
    }

    protected void endBugs() {
    }

    protected void endApp() {
        _appName = null;
    }

    protected void endApps() {
    }

    private void startSession(Attributes attributes) {
        final long sessionId = getLong(attributes, ImportExportConstants.NAME_SESSION_ID);
        final String application = attributes.getValue(ImportExportConstants.NAME_SESSION_APP_NAME);
        final String version = attributes.getValue(ImportExportConstants.NAME_SESSION_APP_VER);
        final Long firstHit = getDateAsLong(attributes, ImportExportConstants.NAME_SESSION_FIRST_HIT);
        final String hostName = attributes.getValue(ImportExportConstants.NAME_SESSION_HOST_NAME);
        final long newSessionId = whenSession(sessionId, application, version, firstHit, hostName);
        _sessionMap.put(sessionId, newSessionId);
    }

    private static long getLong(Attributes attributes, String qName) {
        final String value = attributes.getValue(qName);
        if (value != null) {
            try {
                final long sessionId = Long.parseLong(value);
                return sessionId;
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid value " + value + " for attribute " + ImportExportConstants.NAME_SESSION_ID);
            }
        } else {
            throw new IllegalStateException("Missing value for attribute " + ImportExportConstants.NAME_SESSION_ID);
        }
    }

    private Long getDateAsLong(Attributes attributes, String qName) {
        Long ret = null;
        final String value = attributes.getValue(qName);
        if (value != null) {
            try {
                final Date date = _dateFormat.parse(value);
                ret = date.getTime();
            } catch (ParseException e) {
                LOGGER.error("Failed to parse the date '" + value + "'");
            }
        }

        return ret;
    }

    protected abstract long whenSession(long sessionId, String application, String version, Long firstHit, String hostName);

    protected abstract void whenHit(String app, Long sessionId, long bugId, String title, long hitId, long dateReported, String appVer, String user, String message, String stack, Long buildDate, boolean devBuild, Integer buildNumber);
}

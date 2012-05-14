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





package org.bug4j

import org.apache.commons.lang.StringUtils
import org.bug4j.common.FullStackHashCalculator
import org.bug4j.common.TextToLines
import org.bug4j.server.processor.StackAnalyzer
import org.bug4j.server.processor.StackPathHashCalculator
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.zip.ZipInputStream
import javax.xml.parsers.SAXParserFactory

class BugService {
    ClientSession createSession(String appCode,
                                String appVersion,
                                Long dataMillis,
                                String devBuild,
                                Integer buildNumber,
                                String remoteAddr) {

        log.debug("createSession ${appCode}")

        final app = App.findByCode(appCode)
        if (!app) {
            log.warn("Request to create a session for an invalid application. Application:${appCode} - from:${remoteAddr}")
            throw new IllegalArgumentException("Unknown application: ${appCode}")
        }

        final clientSession = new ClientSession(
                app: app,
                appVersion: appVersion,
                dateBuilt: new Timestamp(dataMillis),
                devBuild: 'Y'.equals(devBuild),
                buildNumber: buildNumber,
                hostName: remoteAddr,
                firstHit: new Timestamp(System.currentTimeMillis())
        )
        app.addToClientSessions(clientSession)
        if (!clientSession.save()) {
            log.error("Failed to create a session: ${clientSession.errors.toString()}")
            throw new IllegalStateException('Failed to create a session: ')
        }
        return clientSession
    }

    /**
     * When a client sends a bug report, it first calculates the full hash of the stack
     * and asks if that one has already been reported.
     */
    boolean isNewBug(String sessionId,
                     String appCode,
                     String message,
                     String user,
                     String hash) {

        log.debug("check ${sessionId}-${appCode}-${user}-${hash}-${message}")

        if (sessionId == null || appCode == null || hash == null) {
            throw new IllegalArgumentException('Invalid request parameters')
        }

        final clientSession = null
        if (sessionId) {
            clientSession = ClientSession.get(sessionId)
            if (!clientSession) {
                throw new IllegalArgumentException('Invalid request parameters')
            }
        }

        final App app = App.findByCode(appCode)
        if (!app) {
            final errorMessage = "Unknown application: ${appCode}"
            log.error(errorMessage)
            throw new IllegalArgumentException(errorMessage)
        }

        def results = Stack.find("from Stack s, Hit h, Bug b where s.hash=:hash and s=h.stack and h.bug=b and b.app=:app",
                [app: app, hash: hash])

        if (results) {
            Stack stack = (Stack) results[0]
            Bug bug = (Bug) results[2]
            final newHit = new Hit(
                    bug: bug,
                    clientSession: clientSession,
                    stack: stack,
                    dateReported: new Date(),
                    reportedBy: user,
                    message: message
            )
            bug.addToHits(newHit)
            if (clientSession) {
                clientSession.addToHits(newHit)
            }
            stack.addToHits(newHit)
            newHit.save()
            return false
        } else {
            return true
        }
    }

    /**
     * Reports a bug
     */
    def reportBug(long sessionId, String appCode, String message, long dateReported, String user, String stackString) {

        if (sessionId == null) {
            throw new IllegalArgumentException('Invalid request parameters')
        }

        final clientSession = null
        if (sessionId) {
            clientSession = ClientSession.get(sessionId)
            if (!clientSession) {
                throw new IllegalArgumentException('Invalid request parameters')
            }
        }

        final App app = App.findByCode(appCode)
        if (!app) {
            final errorMessage = "Cannot find application ${appCode}"
            log.error(errorMessage)
            throw new IllegalArgumentException(errorMessage)
        }

        reportBug(clientSession, app, message, dateReported, user, stackString)
    }

    def reportBug(ClientSession clientSession, App app, String message, long dateReported, String user, String stackString) {

        final List<String> stackLines = stackString ? TextToLines.toLineList(stackString.trim()) : [];
        if (message) {
            if (message.size() >= 1024) {
                message = message.substring(0, 1024)
            }
        }

        Bug bug = null
        Stack stack = null

        // First try based on the full hash of the exception.
        final String fullHash = hash(message, stackLines);
        def results = Stack.find("from Stack s, Hit h, Bug b where s.hash=:hash and s=h.stack and h.bug=b and b.app=:app",
                [app: app, hash: fullHash])
        if (results) {
            bug = (Bug) results[2]
            stack = (Stack) results[0]
        }

        if (!bug) {
            if (stackLines) {
                // Try to find a matching strain.
                final String strainHash = StackPathHashCalculator.analyze(stackLines);
                def strainAnBug = Strain.find("from Strain s, Bug b where s.hash=:hash and s.bug=b and b.app=:app",
                        [hash: strainHash, app: app])
                Strain strain
                if (strainAnBug == null) {
                    // Determine the title that the bug would get.
                    final List<String> appPackages = AppPackages.findAllByApp(app)*.packageName;
                    final StackAnalyzer stackAnalyzer = new StackAnalyzer();
                    stackAnalyzer.setApplicationPackages(appPackages);

                    String title = stackAnalyzer.getTitle(stackLines);
                    if (title == null) {
                        // This may happen if the stack does not contain any of the application packages.
                        // Try again without application packages
                        stackAnalyzer.setApplicationPackages(Collections.<String> emptyList());
                        title = stackAnalyzer.getTitle(stackLines);
                    }

                    if (title == null) {
                        throw new IllegalStateException("Failed to analyze a stack [\n" + stackString + "\n]");
                    }

                    // Try to find a bug with the exact same title
                    bug = identifyBugByTitle(app, stackLines, title);
                    if (!bug) {
                        // if everything failed then it is a new bug.
                        bug = new Bug(app: app, title: title)
                        app.addToBugs(bug)
                        bug.save(failOnError: true)
                    } // else ==> _matchByCauses++
                    strain = new Strain(bug: bug, hash: strainHash)
                    bug.addToStrains(strain)
                    strain.save(failOnError: true)
                } else {
                    // _matchByStrain++;
                    strain = (Strain) strainAnBug[0]
                    bug = (Bug) strainAnBug[1]
                }
                stack = new Stack(strain: strain, hash: fullHash)
                strain.addToStacks(stack)
                final stackText = new StackText(stack: stack)
                stackText.writeStackString(stackString)
                stack.setStackText(stackText)

            } else {
                bug = identifyBugByTitle(app, stackLines, message);
                if (!bug) {
                    bug = new Bug(app: app, title: message)
                    app.addToBugs(bug)
                }
                bug.save(failOnError: true)
                stack = new Stack(hash: fullHash)
            }
            stack.save(failOnError: true)
        }
        final newHit = new Hit(
                bug: bug,
                clientSession: clientSession,
                stack: stack,
                dateReported: new Date(dateReported),
                reportedBy: user,
                message: message
        )
        bug.addToHits(newHit)
        if (clientSession) {
            clientSession.addToHits(newHit)
        }
        stack.addToHits(newHit)
        newHit.save(failOnError: true)

        return bug.id;
    }

    private static String hash(String message, List<String> stackLines) {
        final String ret;
        final List<String> hashable;
        if (stackLines != null) {
            hashable = stackLines;
        } else {
            hashable = Collections.singletonList(message);
        }
        ret = FullStackHashCalculator.getTextHash(hashable);
        return ret;
    }

    /**
     * Tries to deduplicate based on the exact location and causes.
     * This addresses the case for example of a NPE at the exact same location but from different code paths.
     * To match, the underlying causes must be identical.
     */
    private Bug identifyBugByTitle(App app, List<String> thisStackLines, String title) {
        final StackAnalyzer stackAnalyzer = new StackAnalyzer();
        final List<String> thisCauses = stackAnalyzer.getCauses(thisStackLines);
        final List<Bug> bugs = Bug.findAllByAppAndTitle(app, title)
        for (Bug bug : bugs) {
            List<Hit> hits = bug.hits.sort {it.dateReported}.reverse()
            hits.each {Hit hit ->
                final stack = hit.stack
                if (stack) {
                    final StackText stackText = stack.stackText
                    final String thatStackText = stackText.text
                    final List<String> thatStackLines = TextToLines.toLineList(thatStackText);
                    final List<String> thatCauses = stackAnalyzer.getCauses(thatStackLines);
                    if (thisCauses.equals(thatCauses)) {
                        return bug;
                    }
                }

            }
        }
        return null;
    }


    public void importFile(File file) {
        if (file.name.endsWith('.zip')) {
            file.withInputStream {
                final zipInputStream = new ZipInputStream(it)
                try {
                    final entry = zipInputStream.nextEntry
                    if (entry.name != 'bugs.xml') {
                        throw new IllegalArgumentException('Invalid file: ' + file)
                    }
                    importFile(zipInputStream)
                } finally {
                    zipInputStream.close()
                }
            }
        } else {
            file.withInputStream {
                importFile(it)
            }
        }
    }

    private void importFile(InputStream inputStream) {
        final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);

        def reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
        reader.setContentHandler(new DefaultHandler() {
            String _user
            String _password
            boolean _admin
            boolean _external

            App _app
            String _bugTitle

            String _hitId
            String _sessionId
            String _message
            String _appVer
            long _dateReported
            String _stackString
            Map<String, ClientSession> _sessions = new HashMap<String, ClientSession>()

            void startElement(String ns, String localName, String qName, Attributes atts) {
                switch (qName) {
                    case 'user':
                        _user = atts.getValue('userName')
                        _password = atts.getValue('password')
                        _admin = 'true' == atts.getValue('admin')
                        _external = 'true' == atts.getValue('external')
                        if (!User.findByUsername(_user)) {
                            new User(username: _user, password: _password, enabled: true).save(flush: true)
                        }

                        break;
                    case 'app':
                        String appName = atts.getValue('name')
                        _app = App.findByLabel(appName)
                        if (!_app) {
                            _app = new App(label: appName, code: appName)
                            _app.save(flush: true)
                        }
                        break

                    case 'package':
                        final packageName = atts.getValue('name')
                        if (!AppPackages.findByAppAndPackageName(_app, packageName)) {
                            final appPackages = new AppPackages(app: _app, packageName: packageName)
                            _app.addToAppPackages(appPackages)
                            appPackages.save(flush: true)
                        }
                        break;

                    case 'session':
                        final String sessionId = atts.getValue('sessionId')
                        final appVer = atts.getValue('appVer')
                        final firstHit = dateFormat.parse(atts.getValue('firstHit')).getTime()
                        final hostName = atts.getValue('hostName')
                        final buildDate = dateFormat.parse(atts.getValue('buildDate')).getTime()
                        final buildNumber = atts.getValue('buildNumber')
                        final clientSession = new ClientSession(
                                app: _app,
                                dateBuilt: new Timestamp(buildDate),
                                devBuild: false,
                                buildNumber: buildNumber as int,
                                hostName: hostName,
                                firstHit: new Timestamp(firstHit)
                        )
                        _app.addToClientSessions(clientSession)
                        clientSession.save(flush: true)
                        _sessions.put(sessionId, clientSession)
                        break;

                    case 'bug':
                        final bugId = atts.getValue('id')
                        _bugTitle = atts.getValue('title')
                        break
                    case 'hit':
                        _hitId = atts.getValue('id')
                        _sessionId = atts.getValue('sessionId')
                        _user = atts.getValue('user')
                        String dateString = atts.getValue('date')
                        _dateReported = dateFormat.parse(dateString).getTime()
                        _appVer = atts.getValue('appVer')
                        _message = atts.getValue('message')
                        _stackString = ''
                        break;
                }
            }

            void characters(char[] chars, int offset, int length) {
                if (_hitId) {
                    final String s = new String(chars, offset, length)
                    final String trimmed = StringUtils.remove(s, '\n')
                    trimmed = StringUtils.remove(trimmed, '\r')
                    trimmed = StringUtils.remove(trimmed, ' ')
                    if (trimmed) {
                        _stackString = s
                    }
                }
            }

            void endElement(String ns, String localName, String qName) {
                switch (qName) {
                    case 'user':
                        break
                    case 'app':
                        _app = null
                        break
                    case 'package':
                        break;
                    case 'bug':
                        _bugTitle = null
                        break
                    case 'hit':
                        final clientSession = _sessions.get(_sessionId)
                        reportBug(clientSession, _app, _message, _dateReported, _user, _stackString)
                        _hitId = null
                        break;
                }
            }
        })
        reader.parse(new InputSource(inputStream))
    }

    public void export(OutputStream outputStream) {
        final exporter = new Exporter()
        final users = User.list()
        final apps = App.list()
        exporter.exportAll(outputStream, users, apps)
    }
}

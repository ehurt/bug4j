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
    ClientSession createSession(String applicationCode,
                                String applicationVersion,
                                Long dataMillis,
                                String devBuild,
                                Integer buildNumber,
                                String remoteAddr) {

        log.debug("createSession ${applicationCode}")

        final application = Application.findByCode(applicationCode)
        if (!application) {
            log.warn("Request to create a session for an invalid application. Application:${applicationCode} - from:${remoteAddr}")
            throw new IllegalArgumentException("Unknown application: ${applicationCode}")
        }

        final clientSession = new ClientSession(
                application: application,
                applicationVersion: applicationVersion,
                dateBuilt: new Timestamp(dataMillis),
                devBuild: 'Y'.equals(devBuild),
                buildNumber: buildNumber,
                hostName: remoteAddr,
                firstHit: new Timestamp(System.currentTimeMillis())
        )
        application.addToClientSessions(clientSession)
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
                     String applicationCode,
                     String message,
                     String user,
                     String hash) {

        log.debug("check ${sessionId}-${applicationCode}-${user}-${hash}-${message}")

        if (sessionId == null || applicationCode == null || hash == null) {
            throw new IllegalArgumentException('Invalid request parameters')
        }

        final clientSession = null
        if (sessionId) {
            clientSession = ClientSession.get(sessionId)
            if (!clientSession) {
                throw new IllegalArgumentException('Invalid request parameters')
            }
        }

        final Application application = Application.findByCode(applicationCode)
        if (!application) {
            final errorMessage = "Unknown application: ${applicationCode}"
            log.error(errorMessage)
            throw new IllegalArgumentException(errorMessage)
        }

        def results = Stack.find("from Stack s, Hit h, Bug b where s.hash=:hash and s=h.stack and h.bug=b and b.application=:application",
                [application: application, hash: hash])

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
    def reportBug(long sessionId, String applicationCode, String message, long dateReported, String user, String stackString) {

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

        final Application application = Application.findByCode(applicationCode)
        if (!application) {
            final errorMessage = "Cannot find application ${applicationCode}"
            log.error(errorMessage)
            throw new IllegalArgumentException(errorMessage)
        }

        reportBug(clientSession, application, message, dateReported, user, stackString)
    }

    def reportBug(ClientSession clientSession, Application application, String message, long dateReported, String user, String stackString) {

        final List<String> stackLines = stackString ? TextToLines.toLineList(stackString.trim()) : [];

        Bug bug = null
        Stack stack = null

        // First try based on the full hash of the exception.
        final String fullHash = hash(message, stackLines);
        def results = Stack.find("from Stack s, Hit h, Bug b where s.hash=:hash and s=h.stack and h.bug=b and b.application=:application",
                [application: application, hash: fullHash])
        if (results) {
            bug = (Bug) results[2]
            stack = (Stack) results[0]
        }

        if (!bug) {
            if (stackLines) {
                // Try to find a matching strain.
                final String strainHash = StackPathHashCalculator.analyze(stackLines);
                Strain strain = Strain.find("from Strain s, Bug b where s.hash=:hash and s.bug=b and b.application=:application",
                        [hash: strainHash, application: application])
                if (strain == null) {
                    // Determine the title that the bug would get.
                    final applicationPackages = ApplicationPackages.findAllByApplication(application)
                    final List<String> appPackages = applicationPackages*.packageName;
                    final StackAnalyzer stackAnalyzer = new StackAnalyzer();
                    stackAnalyzer.setApplicationPackages(appPackages);

                    String title = stackAnalyzer.getTitle(stackLines);
                    if (title == null) { // This may happen if the stack does not contain any of the application packages.
                        // Try again without application packages
                        stackAnalyzer.setApplicationPackages(Collections.<String> emptyList());
                        title = stackAnalyzer.getTitle(stackLines);
                    }

                    if (title == null) {
                        throw new IllegalStateException("Failed to analyze a stack [\n" + stackString + "\n]");
                    }

                    // Try to find a bug with the exact same title
                    bug = identifyBugByTitle(application, stackLines, title);
                    if (!bug) {
                        // if everything failed then it is a new bug.
                        bug = new Bug(application: application, title: title)
                        application.addToBugs(bug)
                        bug.save(failOnError: true)
                    } // else ==> _matchByCauses++
                    strain = new Strain(bug: bug, hash: strainHash)
                    bug.addToStrains(strain)
                    strain.save(failOnError: true)
                } else {
                    // _matchByStrain++;
                    bug = strain.bug
                }
                stack = new Stack(strain: strain, hash: fullHash)
                strain.addToStacks(stack)
                final stackText = new StackText(stack: stack)
                stackText.writeStackString(stackString)
                stack.setStackText(stackText)

            } else {
                bug = identifyBugByTitle(application, stackLines, message);
                if (!bug) {
                    bug = new Bug(application: application, title: message)
                    application.addToBugs(bug)
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
                dateReported: new Date(),
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
    private Bug identifyBugByTitle(Application application, List<String> thisStackLines, String title) {
        final StackAnalyzer stackAnalyzer = new StackAnalyzer();
        final List<String> thisCauses = stackAnalyzer.getCauses(thisStackLines);
        final List<Bug> bugs = Bug.findAllByApplicationAndTitle(application, title)
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

            Application _application
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
                        String applicationName = atts.getValue('name')
                        _application = Application.findByLabel(applicationName)
                        if (!_application) {
                            _application = new Application(label: applicationName, code: applicationName)
                            _application.save(flush: true)
                        }
                        break

                    case 'package':
                        final packageName = atts.getValue('name')
                        if (!ApplicationPackages.findByApplicationAndPackageName(_application, packageName)) {
                            final applicationPackages = new ApplicationPackages(application: _application, packageName: packageName)
                            _application.addToApplicationPackages(applicationPackages)
                            applicationPackages.save(flush: true)
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
                                application: _application,
                                dateBuilt: new Timestamp(buildDate),
                                devBuild: false,
                                buildNumber: buildNumber as int,
                                hostName: hostName,
                                firstHit: new Timestamp(firstHit)
                        )
                        _application.addToClientSessions(clientSession)
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
                        _application = null
                        break
                    case 'package':
                        break;
                    case 'bug':
                        _bugTitle = null
                        break
                    case 'hit':
                        final clientSession = _sessions.get(_sessionId)
                        reportBug(clientSession, _application, _message, _dateReported, _user, _stackString)
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
        final applications = Application.list()
        exporter.exportAll(outputStream, users, applications)
    }
}

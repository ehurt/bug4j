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
package org.bug4j.server

import org.apache.commons.lang.StringUtils
import org.bug4j.server.processor.FullStackHashCalculator
import org.bug4j.server.processor.StackAnalyzer
import org.bug4j.server.processor.StackPathHashCalculator
import org.bug4j.server.processor.TextToLines
import org.bug4j.server.util.DateUtil
import org.bug4j.server.util.StringUtil
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.util.zip.ZipInputStream
import javax.xml.parsers.SAXParserFactory

import org.bug4j.*

class BugService {
    public static final char[] PATTERN_CHARS = '\\[].^$?*+'.toCharArray()
    def extensionService

    private Map<String, String> _hostNameCache = [:]

    ClientSession createSession(String appCode,
                                String appVersion,
                                Long buildDateMillis,
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
                dateBuilt: new Timestamp(buildDateMillis),
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
                     String remoteAddr,
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
                    message: message,
                    remoteAddr: remoteAddr,
            )
            bug.addToHits(newHit)
            testForMultiReports(bug, remoteAddr)
            if (clientSession) {
                clientSession.addToHits(newHit)
            }
            stack.addToHits(newHit)
            newHit.save()
            extensionService.whenHit(newHit)
            return false
        } else {
            return true
        }
    }

    /**
     * Reports a bug
     */
    long reportBug(long sessionId, String appCode, String message, long dateReported, String user, String remoteAddr, String stackString) {

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

        reportBug(clientSession, app, message, dateReported, user, remoteAddr, stackString)
    }

    long reportBug(ClientSession clientSession, App app, String message, long dateReported, String user, String remoteAddr, String stackString) {

        final List<String> stackLines = stackString ? TextToLines.toLineList(stackString.trim()) : null;
        if (message) {
            message = StringUtils.abbreviate(message, Hit.MESSAGE_SIZE)
        }

        String hostName = null
        if (remoteAddr) {
            synchronized (_hostNameCache) {
                hostName = _hostNameCache.get(remoteAddr)
                if (!hostName) {
                    final InetAddress inetAddress = InetAddress.getByName(remoteAddr)
                    hostName = inetAddress.getCanonicalHostName();
                    _hostNameCache.put(remoteAddr, hostName)
                }
            }
        }


        Bug bug = null
        Stack stack = null
        String fullHash = null
        boolean isNewBug = false;

        // First try based on the full hash of the exception.
        if (stackLines) {
            fullHash = FullStackHashCalculator.getTextHash(stackLines);

            def results = Stack.find("from Stack s, Hit h, Bug b where s.hash=:hash and s=h.stack and h.bug=b and b.app=:app",
                    [app: app, hash: fullHash])
            if (results) {
                bug = (Bug) results[2]
                stack = (Stack) results[0]
            }
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
                    title = StringUtil.fixTitle(title)

                    // Try to find a bug with the exact same title
                    bug = identifyBugByTitle(app, stackLines, title);
                    if (!bug) {
                        // if everything failed then it is a new bug.
                        bug = new Bug(app: app, title: title)
                        app.addToBugs(bug)
                        bug.save(failOnError: true)
                        extensionService.whenBug(bug)
                        isNewBug = true
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
                stack.save(failOnError: true)
            } else {
                String title = StringUtil.fixTitle(message)
                bug = identifyBugByTitle(app, stackLines, title);
                if (!bug) {
                    bug = new Bug(app: app, title: title)
                    app.addToBugs(bug)
                    bug.save(failOnError: true)
                    extensionService.whenBug(bug)
                }
            }
        }
        final newHit = new Hit(
                bug: bug,
                clientSession: clientSession,
                stack: stack,
                dateReported: new Date(dateReported),
                reportedBy: user,
                message: message,
                remoteAddr: hostName,
        )
        bug.addToHits(newHit)
        if (clientSession) {
            clientSession.addToHits(newHit)
        }
        if (stack) {
            stack.addToHits(newHit)
        }
        if (!isNewBug) {
            testForMultiReports(bug, hostName)
        }
        newHit.save(failOnError: true)

        extensionService.whenHit(newHit)

        return bug.id;
    }

    private void testForMultiReports(Bug bug, String remoteAddr) {
        if (!bug.multiReport) {
            if (remoteAddr) { // if remoteAddr is null we can't tell
                final hitFromOtherAddr = Hit.executeQuery("from Hit h where h.bug=:bug and h.remoteAddr != :remoteAddr",
                        [bug: bug, remoteAddr: remoteAddr])
                if (hitFromOtherAddr) {
                    bug.multiReport = true
                    bug.save()
                }
            }
        }
    }

    /**
     * Tries to deduplicate based on the exact location and causes.
     * This addresses the case for example of a NPE at the exact same location but from different code paths.
     * To match, the underlying causes must be identical.
     */
    private Bug identifyBugByTitle(App app, List<String> stackLines, String title) {
        final StackAnalyzer stackAnalyzer = new StackAnalyzer();
        if (stackLines) {
            final List<String> thisCauses = stackAnalyzer.getCauses(stackLines);
            final List<Bug> bugs = Bug.findAllByAppAndTitle(app, title)
            return Hit.withTransaction {
                for (Bug bug : bugs) {
                    List<Hit> hits = bug.hits.sort {it.dateReported}.reverse()
                    Bug ret = hits.findResult {Hit hit ->
                        final thatStack = hit.stack
                        List<String> thatCauses = null

                        final String thatStackString = thatStack?.stackText?.readStackString()
                        if (thatStackString) {
                            final List<String> thatStackLines = TextToLines.toLineList(thatStackString);
                            thatCauses = stackAnalyzer.getCauses(thatStackLines);
                        }
                        if (thisCauses == null && thatCauses == null) {
                            return bug;
                        }
                        if (thisCauses != null && thisCauses.equals(thatCauses)) {
                            return bug;
                        }
                    }
                    if (ret) {
                        return ret;
                    }
                }
            }
        } else {
            def bugs = Bug.executeQuery("select b from Bug b where b.title=:title",
                    [title: title],
                    [fetchSize: 1]
            )
            if (bugs) {
                return bugs[0]
            }
        }

        final MergePattern mergePattern = MergePattern.list().find {
            final pattern = Pattern.compile(it.patternString)
            final matcher = pattern.matcher(title)
            return matcher.matches()
        }

        if (mergePattern) {
            return mergePattern.bug
        }

        return null;
    }


    public void importNamedInputStream(InputStream inputStream, String fileName) {
        if (fileName.endsWith('.zip')) {
            final zipInputStream = new ZipInputStream(inputStream)
            try {
                final entry = zipInputStream.nextEntry
                if (entry.name != 'bugs.xml') {
                    throw new IllegalArgumentException('Invalid file: ' + fileName)
                }
                importInputStream(zipInputStream)
            } finally {
                zipInputStream.close()
            }
        } else {
            importInputStream(inputStream)
        }
    }

    private App identifyApp(String appId) {
        App app = App.findByCode(appId)
        if (!app) {
            app = App.findByLabel(appId)
        }
        return app
    }

    private void importInputStream(InputStream inputStream) {
        final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
        def _mergePatterns = []

        def reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
        Bug.withTransaction {

            reader.setContentHandler(new DefaultHandler() {
                String _userName

                App _app
                String _bugTitle

                Long _bugId
                String _hitId
                String _sessionId
                String _message
                String _remoteAddr
                String _appVer
                long _dateReported
                String _text
                Map<String, ClientSession> _sessions = new HashMap<String, ClientSession>()

                String _commentAddedBy
                long _commentDateAdded

                void startElement(String ns, String localName, String qName, Attributes atts) {
                    switch (qName) {
                        case 'user':
                            _userName = atts.getValue('userName')
                            String password = atts.getValue('password')
                            String email = atts.getValue('email')
                            boolean admin = 'true' == atts.getValue('admin')
                            boolean externalAuthentication = 'true' == atts.getValue('externalAuthentication') || 'true' == atts.getValue('external')
                            if (!User.findByUsername(_userName)) {
                                final user = new User(
                                        username: _userName,
                                        password: password,
                                        enabled: true,
                                        externalAuthentication: externalAuthentication,
                                        email: email
                                ).save()
                                if (admin) { // That's a 0.1 version
                                    UserRole.create(user, Role.findByAuthority(Role.USER))
                                    UserRole.create(user, Role.findByAuthority(Role.ADMIN))
                                }
                            }

                            break;

                        case 'role':
                            final user = User.findByUsername(_userName)
                            String roleName = atts.getValue('name')
                            final role = Role.findByAuthority(roleName)
                            if (role) {
                                UserRole.create(user, role)
                            }
                            break;

                        case 'app':
                            String appId = atts.getValue('name')
                            boolean multiHost = 'true' == atts.getValue('multiHost')
                            _app = identifyApp(appId)
                            if (!_app) {
                                _app = new App(label: appId, code: appId, multiHost: multiHost)
                                _app.save()
                            }
                            break

                        case 'package':
                            final packageName = atts.getValue('name')
                            if (!AppPackages.findByAppAndPackageName(_app, packageName)) {
                                final appPackages = new AppPackages(app: _app, packageName: packageName)
                                _app.addToAppPackages(appPackages)
                                appPackages.save()
                            }
                            break;

                        case 'session':
                            final String sessionId = atts.getValue('sessionId')
                            final appVer = atts.getValue('appVer')

                            def firstHitTimestamp = null
                            final firstHitString = atts.getValue('firstHit')
                            if (firstHitString != null) {
                                final firstHit = dateFormat.parse(firstHitString).getTime()
                                firstHitTimestamp = new Timestamp(firstHit)
                            }
                            final hostName = atts.getValue('hostName')

                            def buildDateTimestamp = null;
                            final buildDateString = atts.getValue('buildDate')
                            if (buildDateString) {
                                final buildDate = dateFormat.parse(buildDateString).getTime()
                                buildDateTimestamp = new Timestamp(buildDate)
                            }

                            final buildNumber = atts.getValue('buildNumber')
                            final clientSession = new ClientSession(
                                    app: _app,
                                    appVersion: appVer,
                                    dateBuilt: buildDateTimestamp,
                                    devBuild: false,
                                    buildNumber: buildNumber as Integer,
                                    hostName: hostName,
                                    firstHit: firstHitTimestamp
                            )
                            _app.addToClientSessions(clientSession)
                            clientSession.save()
                            _sessions.put(sessionId, clientSession)
                            break;

                        case 'bug':
//                            final bugId = atts.getValue('id')
                            _bugTitle = atts.getValue('title')
                            break

                        case 'bugs':
                            final appId = atts.getValue('app')
                            if (appId) {
                                _app = App.findByCode(appId)
                                if (!_app) {
                                    _app = new App(label: appId, code: appId)
                                    _app.save()
                                }
                            }
                            break;

                        case 'hit':
                            _hitId = atts.getValue('id')
                            _sessionId = atts.getValue('sessionId')
                            _userName = atts.getValue('user')
                            String dateString = atts.getValue('date')
                            _dateReported = dateFormat.parse(dateString).getTime()
                            _appVer = atts.getValue('appVer')
                            _message = atts.getValue('message')
                            _remoteAddr = atts.getValue('remoteAddr')
                            _text = ''
                            break;

                        case 'comment':
                            _commentAddedBy = atts.getValue('addedBy')
                            final String dateAddedString = atts.getValue('dateAdded')
                            _commentDateAdded = dateFormat.parse(dateAddedString).getTime()
                            _text = ''
                            break;

                        case 'mergePattern':
                            String pattern = atts.getValue('pattern')
                            _mergePatterns += [bugId: _bugId, pattern: pattern]
                            break;
                    }
                }

                void characters(char[] chars, int offset, int length) {
                    if (_hitId || _commentAddedBy) {
                        final String s = new String(chars, offset, length)
                        final String trimmed = StringUtils.remove(s, '\n')
                        trimmed = StringUtils.remove(trimmed, '\r')
                        trimmed = StringUtils.remove(trimmed, ' ')
                        if (trimmed) {
                            _text = s
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
                            final remoteAddr = _remoteAddr ? _remoteAddr : clientSession?.hostName
                            _bugId = reportBug(clientSession, _app, _message, _dateReported, _userName, remoteAddr, _text)
                            _hitId = null
                            break;
                        case 'comment':
                            final bug = Bug.get(_bugId)
                            String text;
                            Date dateAdded
                            String addedBy
                            final comment = new Comment(
                                    dateAdded: new Date(_commentDateAdded),
                                    addedBy: _commentAddedBy,
                                    text: _text,
                            )
                            comment.bug = bug
                            if (!comment.validate()) {
                                println comment.errors
                            }
                            comment.save()
                            _commentAddedBy = null
                            break;
                    }
                }
            })
            reader.parse(new InputSource(inputStream))
        }

        Bug.withTransaction {
            _mergePatterns.each {
                final bug = Bug.get(it.bugId)
                merge(bug, it.pattern)
            }
        }
    }

    public static String stackToHtml(String stackString, List<String> packages) {
        String stackHtml = ''
        if (stackString) {
            final hilightPrefixes = packages.collect {'\tat ' + it}
            stackString = stackString.replace('\r', '')
            List<String> lines = StringUtils.split(stackString, '\n')
            String messageLine = lines.remove(0)
            messageLine = highlightStackLine(messageLine, true)
            stackHtml = lines.sum {line ->
                def highlight = false
                if (line.startsWith('\tat ')) {
                    highlight = hilightPrefixes.find {hilightPrefix ->
                        if (line.startsWith(hilightPrefix)) {
                            return true
                        }
                    }
                } else {
                    if (!line.startsWith('\t...')) {
                        highlight = true
                    }
                }
                return highlightStackLine(line, highlight as boolean)
            }
            stackHtml = messageLine + stackHtml
        }
        return stackHtml
    }

    private static String highlightStackLine(String line, boolean highlight) {
        return "<div class=\"${highlight ? 'stack-highlight' : 'stack-dim'}\">${line.encodeAsHTML()}</div>"
    }

    public int merge(Bug bug, String titlePattern) {
        int ret = 0
        final app = bug.app
        final pattern = Pattern.compile(titlePattern)

        final bugs = Bug.findAllByApp(app, [sort: 'id', order: 'asc'])
        bugs.each {Bug matchingBug ->
            final String title = matchingBug.title
            final matcher = pattern.matcher(title)
            if (matcher.matches()) {
                if (bug.id != matchingBug.id) {
                    Hit.executeUpdate("update Hit set bug=:bug where bug=:matchingBug", [bug: bug, matchingBug: matchingBug])
                    Strain.executeUpdate("update Strain set bug=:bug where bug=:matchingBug", [bug: bug, matchingBug: matchingBug])
                    Comment.executeUpdate("update Comment set bug=:bug where bug=:matchingBug", [bug: bug, matchingBug: matchingBug])
                    MergePattern.executeUpdate("update MergePattern set bug=:bug where bug=:matchingBug", [bug: bug, matchingBug: matchingBug])
                    Bug.executeUpdate("delete Bug where id=:matchingBug", [matchingBug: matchingBug.id])
                }
                ret++
            }
        }

        final mergePattern = new MergePattern(patternString: titlePattern)
        mergePattern.bug = bug
        mergePattern.save()

        PATTERN_CHARS.each {
            titlePattern = titlePattern.replace('\\' + it, it.toString())
        }

        bug.title = titlePattern
        bug.save()
        return ret
    }

    def queryBugsParamsAndCondAndFilter(app, from, to, includeSingleHost, includeIgnored, sort, order, max, offset) {
        def queryParams = [app: app]
        def queryCond = ''
        def filter = ''

        if (from) {
            Date fromDate = DateUtil.interpretDate(from, DateUtil.TimeAdjustType.BEGINNING_OF_DAY)
            if (fromDate) {
                queryCond += " and h.dateReported>=:fromDate"
                queryParams += [fromDate: fromDate]
                filter += "from:${from} "
            }
        }

        if (to) {
            Date toDate = DateUtil.interpretDate(to, DateUtil.TimeAdjustType.END_OF_DAY)
            if (toDate) {
                queryCond += " and h.dateReported<=:toDate"
                queryParams += [toDate: toDate]
                filter += "to:${to} "
            }
        }

        if (app.multiHost) {
            if (includeSingleHost) {
                filter += "showSingleHost "
            } else {
                queryCond += " and b.multiReport = true"
            }
        }

        if (includeIgnored) {
            filter += "includeIgnored "
        } else {
            queryCond += " and b.ignore = false"
        }

        final sql = """
                select
                    b.id as id,
                    b.title as title,
                    count(h.id) as hitCount,
                    min(h.dateReported) as firstHitDate,
                    max(h.dateReported) as lastHitDate,
                    b.hot,
                    count(h.stack.id) as stackCount
                from Bug b, Hit h
                where b.app=:app
                and b=h.bug
                ${queryCond}
                group by b.id,b.title,b.hot
                order by ${sort} ${order}
                """

        return [sql, queryParams, queryCond, filter]
    }
}

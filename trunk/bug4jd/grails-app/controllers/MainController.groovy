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



import org.bug4j.App

class MainController {
    def bugService
    def statsService

    def index() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        final apps = App.list()
        return [
                apps: apps
        ]
    }

    def export() {
        response.setContentType("text/xml")
        bugService.export(response.outputStream)
    }

    def testImport() {
        try {
            final zipFile = new File('D:/bug4j/bugs.zip')
            bugService.importFile(zipFile)
            render(text: 'Imported', contentType: 'text/plain')
        } catch (Exception e) {
            log.error("Failed to import file", e)
            render(text: 'Failed', contentType: 'text/plain')
        }
    }

    def testImport2() {
        try {
            final zipFile = new File('C:/Users/dandoy/Downloads/bug4j/Discovery Manager.xml')
            bugService.importFile(zipFile)
            render(text: 'Imported', contentType: 'text/plain')
        } catch (Exception e) {
            log.error("Failed to import file", e)
            render(text: 'Failed', contentType: 'text/plain')
        }
    }

    def generateStats() {
        try {
            statsService.generateStats(true)
            render(text: 'Statistics generated', contentType: 'text/plain')
        } catch (Exception e) {
            log.error("Failed to generate the statistics", e)
            render(text: 'Failed', contentType: 'text/plain')
        }
    }

    def deleteDemoBugs() {
        try {
            final app = App.findByCode('bug4jDemo')
            app.bugs*.delete()
            app.bugs.clear()
            render text: 'Bugs deleted'
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    def generateTestData() {
        try {
            final t0 = System.currentTimeMillis()
            final nbrReports = 100
            def exceptionTexts = (1..10).collect {createRandomStackTrace()}
            def remoteAddresses = (1..10).collect {'192.168.0.' + it}
            def nowInMs = System.currentTimeMillis()
            def reportDates = (0..60).collect {nowInMs - (1000L * 60 * 60 * 24 * it)}

            final r = new Random()
            (1..nbrReports).each {
                final buildDate = System.currentTimeMillis()
                final exceptionText = exceptionTexts[r.nextInt(exceptionTexts.size())]
                final remoteAddress = remoteAddresses[r.nextInt(remoteAddresses.size())]
                final reportDate = reportDates[r.nextInt(reportDates.size())]
                final clientSession = bugService.createSession('bug4jDemo', '1.0', buildDate, 'N', 123, remoteAddress)
                bugService.reportBug(clientSession.id, 'bug4jDemo', 'test', reportDate, 'cdandoy', remoteAddress, exceptionText)
            }
            final t1 = System.currentTimeMillis()
            render(text: "Generated ${nbrReports} reports in ${(t1 - t0) / 1000}s", contentType: 'text/plain')

        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private static String createRandomStackTrace() {
        final random = new Random()
        final randomPart = (1..10).collect {
            "\n\tat org.bug4j.test.t(test.java:${random.nextInt(100) + 10})"
        }.join('')
        String ret = "Exception in thread \"Thread-${random.nextInt(100)}\" java.lang.NullPointerException\n" +
                "\tat java.lang.System.arraycopy(Native Method)" +
                randomPart +
                '\n\tat org.bug4j.main(test.java:10)'
        return ret
    }
}

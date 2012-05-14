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
}

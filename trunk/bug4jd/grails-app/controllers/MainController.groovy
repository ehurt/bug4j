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



import org.bug4j.Application

class MainController {
    def bugService

    def index() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        final applications = Application.list()
        return [
                applications: applications
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
            e.printStackTrace()
        }
    }

    def testImport2() {
        try {
            try {
                final zipFile = new File('C:/Users/dandoy/Downloads/bug4j/Discovery Manager.xml')
                bugService.importFile(zipFile)
                render(text: 'Imported', contentType: 'text/plain')
            } catch (Exception e) {
                log.error("Failed to import file", e)
                render(text: 'Failed', contentType: 'text/plain')
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}

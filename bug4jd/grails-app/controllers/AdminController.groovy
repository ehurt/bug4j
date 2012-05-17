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

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AdminController {

    def bugService

    def index() {
        final apps = App.list()
        return [
                apps: apps,
        ]
    }

    def doimp() {
        final file = request.getFile("file")
        final fileItem = file.fileItem
        final fileName = fileItem.getName()
        final inputStream = file.inputStream
        bugService.importNamedInputStream(inputStream, fileName)
        flash.message = 'Imported'
        redirect(action: 'index')
    }

    def exp() {
        response.addHeader('Content-Type', 'text/xml')
        response.addHeader('Content-Disposition', 'attachment; filename="bugs.zip"');
        final OutputStream outputStream = response.outputStream
        final zipOutputStream = new ZipOutputStream(outputStream)
        zipOutputStream.putNextEntry(new ZipEntry('bugs.xml'))
        zipOutputStream.write("Sorry, export is not available yet".bytes)
        zipOutputStream.closeEntry()
        zipOutputStream.close()
    }
}

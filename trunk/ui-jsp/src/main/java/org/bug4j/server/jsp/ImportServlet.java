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

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ImportServlet.class);

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ServletFileUpload upload = new ServletFileUpload();

        try {
            final FileItemIterator iter = upload.getItemIterator(request);

            while (iter.hasNext()) {
                final FileItemStream item = iter.next();

                final Store store = StoreFactory.getStore();
                final InjectImporter injectImporter = new InjectImporter(store);

                final String name = item.getName();
                final String extension = FilenameUtils.getExtension(name);
                final InputStream inputStream = item.openStream();
                try {
                    if ("xml".equals(extension)) {
                        injectImporter.importFile(inputStream);
                    } else if ("zip".equals(extension)) {
                        final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                        try {
                            final ZipEntry zipEntry = zipInputStream.getNextEntry();
                            if (!zipEntry.isDirectory()) {
                                final String zipEntryName = zipEntry.getName();
                                final String zipExt = FilenameUtils.getExtension(zipEntryName);
                                if ("xml".equals(zipExt)) {
                                    injectImporter.importFile(zipInputStream);
                                }
                            }
                        } finally {
                            zipInputStream.close();
                        }
                    }
                } finally {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}

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

import org.bug4j.gwt.user.client.data.Bug;
import org.bug4j.gwt.user.client.data.BugHit;
import org.bug4j.gwt.user.client.data.Filter;
import org.bug4j.server.processor.BugProcessor;
import org.bug4j.server.store.TestingStore;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ImportExportTest {

    private static final String APP = "tst";

    @Test
    public void testImportExport() throws Exception {

        final byte[] bytes;
        { // Create one bug and export
            final TestingStore store = TestingStore.createMemStore();

            final long now = System.currentTimeMillis();
            final long sessionId = store.createSession("tst", "1.0", now, "127.0.0.1", now, true, 1);
            BugProcessor.process(store, sessionId, "tst", "This is a mistake", now, "nobody", "" +
                    "java.lang.IllegalStateException: oh oh!\n" +
                    "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:100)\n" +
                    "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:200)\n" +
                    "Caused by: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0\n" +
                    "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:300)\n" +
                    "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:400)\n" +
                    "\t... 22 more");


            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                final Exporter exporter = new Exporter(store);
                exporter.exportAll(outputStream);
                bytes = outputStream.toByteArray();
            } finally {
                outputStream.close();
            }
        }

        { // Reimport the bug in a new store
            final TestingStore store = TestingStore.createMemStore();
            final Importer importer = new InjectImporter(store);
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try {
                importer.importFile(byteArrayInputStream);
            } finally {
                byteArrayInputStream.close();
            }

            final List<Bug> bugs = store.getBugs("", APP, new Filter(), 0, 100, "");
            Assert.assertEquals(1, bugs.size());
            final Bug bug = bugs.get(0);
            final List<BugHit> hits = store.getHits(bug.getId(), 0, 100, "");
            Assert.assertEquals(1, hits.size());
            System.out.println(hits.get(0));
        }
    }
}

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

import org.bug4j.server.store.TestingStore;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class ExporterTest {

    private TestingStore _store;

    @Before
    public void setUp() throws Exception {
        _store = TestingStore.createEmbeddedStore();
    }

    @Test
    public void testExportAll() throws Exception {
//        final FileOutputStream outputStream = new FileOutputStream("C:\\temp\\export.xml");
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            final Exporter exporter = new Exporter(_store);
            exporter.exportAll(outputStream);
        } finally {
            outputStream.close();
        }
    }
}

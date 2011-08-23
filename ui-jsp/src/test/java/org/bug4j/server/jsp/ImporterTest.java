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

import org.bug4j.server.store.StoreFactory;
import org.bug4j.server.store.TestingStore;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImporterTest {

    private TestingStore _store;

    @Before
    public void setUp() throws Exception {
        _store = TestingStore.createMemStore();
        StoreFactory.setStore(_store);
    }

    @Test
    public void testReinject() throws IOException, SAXException, ParserConfigurationException {
        final Importer importer = new InjectImporter(_store);
        final ClassLoader classLoader = getClass().getClassLoader();
        final InputStream inputStream = classLoader.getResourceAsStream("import/bugs.xml");
        try {
            final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            try {
                importer.importFile(bufferedInputStream);
            } finally {
                bufferedInputStream.close();
            }
        } finally {
            inputStream.close();
        }
    }
}

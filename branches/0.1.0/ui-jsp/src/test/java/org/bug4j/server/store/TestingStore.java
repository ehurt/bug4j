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

package org.bug4j.server.store;

import org.bug4j.server.store.jdbc.JdbcStore;
import org.junit.Assert;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestingStore extends JdbcStore {
    public static final String MEM_URL = "jdbc:derby:memory:tst;create=true";
    private String _url;

    private TestingStore(String url) {
        _url = url;
        initialize();
    }

    public static TestingStore createMemStore() {
        return new TestingStore(MEM_URL);
    }

    public static TestingStore createEmbeddedStore() {
        final File databasePath = getDatabasePath();
        Assert.assertNotNull("Could not find the database, started from " + new File(".").getAbsolutePath(), databasePath);
        //noinspection ConstantConditions
        return new TestingStore("jdbc:derby:" + databasePath.getAbsolutePath());
    }

    private static File getDatabasePath() {
        File base = new File(".").getAbsoluteFile();
        while (base != null) {
            final File file = new File(base, "out/test/ui-jsp/bug4jdb");
            if (file.exists()) {
                return file;
            }
            base = base.getParentFile();
        }
        return null;
    }

    @Override
    protected Connection getConnection() {
        final Connection ret;
        try {
            ret = DriverManager.getConnection(_url);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return ret;
    }
}

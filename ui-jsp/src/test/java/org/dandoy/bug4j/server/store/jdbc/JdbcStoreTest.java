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

package org.dandoy.bug4j.server.store.jdbc;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcStoreTest {
    static class TestingStore extends JdbcStore {

        @Override
        protected Connection getConnection() {
            final Connection ret;
            try {
                ret = DriverManager.getConnection("jdbc:derby:C:\\Java\\apache-tomcat-7.0.12\\bin\\bug4j;create=true");
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return ret;
        }
    }

    @Test
    public void testSomethign() throws Exception {
        final TestingStore testingStore = new TestingStore();
        testingStore.getTopHits("My Application", 5, 5);
        System.out.println("JdbcStoreTest.testSomethign");
    }
}

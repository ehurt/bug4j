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

package org.bug4j.server.migration;

import org.apache.commons.io.FileUtils;
import org.bug4j.server.store.jdbc.JdbcStore;

import java.io.File;
import java.io.IOException;

public class Migrator {
    private static Migrator INSTANCE;

    private Migrator() {
    }

    public static synchronized Migrator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Migrator();
        }
        return INSTANCE;
    }

    public void preOpenDB() {
        migrateDatabaseLocation();
    }

    public void postOpenDB() {
        addHitSession();
    }

    /**
     * 2011-08-14 Database used to be stored in the default location wich would usually end-up in .../server/bin/bug4j.
     * The database has now been moved to .../server/bug4jdb
     */
    private void migrateDatabaseLocation() {
        final String catalinaHome = System.getProperty("catalina.home", null);
        final File oldFile = new File(catalinaHome, "bin/bug4j");
        if (oldFile.isDirectory()) {
            final File newFile = new File(catalinaHome, "bug4jdb");
            try {
                FileUtils.moveDirectory(oldFile, newFile);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    /**
     * 2011-09-10 Added the SESSION_ID to the HIT table
     */
    private void addHitSession() {
        final JdbcStore jdbcStore = JdbcStore.getInstance();
        jdbcStore.migrate_addHitSession();
    }
}

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
package org.bug4j.server

import groovy.sql.Sql
import org.bug4j.AppSettings
import org.bug4j.Bug
import org.bug4j.Comment
import org.bug4j.MergePattern
import org.bug4j.server.util.StringUtil

import java.sql.SQLException
import javax.sql.DataSource

class AppService {
    public static final String DBVERSION = 'DBVERSION'
    private static final int CURRENT_VERSION = 2

    DataSource dataSource

    int getDbVersion() {
        def appSettings = AppSettings.findByKey(DBVERSION)
        if (!appSettings) {
            return 0
        } else {
            return appSettings.value as int
        }

    }

    def setDbVersion() {
        def appSettings = AppSettings.findByKey(DBVERSION)
        if (!appSettings) {
            appSettings = new AppSettings(key: DBVERSION)
        }
        appSettings.value = CURRENT_VERSION
        appSettings.save()
    }

    def upgradeDb() {
        //noinspection GroovyFallthrough
        switch (getDbVersion()) {
            case 0:
                upgradeFrom0();
            case 1:
                upgradeFrom1();
                setDbVersion();
            case CURRENT_VERSION:
                break;
        }
    }

    private def upgradeFrom0() {

        final sql = new Sql(dataSource)
        try {
            sql.eachRow("select BUG_ID, ADDED_BY, DATE_ADDED, TEXT from COMMENT ORDER BY BUG_ID, ID") {
                final bugId = it[0]
                final addedBy = it[1]
                final dateAdded = it[2]
                final text = it[3]
                final bug = Bug.get(bugId)
                final comment = new Comment(text: text, dateAdded: dateAdded, addedBy: addedBy)
                comment.bug = bug
                bug.addToComments(comment)
                comment.save()
            }
            sql.execute("drop table comment")
        } catch (SQLException ignore) {
        } finally {
            sql.close()
        }

        Bug.list().each {
            final title = it.title
            String newTitle = StringUtil.fixTitle(title)
            if (!title.equals(newTitle)) {
                it.title = newTitle
                it.save();
            }
        }
    }

    private def upgradeFrom1() {
        final sql = new Sql(dataSource)

        try {
            // Bug title size increased

            try { // Derby syntax
                sql.execute((String) "ALTER TABLE BUG ALTER COLUMN TITLE SET DATA TYPE VARCHAR(${Bug.TITLE_SIZE})")
            } catch (SQLException ignore) {
                ignore.printStackTrace()
            }

            try { // Oracle syntax
                sql.execute((String) "ALTER TABLE BUG MODIFY ( TITLE VARCHAR2(${Bug.TITLE_SIZE})")
            } catch (SQLException ignore) {
            }

            // Pattern size increased to match bug size
            try { // Derby syntax
                sql.execute((String) "ALTER TABLE MERGE_PATTERN ALTER COLUMN PATTERN_STRING SET DATA TYPE VARCHAR(${MergePattern.PATTERN_SIZE})")
            } catch (SQLException ignore) {
                ignore.printStackTrace()
            }

            try { // Oracle syntax
                sql.execute((String) "ALTER TABLE MERGE_PATTERN MODIFY ( PATTERN_STRING VARCHAR2(${MergePattern.PATTERN_SIZE})")
            } catch (SQLException ignore) {
            }

        } finally {
            sql.close()
        }
    }
}

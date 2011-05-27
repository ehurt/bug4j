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

import org.apache.commons.dbutils.DbUtils;
import org.dandoy.bug4j.server.gwt.client.data.Bug;
import org.dandoy.bug4j.server.gwt.client.data.BugDetail;
import org.dandoy.bug4j.server.store.Store;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcStore extends Store {
    private static JdbcStore INSTANCE;
    private final Set<String> _knownApps = new HashSet<String>();

    private JdbcStore() {
    }

    private void initialize() {
        final Connection connection = getConnection();
        try {
            createTables(connection);
            loadApps(connection);
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    public static synchronized JdbcStore getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JdbcStore();
            INSTANCE.initialize();
        }
        return INSTANCE;
    }

    private Connection getConnection() {
        final Connection ret;
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            final DataSource bugDB = (DataSource) envCtx.lookup("jdbc/bugDB");
            ret = bugDB.getConnection();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return ret;
    }

    private static void createTables(Connection connection) throws SQLException {
        final Statement statement = connection.createStatement();

        try {
            if (!doesTableExist(statement, "APP")) {
                statement.execute("" +
                        "CREATE TABLE APP (" +
                        " APP VARCHAR(32)," +
                        " VER VARCHAR(32)" +
                        ")"
                );
            }

            if (!doesTableExist(statement, "APP_PACKAGES")) {
                statement.execute("" +
                        "CREATE TABLE APP_PACKAGES (" +
                        " APP VARCHAR(32)," +
                        " APP_PACKAGE VARCHAR(64)" +
                        ")"
                );
            }

            if (!doesTableExist(statement, "BUG")) {
                statement.execute("" +
                        "CREATE TABLE BUG (" +
                        " ID INT GENERATED ALWAYS AS IDENTITY," +
                        " HASH VARCHAR(64)," +
                        " APP VARCHAR(32)," +
                        " TITLE VARCHAR(256)," +
                        " MESSAGE VARCHAR(256)," +
                        " EXCEPTION_MESSAGE VARCHAR(256)," +
                        " STACK_TEXT CLOB(16 K)" +
                        ")"
                );
                statement.execute("CREATE UNIQUE INDEX BUG_HASH ON BUG (APP, HASH)");
            }

            if (!doesTableExist(statement, "HIT")) {
                statement.execute("" +
                        "CREATE TABLE HIT (" +
                        " ID INT GENERATED ALWAYS AS IDENTITY," +
                        " BUG_ID INT," +
                        " APP_VER VARCHAR(32)," +
                        " DATE_REPORTED TIMESTAMP" +
                        ")"
                );
            }
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private static boolean doesTableExist(Statement statement, String tableName) {
        try {
            statement.executeQuery("SELECT 1 FROM " + tableName + " WHERE 1 = 2");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private void loadApps(Connection connection) throws SQLException {
        final Statement statement = connection.createStatement();
        try {
            final ResultSet resultSet = statement.executeQuery("select * from APP");
            try {
                while (resultSet.next()) {
                    final String app = resultSet.getString(1);
                    final String ver = resultSet.getString(2);
                    final String key = app + '\0' + ver;
                    _knownApps.add(key);
                }
            } finally {
                DbUtils.closeQuietly(resultSet);
            }
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private void createApp(Connection connection, String app, String ver) {
        final String key = app + '\0' + ver;
        final boolean isNew;
        synchronized (JdbcStore.class) {
            isNew = _knownApps.add(key);
        }
        if (isNew) {
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO APP (APP, VER) VALUES(?,?)");
                try {
                    preparedStatement.setString(1, app);
                    preparedStatement.setString(2, ver);
                    preparedStatement.execute();
                } finally {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    @Override
    public long find(String app, String hash) {
        long ret = -1;
        try {
            final Connection connection = getConnection();
            try {
                final PreparedStatement existsStatement = connection.prepareStatement("SELECT ID FROM BUG WHERE APP=? AND HASH=?");
                existsStatement.setString(1, app);
                existsStatement.setString(2, hash);
                final ResultSet resultSet = existsStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        ret = resultSet.getLong(1);
                    }
                } finally {
                    DbUtils.closeQuietly(resultSet);
                }
            } finally {
                DbUtils.closeQuietly(connection);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return ret;
    }

    @Override
    public long report(String app, String ver, String hash, String title, String message, String exceptionMessage, String stackText) {
        final Connection connection = getConnection();
        try {
            createApp(connection, app, ver);
            final PreparedStatement insertBugStatement = connection.prepareStatement("INSERT INTO BUG (HASH, APP, TITLE, MESSAGE, EXCEPTION_MESSAGE, STACK_TEXT) VALUES (?,?,?,?,?,?)");
            try {
                insertBugStatement.setString(1, hash);
                insertBugStatement.setString(2, app);
                insertBugStatement.setString(3, title);
                insertBugStatement.setString(4, message);
                insertBugStatement.setString(5, exceptionMessage);
                insertBugStatement.setString(6, stackText);
                insertBugStatement.execute();
                final long bugid = find(app, hash);
                reportHit(bugid, ver);
                return bugid;
            } finally {
                DbUtils.closeQuietly(insertBugStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public void reportHit(long bugId, String version) {
        final Connection connection = getConnection();
        try {
            final PreparedStatement insertHitStatement = connection.prepareStatement("INSERT INTO HIT (BUG_ID,APP_VER,DATE_REPORTED) VALUES(?,?,CURRENT_TIMESTAMP)");
            try {
                insertHitStatement.setLong(1, bugId);
                insertHitStatement.setString(2, version);
                insertHitStatement.execute();
            } finally {
                DbUtils.closeQuietly(insertHitStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<Bug> getBugs(String app, int offset, int max, String orderBy) {
        final List<Bug> ret = new ArrayList<Bug>();
        final Connection connection = getConnection();
        try {
            StringBuilder sql = new StringBuilder("select bug.id,bug.title,(select count(*) from hit where hit.bug_id=bug.id) hitcount from bug\n");
            String sep = "order by ";
            for (int i = 0; i < orderBy.length(); i++) {
                final char c = orderBy.charAt(i);
                final char lc = Character.toLowerCase(c);
                final int columnPos = "ith".indexOf(lc) + 1;
                final boolean asc = Character.isLowerCase(c);
                sql
                        .append(sep)
                        .append(columnPos)
                        .append(asc ? " ASC" : " DESC");
                sep = ", ";
            }
            final PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
            try {
                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    while (offset > 0 && resultSet.next()) {
                        offset--;
                    }
                    while (max > 0 && resultSet.next()) {
                        max--;
                        final long bugId = resultSet.getLong(1);
                        final String title = resultSet.getString(2);
                        final int hitCount = resultSet.getInt(3);
                        final Bug bug = new Bug(bugId, title, hitCount);
                        ret.add(bug);
                    }
                } finally {
                    DbUtils.closeQuietly(resultSet);
                }
            } finally {
                DbUtils.closeQuietly(preparedStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
        return ret;
    }

    @Override
    public BugDetail getBug(long bugId) {
        BugDetail ret = null;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "select BUG.TITLE," +
                    "       (select count(*) from hit where hit.bug_id=?)," +
                    "       BUG.MESSAGE," +
                    "       BUG.EXCEPTION_MESSAGE," +
                    "       BUG.STACK_TEXT" +
                    "  from BUG" +
                    "  where BUG.ID=?");
            try {
                preparedStatement.setLong(1, bugId);
                preparedStatement.setLong(2, bugId);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    final String title = resultSet.getString(1);
                    final int hitCount = resultSet.getInt(2);
                    final String message = resultSet.getString(3);
                    final String exceptionMessage = resultSet.getString(4);
                    final String stackText = resultSet.getString(5);
                    ret = new BugDetail(bugId, title, hitCount, message, exceptionMessage, stackText);
                }
            } finally {
                DbUtils.closeQuietly(preparedStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
        return ret;
    }

    @Override
    public List<String> getPackages(String app) {
        final List<String> ret = new ArrayList<String>();
        final Connection connection = getConnection();
        try {
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("SELECT APP_PACKAGE FROM APP_PACKAGES WHERE APP=? ORDER BY APP_PACKAGE");
                try {
                    preparedStatement.setString(1, app);
                    final ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            final String appPackage = resultSet.getString(1);
                            ret.add(appPackage);
                        }
                    } finally {
                        DbUtils.closeQuietly(resultSet);
                    }
                } finally {
                    DbUtils.close(preparedStatement);
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        } finally {
            DbUtils.closeQuietly(connection);
        }
        return ret;
    }

    @Override
    public void setPackages(String app, List<String> appPackages) {
        final Connection connection = getConnection();
        try {
            final PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM APP_PACKAGES WHERE APP=?");
            try {
                deleteStatement.setString(1, app);
                deleteStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(deleteStatement);
            }
            final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO APP_PACKAGES (APP, APP_PACKAGE) VALUES (?, ?)");
            try {
                insertStatement.setString(1, app);
                for (String appPackage : appPackages) {
                    insertStatement.setString(2, appPackage);
                }
            } finally {
                DbUtils.closeQuietly(insertStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void addPackage(String app, String appPackage) {
        final Connection connection = getConnection();
        try {
            final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO APP_PACKAGES (APP, APP_PACKAGE) VALUES (?, ?)");
            try {
                insertStatement.setString(1, app);
                insertStatement.setString(2, appPackage);
                insertStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(insertStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public void deletePackage(String app, String appPackage) {
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM APP_PACKAGES WHERE APP=? AND APP_PACKAGE=?");
            try {
                preparedStatement.setString(1, app);
                preparedStatement.setString(2, appPackage);
                preparedStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(preparedStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }
}

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

package org.bug4j.server.store.jdbc;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.bug4j.gwt.user.client.data.*;
import org.bug4j.gwt.user.client.data.Stack;
import org.bug4j.server.store.Store;
import org.jetbrains.annotations.Nullable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class JdbcStore extends Store {
    private static JdbcStore INSTANCE;
    private static final int STACK_SIZE_LIMIT = 128 * 1024;
    private static final String PREF_DEFAULT_APP = "DEFAULT_APP";
    private final Set<String> _knownApps = new HashSet<String>();
    private static final long DAY = 1000L * 60 * 60 * 24;

    protected JdbcStore() {
    }

    protected void initialize() {
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

    protected Connection getConnection() {
        final Connection ret;
        try {
            final Context initCtx = new InitialContext();
            final Context envCtx = (Context) initCtx.lookup("java:comp/env");
            final DataSource bugDB = (DataSource) envCtx.lookup("jdbc/bugDB");
            ret = bugDB.getConnection();
            ret.setAutoCommit(true);
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
                        " APP VARCHAR(32) NOT NULL," +
                        " VER VARCHAR(32)" +
                        ")"
                );
            } else {
                // TODO: Delete this
                statement.execute("update APP set APP='Discovery Manager' where APP='My Application'");
            }

            if (!doesTableExist(statement, "APP_PACKAGES")) {
                statement.execute("" +
                        "CREATE TABLE APP_PACKAGES (" +
                        " APP VARCHAR(32) NOT NULL," +
                        " APP_PACKAGE VARCHAR(64) NOT NULL" +
                        ")"
                );
                statement.execute("INSERT INTO APP_PACKAGES (APP,APP_PACKAGE)VALUES('bug4jDemo','org.bug4j.demo')");
            } else { // TODO: Delete this
                statement.execute("update APP_PACKAGES set APP='Discovery Manager' where APP='My Application'");
            }

            if (!doesTableExist(statement, "BUG")) {
                statement.execute("" +
                        "CREATE TABLE BUG (" +
                        " BUG_ID INT GENERATED ALWAYS AS IDENTITY," +
                        " APP VARCHAR(32) NOT NULL," +
                        " TITLE VARCHAR(256) NOT NULL" +
                        ")"
                );
                statement.execute("CREATE INDEX BUG_ID_IDX ON BUG(BUG_ID)");
                statement.execute("CREATE INDEX BUG_TITLE_IDX ON BUG(TITLE)");
            } else { // TODO: Delete this
                statement.execute("update BUG set APP='Discovery Manager' where APP='My Application'");
            }

            if (!doesTableExist(statement, "STRAIN")) {
                statement.execute("" +
                        "CREATE TABLE STRAIN (" +
                        " STRAIN_ID INT GENERATED ALWAYS AS IDENTITY," +
                        " BUG_ID INT," +
                        " HASH VARCHAR(64)" +
                        ")"
                );
                statement.execute("CREATE INDEX STRAIN_HASH_IDX ON STRAIN(HASH)");
            }

            if (!doesTableExist(statement, "STACK")) {
                statement.execute("" +
                        "CREATE TABLE STACK (" +
                        " STACK_ID INT GENERATED ALWAYS AS IDENTITY," +
                        " BUG_ID INT," +
                        " STRAIN_ID INT," +
                        " HASH VARCHAR(64)" +
                        ")"
                );
                statement.execute("CREATE INDEX STACK_HASH_IDX ON STACK(HASH)");
                statement.execute("CREATE INDEX STACK_BUGID_IDX ON STACK(BUG_ID)");
            }

            if (!doesTableExist(statement, "STACK_TEXT")) {
                statement.execute("" +
                        "CREATE TABLE STACK_TEXT (" +
                        " STACK_ID INT," +
                        " STACK_TEXT CLOB(" + STACK_SIZE_LIMIT + ")" +
                        ")"
                );
                statement.execute("CREATE INDEX STACK_TEXT_ID_IDX ON STACK_TEXT(STACK_ID)");
            }

            if (!doesTableExist(statement, "HIT")) {
                statement.execute("" +
                        "CREATE TABLE HIT (" +
                        " HIT_ID INT GENERATED ALWAYS AS IDENTITY," +
                        " BUG_ID INT," +
                        " STACK_ID INT," +
                        " APP_VER VARCHAR(32)," +
                        " DATE_REPORTED TIMESTAMP," +
                        " REPORTED_BY VARCHAR(1024)," +
                        " MESSAGE VARCHAR(1024)" +
                        ")"
                );
                statement.execute("CREATE INDEX HIT_HIT_ID_IDX ON HIT(HIT_ID)");
                statement.execute("CREATE INDEX HIT_BUG_ID_IDX ON HIT(BUG_ID)");
            } else {
                try {
                    final ResultSet resultSet = statement.executeQuery("SELECT REPORTED_BY FROM HIT WHERE 1=2");
                    resultSet.close();
                } catch (SQLException e) {
                    statement.execute("ALTER TABLE HIT ADD COLUMN REPORTED_BY VARCHAR(1024)");
                }
            }

            if (!doesTableExist(statement, "USER_PREFS")) {
                statement.execute("" +
                        "CREATE TABLE USER_PREFS (" +
                        " USER_NAME VARCHAR(255)," +
                        " PREF_KEY  VARCHAR(1024)," +
                        " PREF_VALUE VARCHAR(1024)" +
                        ")"
                );
                statement.execute("CREATE INDEX USER_NAME_IDX ON USER_PREFS(USER_NAME,PREF_KEY)");
            } else {
                // TODO: Delete this
                statement.execute("update USER_PREFS set PREF_VALUE='Discovery Manager' WHERE PREF_KEY='DEFAULT_APP' AND PREF_VALUE='My Application'");
            }

            if (!doesTableExist(statement, "USER_READ")) {
                statement.execute("" +
                        "CREATE TABLE USER_READ (" +
                        " USER_NAME VARCHAR(255)," +
                        " BUG_ID INT," +
                        " LAST_HIT_ID INT" +
                        ")"
                );
                statement.execute("CREATE INDEX USER_READ_IDX ON USER_READ(USER_NAME,BUG_ID)");
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

    private static String truncate(String s, int length) {
        if (s != null) {
            if (length < s.length()) {
                s = s.substring(0, length);
            }
        }
        return s;
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

    private void declareApp(Connection connection, String app, String ver) {
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

    /**
     * Get a set of bugs
     *
     * @param orderBy can be a combination of i(d), t(itle) or h(its)
     * @return
     */
    @Override
    public List<Bug> getBugs(String userName, String app, Filter filter, int offset, int max, String orderBy) {
        final List<Bug> ret = new ArrayList<Bug>();
        final Connection connection = getConnection();
        try {
            StringBuilder sql = new StringBuilder("" +
                    "SELECT b.bug_id," +
                    "        b.title," +
                    "        COUNT(h.hit_id) AS hit_count," +
                    "        MAX(h.hit_id) AS hit_max," +
                    "        ur.last_hit_id" +
                    "  FROM bug b" +
                    "    LEFT OUTER JOIN hit h ON b.bug_id = h.bug_id" +
                    "    LEFT OUTER JOIN user_read ur ON b.bug_id = ur.bug_id AND ur.user_name=:userName" +
                    "  WHERE b.app = :app");

            if (filter.hasHitWithinDays()) {
                sql.append(" AND h.date_reported > :hitDateMin");
            }
            if (filter.hasTitle()) {
                sql.append(" AND upper(b.title) like :titleFilter");
            }

            sql.append("  GROUP BY b.bug_id, b.title,ur.last_hit_id");

            sql.append("  HAVING COUNT(h.hit_id) > 0");
            if (filter.isReportedByMultiple()) {
                sql.append(" AND");
                sql.append(" COUNT(distinct h.reported_by) > 1");
            }

            String orderBySep = " order by ";
            for (int i = 0; i < orderBy.length(); i++) {
                final char c = orderBy.charAt(i);
                final char lc = Character.toLowerCase(c);
                final int columnPos = "ith".indexOf(lc) + 1;
                final boolean asc = Character.isLowerCase(c);
                sql
                        .append(orderBySep)
                        .append(columnPos)
                        .append(asc ? " ASC" : " DESC");
                orderBySep = ", ";
            }

            final NamedParameterProcessor namedParameterProcessor = new NamedParameterProcessor(sql.toString());

            final String jdbcSql = namedParameterProcessor.getJdbcSql();
            final PreparedStatement preparedStatement = connection.prepareStatement(jdbcSql);
            namedParameterProcessor.setParameter(preparedStatement, "app", app);
            namedParameterProcessor.setParameter(preparedStatement, "userName", userName);
            if (filter.hasHitWithinDays()) {
                final Integer hitWithinDays = filter.getHitWithinDays();
                final Timestamp timestamp = getPrevDaysTimestamp(hitWithinDays);
                namedParameterProcessor.setParameter(preparedStatement, "hitDateMin", timestamp);
            }
            if (filter.hasTitle()) {
                namedParameterProcessor.setParameter(preparedStatement, "titleFilter", "%" + filter.getTitle().toUpperCase() + "%");
            }
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
                        final long maxHit = resultSet.getLong(4);
                        Long lastReadHit = resultSet.getLong(5);
                        if (resultSet.wasNull()) {
                            lastReadHit = null;
                        }
                        final Bug bug = new Bug(bugId, title, hitCount, maxHit, lastReadHit);
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
            {
                final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM APP_PACKAGES WHERE APP=?");
                try {
                    preparedStatement.setString(1, app);
                    preparedStatement.executeUpdate();
                } finally {
                    DbUtils.closeQuietly(preparedStatement);
                }
            }

            {
                final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO APP_PACKAGES (APP, APP_PACKAGE) VALUES (?, ?)");
                try {
                    insertStatement.setString(1, app);
                    for (String appPackage : appPackages) {
                        insertStatement.setString(2, appPackage);
                        insertStatement.addBatch();
                    }
                    insertStatement.executeBatch();
                } finally {
                    DbUtils.closeQuietly(insertStatement);
                }
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
    public List<BugHit> getHits(long bugId, @Nullable Filter filter, int offset, int max, String orderBy) {
        final List<BugHit> ret = new ArrayList<BugHit>();
        final Connection connection = getConnection();
        try {
            StringBuilder sql = new StringBuilder("select H.HIT_ID,H.APP_VER,H.DATE_REPORTED,H.REPORTED_BY from HIT H WHERE H.BUG_ID=:bugId\n");
            final boolean hasHitWithinDays = filter != null && filter.hasHitWithinDays();
            if (hasHitWithinDays) {
                sql.append(" AND H.DATE_REPORTED > :hitDateMin");
            }
            String sep = " order by ";
            for (int i = 0; i < orderBy.length(); i++) {
                final char c = orderBy.charAt(i);
                final char lc = Character.toLowerCase(c);
                final int columnPos = "ivdb".indexOf(lc) + 1;
                final boolean asc = Character.isLowerCase(c);
                sql
                        .append(sep)
                        .append(columnPos)
                        .append(asc ? " ASC" : " DESC");
                sep = ", ";
            }
            final NamedParameterProcessor namedParameterProcessor = new NamedParameterProcessor(sql.toString());
            final String jdbcSql = namedParameterProcessor.getJdbcSql();
            final PreparedStatement preparedStatement = connection.prepareStatement(jdbcSql);

            namedParameterProcessor.setParameter(preparedStatement, "bugId", bugId);
            if (hasHitWithinDays) {
                final Integer hitWithinDays = filter.getHitWithinDays();
                final Timestamp timestamp = getPrevDaysTimestamp(hitWithinDays);
                namedParameterProcessor.setParameter(preparedStatement, "hitDateMin", timestamp);
            }
            try {
                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    while (offset > 0 && resultSet.next()) {
                        offset--;
                    }
                    while (max > 0 && resultSet.next()) {
                        max--;
                        final long hitId = resultSet.getLong(1);
                        final String appVer = resultSet.getString(2);
                        final long dateReported = resultSet.getTimestamp(3).getTime();
                        final String user = resultSet.getString(4);
                        final BugHit bugHit = new BugHit(
                                hitId,
                                appVer,
                                dateReported,
                                user
                        );
                        ret.add(bugHit);
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

    private static Timestamp getPrevDaysTimestamp(int days) {
        final Timestamp ret;

        final Calendar from = Calendar.getInstance();
        from.add(Calendar.DAY_OF_MONTH, -days);
        from.set(Calendar.HOUR_OF_DAY, 23);
        from.set(Calendar.MINUTE, 59);
        from.set(Calendar.SECOND, 59);
        from.set(Calendar.MILLISECOND, 999);
        final long timeInMillis = from.getTimeInMillis();
        ret = new Timestamp(timeInMillis);

        return ret;
    }

    @Override
    public List<Long> getBugIdByTitle(String app, String title) {
        final List<Long> ret = new ArrayList<Long>();
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("select BUG_ID FROM BUG WHERE APP=? AND TITLE=?");
            try {
                preparedStatement.setString(1, app);
                preparedStatement.setString(2, title);

                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    while (resultSet.next()) {
                        ret.add(resultSet.getLong(1));
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
    public List<String> getApplications() {
        final List<String> ret = new ArrayList<String>();
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT DISTINCT APP FROM APP ORDER BY 1");
            try {
                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    while (resultSet.next()) {
                        ret.add(resultSet.getString(1));
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

    @Nullable
    public String getUserPref(String remoteUser, String key, @Nullable String defaultValue) {
        String ret = defaultValue;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT PREF_VALUE FROM USER_PREFS WHERE USER_NAME=? AND PREF_KEY=?");
            try {
                preparedStatement.setString(1, remoteUser);
                preparedStatement.setString(2, key);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    ret = resultSet.getString(1);
                }
            } finally {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
        return ret;
    }

    @Nullable
    public Integer getUserPref_Integer(String remoteUser, String key, @Nullable Integer defaultValue) {
        Integer ret = defaultValue;
        final String userPref = getUserPref(remoteUser, key, null);
        if (userPref != null) {
            try {
                ret = Integer.valueOf(userPref);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return ret;
    }

    public void setUserPref(String remoteUser, String key, String value) {
        final Connection connection = getConnection();
        try {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE USER_PREFS SET PREF_VALUE=? WHERE USER_NAME=? AND PREF_KEY=?");
            try {
                updateStatement.setString(1, value);
                updateStatement.setString(2, remoteUser);
                updateStatement.setString(3, key);
                final int updated = updateStatement.executeUpdate();
                if (updated == 0) {
                    final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO USER_PREFS(USER_NAME,PREF_KEY,PREF_VALUE) VALUES(?,?,?)");
                    try {
                        insertStatement.setString(1, remoteUser);
                        insertStatement.setString(2, key);
                        insertStatement.setString(3, value);
                        insertStatement.executeUpdate();
                    } finally {
                        insertStatement.close();
                    }
                }
            } finally {
                updateStatement.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    public void setUserPref(String remoteUser, String key, Integer value) {
        String stringValue = value == null ? null : value.toString();
        setUserPref(remoteUser, key, stringValue);
    }

    @Override
    public void setDefaultApplication(String remoteUser, String app) {
        setUserPref(remoteUser, PREF_DEFAULT_APP, app);
    }

    @Override
    @Nullable
    public String getDefaultApplication(String remoteUser) {
        String ret = getUserPref(remoteUser, PREF_DEFAULT_APP, null);
        if (ret == null) {
            ret = getAnyApp();
            if (ret != null) {
                setDefaultApplication(remoteUser, ret);
            }
        }
        return ret;
    }

    public Filter getDefaultFilter(String remoteUser) {
        final Filter ret = new Filter();
        final String title = getUserPref(remoteUser, "FILTER_TITLE", null);
        final Integer hitWithinDays = getUserPref_Integer(remoteUser, "FILTER_DAYS", 7);
        final String multiUsers = getUserPref(remoteUser, "FILTER_MULTI_USERS", Boolean.FALSE.toString());
        ret.setTitle(title);
        ret.setHitWithinDays(hitWithinDays);
        ret.setReportedByMultiple(Boolean.parseBoolean(multiUsers));
        return ret;
    }

    public void setDefaultFilter(String remoteUser, Filter filter) {
        setUserPref(remoteUser, "FILTER_TITLE", filter.getTitle());
        setUserPref(remoteUser, "FILTER_DAYS", filter.getHitWithinDays());
        setUserPref(remoteUser, "FILTER_MULTI_USERS", Boolean.toString(filter.isReportedByMultiple()));
    }

    @Override
    public void markRead(String userName, long bugId) {
        final Connection connection = getConnection();
        try {
            long lastHit = 0;
            final PreparedStatement selectStatement = connection.prepareStatement("SELECT MAX(HIT_ID) FROM HIT WHERE BUG_ID=?");
            try {
                selectStatement.setLong(1, bugId);
                final ResultSet resultSet = selectStatement.executeQuery();
                if (resultSet.next()) {
                    lastHit = resultSet.getLong(1);
                }
            } finally {
                selectStatement.close();
            }

            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE USER_READ SET LAST_HIT_ID=? WHERE USER_NAME=? AND BUG_ID=?");
            try {
                updateStatement.setLong(1, lastHit);
                updateStatement.setString(2, userName);
                updateStatement.setLong(3, bugId);
                final int updated = updateStatement.executeUpdate();
                if (updated == 0) {
                    final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO USER_READ(USER_NAME,BUG_ID,LAST_HIT_ID) VALUES (?,?,?)");
                    try {
                        insertStatement.setString(1, userName);
                        insertStatement.setLong(2, bugId);
                        insertStatement.setLong(3, lastHit);
                        insertStatement.executeUpdate();
                    } finally {
                        insertStatement.close();
                    }
                }
            } finally {
                updateStatement.close();
            }

        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<BugCountByDate> getBugCountByDate(String app) {
        final List<BugCountByDate> ret = new ArrayList<BugCountByDate>();
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "select date(h.date_reported),count(*)" +
                    "  from hit h, bug b" +
                    "  where h.bug_id=b.bug_id" +
                    "  and b.app=?" +
                    "  group by date(h.date_reported)" +
                    "  order by 1");
            try {
                preparedStatement.setString(1, app);
                final ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    final Date date = resultSet.getDate(1);
                    final int count = resultSet.getInt(2);
                    final long time = date.getTime();
                    ret.add(new BugCountByDate(time, count));
                }
            } finally {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
        return ret;
    }

    private String getAnyApp() {
        String ret = null;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT APP FROM BUG");
            try {
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    ret = resultSet.getString(1);
                }
            } finally {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
        return ret;
    }

    @Override
    public void deleteBug(long bugId) {
        final Connection connection = getConnection();
        try {
            final PreparedStatement deleteHitsStatement = connection.prepareStatement("DELETE FROM HIT WHERE BUG_ID=?");
            try {
                deleteHitsStatement.setLong(1, bugId);
                deleteHitsStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(deleteHitsStatement);
            }

            final PreparedStatement deleteStackTextStatement = connection.prepareStatement("DELETE FROM STACK_TEXT WHERE STACK_ID IN (select DISTINCT S.STACK_ID from STACK S WHERE BUG_ID=?)");
            try {
                deleteStackTextStatement.setLong(1, bugId);
                deleteStackTextStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(deleteStackTextStatement);
            }

            final PreparedStatement deleteStackStatement = connection.prepareStatement("DELETE FROM STACK WHERE BUG_ID=?");
            try {
                deleteStackStatement.setLong(1, bugId);
                deleteStackStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(deleteStackStatement);
            }

            final PreparedStatement deleteStrainStatement = connection.prepareStatement("DELETE FROM STRAIN WHERE BUG_ID=?");
            try {
                deleteStrainStatement.setLong(1, bugId);
                deleteStrainStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(deleteStrainStatement);
            }

            final PreparedStatement deleteBugStatement = connection.prepareStatement("DELETE FROM BUG WHERE BUG_ID=?");
            try {
                deleteBugStatement.setLong(1, bugId);
                deleteBugStatement.executeUpdate();
            } finally {
                DbUtils.closeQuietly(deleteBugStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public Map<Bug, int[]> getTopHits(String app, int daysBack, int max) {
        final Map<Bug, int[]> ret = new HashMap<Bug, int[]>();
        final Connection connection = getConnection();
        try {
            final long now = System.currentTimeMillis();
            final long tFrom = adjustToMidnight(now - daysBack * DAY);
            final long tTo = adjustToMidnight(now) + DAY;
            final Date from = new Date(tFrom);
            final Date to = new Date(tTo);
            final List<Bug> bugs = getTopBugs(connection, app, max, from, to);
            final PreparedStatement datesReportedStatement = connection.prepareStatement("" +
                    "select DATE_REPORTED" +
                    " from hit " +
                    " where bug_id=? " +
                    " and DATE_REPORTED BETWEEN ? AND ?");
            try {
                datesReportedStatement.setDate(2, from);
                datesReportedStatement.setDate(3, to);
                for (Bug bug : bugs) {
                    datesReportedStatement.setLong(1, bug.getId());
                    final ResultSet resultSet = datesReportedStatement.executeQuery();
                    final int[] hitCounts = new int[daysBack + 1]; // if we go 0 days back we still 1: today
                    try {
                        while (resultSet.next()) {
                            final Date date = resultSet.getDate(1);
                            final long time = date.getTime();
                            final int day = daysBack - (int) ((tTo - time) / DAY);
                            hitCounts[day + 1]++;
                        }
                    } finally {
                        resultSet.close();
                    }
                    ret.put(bug, hitCounts);
                }
            } finally {
                datesReportedStatement.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return ret;
    }

    private static long adjustToMidnight(long t) {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(t);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private List<Bug> getTopBugs(Connection connection, String app, int max, Date from, Date to) throws SQLException {
        final List<Bug> ret = new ArrayList<Bug>();
        final PreparedStatement countStatement = connection.prepareStatement("select H.BUG_ID, B.TITLE, count(*) \"CNT\" " +
                "  from HIT H,BUG B" +
                "  where H.BUG_ID=B.BUG_ID" +
                "  and H.DATE_REPORTED BETWEEN ? AND ?" +
                "  and B.APP=?" +
                "  group by H.BUG_ID,B.TITLE" +
                "  order by CNT DESC");
        try {
            countStatement.setDate(1, from);
            countStatement.setDate(2, to);
            countStatement.setString(3, app);
            final ResultSet resultSet = countStatement.executeQuery();
            try {
                for (int i = 0; resultSet.next() && i < max; i++) {
                    final long bugId = resultSet.getLong(1);
                    final String title = resultSet.getString(2);
                    final int count = resultSet.getInt(3);
                    final Bug bug = new Bug(bugId, title, count);
                    ret.add(bug);
                }
            } finally {
                resultSet.close();
            }
        } finally {
            countStatement.close();
        }
        return ret;
    }

    //////////
    @Override
    public Stack getStackByHash(String app, String fullHash) {
        Stack ret = null;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "SELECT S.BUG_ID, S.STACK_ID" +
                    " FROM STACK S, BUG B" +
                    " WHERE S.HASH=?" +
                    " AND S.BUG_ID=B.BUG_ID" +
                    " AND B.APP=?");
            try {
                preparedStatement.setString(1, fullHash);
                preparedStatement.setString(2, app);
                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        final long bugId = resultSet.getLong(1);
                        final long stackId = resultSet.getLong(2);
                        ret = new Stack(bugId, stackId);
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
    public Strain getStrainByHash(String app, String strainHash) {
        Strain ret = null;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "SELECT S.BUG_ID, S.STRAIN_ID" +
                    " FROM STRAIN S, BUG B" +
                    " WHERE S.HASH=?" +
                    " AND S.BUG_ID=B.BUG_ID" +
                    " AND B.APP=?");
            try {
                preparedStatement.setString(1, strainHash);
                preparedStatement.setString(2, app);
                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        final long bugId = resultSet.getLong(1);
                        final long strainId = resultSet.getLong(2);
                        ret = new Strain(bugId, strainId);
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
    public long createBug(String app, String title) {
        long ret;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO BUG (APP, TITLE) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
            try {
                preparedStatement.setString(1, app);
                preparedStatement.setString(2, title);
                preparedStatement.executeUpdate();
                final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("Failed to get the generated bug ID");
                }
                ret = generatedKeys.getLong(1);
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
    public Strain createStrain(long bugid, String strainHash) {
        Strain ret;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO STRAIN (BUG_ID,HASH) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
            try {
                preparedStatement.setLong(1, bugid);
                preparedStatement.setString(2, strainHash);
                preparedStatement.executeUpdate();
                final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("Failed to get the generated strain ID");
                }
                final long strainId = generatedKeys.getLong(1);
                ret = new Strain(bugid, strainId);
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
    public Stack createStack(long bugId, long strainId, String fullHash, String stackText) {
        Stack ret;
        final Connection connection = getConnection();
        try {
            ret = insertStack(connection, bugId, strainId, fullHash);

            insertStackText(connection, ret.getStackId(), stackText);
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
        return ret;
    }

    private static Stack insertStack(Connection connection, long bugId, long strainId, String fullHash) throws SQLException {
        final Stack ret;
        final PreparedStatement insertStackStatement = connection.prepareStatement("INSERT INTO STACK (BUG_ID,STRAIN_ID,HASH) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
        try {
            insertStackStatement.setLong(1, bugId);
            insertStackStatement.setLong(2, strainId);
            insertStackStatement.setString(3, fullHash);
            insertStackStatement.executeUpdate();
            final ResultSet generatedKeys = insertStackStatement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new IllegalStateException("Failed to get the generated STACK ID");
            }
            final long stackId = generatedKeys.getLong(1);
            ret = new Stack(bugId, stackId);
        } finally {
            DbUtils.closeQuietly(insertStackStatement);
        }
        return ret;
    }

    private static void insertStackText(Connection connection, long stackId, String stackText) throws SQLException {
        final PreparedStatement insertStackTextStatement = connection.prepareStatement("INSERT INTO STACK_TEXT (STACK_ID,STACK_TEXT) VALUES(?,?)");
        try {
            insertStackTextStatement.setLong(1, stackId);
            if (stackText.length() > STACK_SIZE_LIMIT) {
                stackText = stackText.substring(0, stackText.length());
            }
            insertStackTextStatement.setString(2, stackText);
            final Clob clob = connection.createClob();
            clob.setString(1, stackText);
            insertStackTextStatement.setClob(2, clob);
            insertStackTextStatement.executeUpdate();
        } finally {
            DbUtils.closeQuietly(insertStackTextStatement);
        }
    }

    @Override
    public void reportHitOnStack(String app, String appVersion, String message, String user, Stack stack) {
        final Connection connection = getConnection();

        declareApp(connection, app, appVersion);

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO HIT (BUG_ID,STACK_ID,APP_VER,DATE_REPORTED,MESSAGE,REPORTED_BY) VALUES(?,?,?,?,?,?)");
            try {
                final long bugId = stack.getBugId();
                final long stackId = stack.getStackId();
                final Timestamp now = new Timestamp(System.currentTimeMillis());

                preparedStatement.setLong(1, bugId);
                preparedStatement.setLong(2, stackId);
                preparedStatement.setString(3, appVersion);
                preparedStatement.setTimestamp(4, now);
                preparedStatement.setString(5, truncate(message, 1024));
                preparedStatement.setString(6, truncate(user, 1024));
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

    @Override
    public BugHit getLastHit(long bugId) {
        BugHit ret = null;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "SELECT H.HIT_ID, H.APP_VER, H.DATE_REPORTED,H.REPORTED_BY" +
                    " FROM HIT H" +
                    " WHERE H.BUG_ID=?" +
                    " ORDER BY DATE_REPORTED DESC");
            try {
                preparedStatement.setLong(1, bugId);
                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        final long hitId = resultSet.getLong(1);
                        final String appVer = resultSet.getString(2);
                        final Timestamp timestamp = resultSet.getTimestamp(3);
                        final long dateReported = timestamp.getTime();
                        final String user = resultSet.getString(4);
                        ret = new BugHit(hitId, appVer, dateReported, user);
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

    public String getStack(long hitId) {
        String ret = null;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "SELECT S.STACK_TEXT" +
                    " FROM STACK_TEXT S, HIT H" +
                    " WHERE H.HIT_ID=?" +
                    " AND H.STACK_ID=S.STACK_ID");
            try {
                preparedStatement.setLong(1, hitId);
                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        final Clob clob = resultSet.getClob(1);
                        final Reader characterStream = clob.getCharacterStream();
                        ret = IOUtils.toString(characterStream);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
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
    public BugHitAndStack getBugHitAndStack(long hitId) {
        BugHitAndStack ret = null;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "SELECT H.APP_VER,H.DATE_REPORTED,H.REPORTED_BY,S.STACK_TEXT" +
                    " FROM STACK_TEXT S, HIT H" +
                    " WHERE H.HIT_ID=?" +
                    " AND H.STACK_ID=S.STACK_ID");
            try {
                preparedStatement.setLong(1, hitId);
                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        final String appVer = resultSet.getString(1);
                        final Timestamp timestamp = resultSet.getTimestamp(2);
                        final long dateReported = timestamp.getTime();
                        final String user = resultSet.getString(3);
                        final String stack = resultSet.getString(4);
                        ret = new BugHitAndStack(hitId, appVer, dateReported, user, stack);
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
}
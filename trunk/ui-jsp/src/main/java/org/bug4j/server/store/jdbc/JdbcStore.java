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

package org.bug4j.server.store.jdbc;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bug4j.gwt.admin.client.data.User;
import org.bug4j.gwt.common.client.data.UserException;
import org.bug4j.gwt.user.client.data.*;
import org.bug4j.gwt.user.client.data.Stack;
import org.bug4j.server.store.HitsCallback;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.UserEx;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public class JdbcStore extends Store {
    private static final Logger LOGGER = Logger.getLogger(JdbcStore.class);
    private static JdbcStore INSTANCE;
    private static final int STACK_SIZE_LIMIT = 128 * 1024;
    private static final long DAY = 1000L * 60 * 60 * 24;

    protected JdbcStore() {
    }

    protected void initialize() {
        final Connection connection = getConnection();
        try {
            createTables(connection);
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

    private void createTables(Connection connection) throws SQLException {
        boolean isInstall = false;
        final Statement statement = connection.createStatement();

        try {
            if (!doesTableExist(statement, "BUG4J_USER")) {
                isInstall = true;
                statement.execute("" +
                        "CREATE TABLE BUG4J_USER (" +
                        " USER_NAME VARCHAR(128) NOT NULL PRIMARY KEY," +
                        " USER_PASS VARCHAR(128) NOT NULL," +
                        " USER_EMAIL VARCHAR(128)," +
                        " USER_ADMIN CHAR(1) NOT NULL," +
                        " USER_BUILT_IN CHAR(1) NOT NULL," +
                        " USER_ENABLED CHAR(1) NOT NULL," +
                        " USER_LAST_SIGNED_IN TIMESTAMP" +
                        ")"
                );
            }

            if (!doesTableExist(statement, "BUG4J_AUTHORITIES")) {
                statement.execute("" +
                        "CREATE TABLE BUG4J_AUTHORITIES (" +
                        " USER_NAME VARCHAR(128) NOT NULL PRIMARY KEY," +
                        " AUTHORITY_NAME VARCHAR(128) NOT NULL," +
                        " CONSTRAINT BUG4J_AUTHORITIES_USER_FK FOREIGN KEY(USER_NAME) REFERENCES BUG4J_USER(USER_NAME)" +
                        ")"
                );
                statement.execute("CREATE UNIQUE INDEX BUG4J_AUTHORITIES_IDX ON BUG4J_AUTHORITIES (USER_NAME,AUTHORITY_NAME)");
            }

            if (!doesTableExist(statement, "APP")) {
                statement.execute("" +
                        "CREATE TABLE APP (" +
                        " APP VARCHAR(32) NOT NULL" +
                        ")"
                );
            }

            if (!doesTableExist(statement, "APP_PACKAGES")) {
                statement.execute("" +
                        "CREATE TABLE APP_PACKAGES (" +
                        " APP VARCHAR(32) NOT NULL," +
                        " APP_PACKAGE VARCHAR(64) NOT NULL" +
                        ")"
                );
            }

            if (!doesTableExist(statement, "BUG")) {
                statement.execute("" +
                        "CREATE TABLE BUG (" +
                        " BUG_ID INT GENERATED ALWAYS AS IDENTITY," +
                        " APP VARCHAR(32) NOT NULL," +
                        " TITLE VARCHAR(256) NOT NULL," +
                        " EXTINCT TIMESTAMP," +
                        " UNEXTINCT TIMESTAMP" +
                        ")"
                );
                statement.execute("CREATE INDEX BUG_ID_IDX ON BUG(BUG_ID)");
                statement.execute("CREATE INDEX BUG_TITLE_IDX ON BUG(TITLE)");
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
                        " SESSION_ID INT," +
                        " BUG_ID INT," +
                        " STACK_ID INT," +
                        " APP_VER VARCHAR(32)," +
                        " DATE_BUILT TIMESTAMP," +
                        " DEV_BUILD CHAR(1)," +
                        " BUILD_NUMBER INT," +
                        " DATE_REPORTED TIMESTAMP NOT NULL," +
                        " REPORTED_BY VARCHAR(1024)," +
                        " MESSAGE VARCHAR(1024)" +
                        ")"
                );
                statement.execute("CREATE INDEX HIT_HIT_ID_IDX ON HIT(HIT_ID)");
                statement.execute("CREATE INDEX HIT_BUG_ID_IDX ON HIT(BUG_ID)");
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

            if (!doesTableExist(statement, "CLIENT_SESSION")) {
                statement.execute("" +
                        "CREATE TABLE CLIENT_SESSION (" +
                        " SESSION_ID INT GENERATED ALWAYS AS IDENTITY," +
                        " APP VARCHAR(32) NOT NULL," +
                        " APP_VER VARCHAR(32)," +
                        " HOST_NAME VARCHAR(255)," +
                        " FIRST_HIT TIMESTAMP" +
                        ")"
                );
                statement.execute("CREATE INDEX CLIENT_SESSION_IDX1 ON CLIENT_SESSION(SESSION_ID)");
            }


            if (isInstall) {
                createDefaultAdminUser();
                createApplication("bug4jDemo");
                setPackages("bug4jDemo", Arrays.asList("org.bug4j.demo"));
            }

        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private void createDefaultAdminUser() {
        final User user = new User("bug4j", null, true, true, true, null);
        createUser(user, "admin");
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
                    "SELECT B.BUG_ID," +
                    "        B.TITLE," +
                    "        COUNT(H.HIT_ID) AS HIT_COUNT," +
                    "        MAX(H.HIT_ID) AS HIT_MAX," +
                    "        UR.LAST_HIT_ID," +
                    "        B.APP," +
                    "        B.EXTINCT," +
                    "        B.UNEXTINCT" +
                    "  FROM BUG B" +
                    "    LEFT OUTER JOIN HIT H ON B.BUG_ID = H.BUG_ID" +
                    "    LEFT OUTER JOIN USER_READ UR ON B.BUG_ID = UR.BUG_ID AND UR.USER_NAME=:userName"
            );

            final List<String> conditions = new ArrayList<String>();
            if (app != null) {
                conditions.add("B.APP = :app");
            }
            if (filter.hasHitWithinDays()) {
                conditions.add("H.DATE_REPORTED > :hitDateMin");
            }
            if (filter.hasTitle()) {
                conditions.add("UPPER(B.TITLE) LIKE :titleFilter");
            }
            if (filter.getBugId() != null) {
                conditions.add("B.BUG_ID=:bugId");
            }
            if (!filter.isShowExtinct()) {
                conditions.add("B.EXTINCT IS NULL");
            }
            if (!conditions.isEmpty()) {
                final String whereClause = StringUtils.join(conditions, " AND ");
                sql.append(" WHERE ").append(whereClause);
            }

            sql.append("  GROUP BY B.APP,B.BUG_ID, B.TITLE,UR.LAST_HIT_ID, B.EXTINCT, B.UNEXTINCT");

            sql.append("  HAVING COUNT(H.HIT_ID) > 0");
            if (!filter.isIncludeSingleUserReports()) {
                sql.append(" AND");
                sql.append(" COUNT(DISTINCT H.REPORTED_BY) > 1");
            }

            String orderBySep = " ORDER BY ";
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
            if (app != null) {
                namedParameterProcessor.setParameter(preparedStatement, "app", app);
            }
            namedParameterProcessor.setParameter(preparedStatement, "userName", userName);

            if (filter.hasHitWithinDays()) {
                final Integer hitWithinDays = filter.getHitWithinDays();
                final Timestamp timestamp = getPrevDaysTimestamp(hitWithinDays);
                namedParameterProcessor.setParameter(preparedStatement, "hitDateMin", timestamp);
            }

            if (filter.hasTitle()) {
                namedParameterProcessor.setParameter(preparedStatement, "titleFilter", "%" + filter.getTitle().toUpperCase() + "%");
            }

            if (filter.getBugId() != null) {
                namedParameterProcessor.setParameter(preparedStatement, "bugId", filter.getBugId());
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
                        final String bugApp = resultSet.getString(6);
                        final Timestamp extinctValue = resultSet.getTimestamp(7);
                        final Timestamp unextinctValue = resultSet.getTimestamp(8);
                        final Long extinct = extinctValue == null ? null : extinctValue.getTime();
                        final Long unextinct = unextinctValue == null ? null : unextinctValue.getTime();
                        final Bug bug = new Bug(bugApp, bugId, title, hitCount, maxHit, lastReadHit, extinct, unextinct);
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

    /**
     * Get all the bugs for one app
     */
    @Override
    public void fetchBugs(String app, BugCallback bugCallback) {
        try {
            final Connection connection = getConnection();
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("" +
                        "SELECT B.BUG_ID, B.TITLE" +
                        "  FROM BUG B" +
                        "  WHERE B.APP=?" +
                        "  ORDER BY B.BUG_ID");
                try {
                    preparedStatement.setString(1, app);
                    final ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            final long bugId = resultSet.getLong(1);
                            final String title = resultSet.getString(2);
                            bugCallback.bug(app, bugId, title);
                        }
                    } finally {
                        resultSet.close();
                    }
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
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
    public List<BugHit> getHits(long bugId, int offset, int max, String orderBy) {
        final List<BugHit> ret = new ArrayList<BugHit>();
        final Connection connection = getConnection();
        try {
            StringBuilder sql = new StringBuilder("SELECT H.HIT_ID,H.APP_VER,H.DATE_REPORTED,H.REPORTED_BY,H.DATE_BUILT,H.DEV_BUILD,H.BUILD_NUMBER FROM HIT H WHERE H.BUG_ID=:bugId\n");
            String sep = " ORDER BY ";
            for (int i = 0; i < orderBy.length(); i++) {
                final char c = orderBy.charAt(i);
                final char lc = Character.toLowerCase(c);
                final int columnPos = "abcdefg".indexOf(lc) + 1;
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
                        final Timestamp dateBuilt = resultSet.getTimestamp(5);
                        final String devBuildValue = resultSet.getString(6);
                        final Integer buildNumber = getInteger(resultSet, 7);
                        final BugHit bugHit = new BugHit(
                                hitId,
                                appVer,
                                dateReported,
                                user,
                                dateBuilt == null ? null : dateBuilt.getTime(),
                                "Y".equals(devBuildValue),
                                buildNumber
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

    private Integer getInteger(ResultSet resultSet, int columnIndex) throws SQLException {
        Integer ret = null;
        final int i = resultSet.getInt(columnIndex);
        if (!resultSet.wasNull()) {
            ret = i;
        }
        return ret;
    }

    private Long getDate(ResultSet resultSet, int columnIndex) throws SQLException {
        Long ret = null;
        final Timestamp dateReportedValue = resultSet.getTimestamp(columnIndex);
        if (!resultSet.wasNull()) {
            ret = dateReportedValue.getTime();
        }
        return ret;
    }

    @Override
    public void fetchHits(long bugId, HitsCallback hitsCallback) {
        try {
            final Connection connection = getConnection();
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("" +
                        "SELECT " +
                        "       H.HIT_ID," +                // 1
                        "       H.APP_VER," +               // 2
                        "       H.DATE_REPORTED," +         // 3
                        "       H.REPORTED_BY," +           // 4
                        "       H.MESSAGE," +               // 5
                        "       H.DATE_BUILT," +            // 6
                        "       H.DEV_BUILD," +             // 7
                        "       H.BUILD_NUMBER," +          // 8
                        "       S.STACK_TEXT" +             // 9
                        "   FROM HIT H LEFT OUTER JOIN STACK_TEXT S ON H.STACK_ID=S.STACK_ID" +
                        "   WHERE H.BUG_ID=?" +
                        "   ORDER BY H.HIT_ID");
                try {
                    preparedStatement.setLong(1, bugId);
                    final ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            final long hitId = resultSet.getLong(1);
                            final String appVer = resultSet.getString(2);
                            final long dateReported = getDate(resultSet, 3);
                            final String user = resultSet.getString(4);
                            final String message = resultSet.getString(5);
                            final Long dateBuilt = getDate(resultSet, 6);
                            final boolean devBuild = "Y".equals(resultSet.getString(7));
                            final Integer buildNumber = getInteger(resultSet, 8);
                            final Clob clob = resultSet.getClob(9);
                            String stack = null;
                            if (clob != null) {
                                final Reader characterStream = clob.getCharacterStream();
                                try {
                                    stack = IOUtils.toString(characterStream);
                                } catch (IOException e) {
                                    LOGGER.error("Failed to read the stack trace for bug " + bugId + " hit " + hitId);
                                }
                            }
                            hitsCallback.hit(hitId, appVer, dateReported, dateBuilt, devBuild, buildNumber, user, message, stack);
                        }
                    } finally {
                        resultSet.close();
                    }
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
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
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT DISTINCT APP FROM APP");
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
        Collections.sort(ret, String.CASE_INSENSITIVE_ORDER);
        return ret;
    }

    @Override
    public boolean doesAppExist(String app) {
        boolean ret = false;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT APP FROM APP WHERE APP=?");
            try {
                preparedStatement.setString(1, app);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    ret = true;
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

    @Override
    @Nullable
    public Integer getUserPref_Integer(String remoteUser, String key, @Nullable Integer defaultValue) {
        Integer ret = defaultValue;

        final String defaultStringValue = defaultValue == null ? null : defaultValue.toString();
        final String userPref = getUserPref(remoteUser, key, defaultStringValue);
        // Note that null is a valid value which should not be replaced by the default value.
        if (userPref == null) {
            ret = null;
        } else {
            try {
                ret = Integer.valueOf(userPref);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return ret;
    }

    @Override
    public void setUserPref(String remoteUser, String key, String value) {
        try {
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
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void setUserPref(String remoteUser, String key, Integer value) {
        String stringValue = value == null ? null : value.toString();
        setUserPref(remoteUser, key, stringValue);
    }

    @Override
    public boolean doesUserExist(String userName) {
        boolean ret = false;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT USER_NAME FROM BUG4J_USER WHERE USER_NAME=?");
            try {
                preparedStatement.setString(1, userName);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    ret = true;
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
                    "SELECT DATE(H.DATE_REPORTED),COUNT(*)" +
                    "  FROM HIT H, BUG B" +
                    "  WHERE H.BUG_ID=B.BUG_ID" +
                    "  AND B.APP=?" +
                    "  GROUP BY DATE(H.DATE_REPORTED)" +
                    "  ORDER BY 1");
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

    @Override
    public List<User> getUsers() {
        final ArrayList<User> ret = new ArrayList<User>();
        final Connection connection = getConnection();
        try {
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("" +
                        "SELECT USER_NAME,USER_EMAIL,USER_ADMIN,USER_BUILT_IN,USER_ENABLED,USER_LAST_SIGNED_IN" +
                        "   FROM BUG4J_USER" +
                        "   ORDER BY USER_NAME");
                try {
                    final ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            final String userName = resultSet.getString(1);
                            final String email = resultSet.getString(2);
                            final boolean isAdmin = "Y".equals(resultSet.getString(3));
                            final boolean isBuiltIn = "Y".equals(resultSet.getString(4));
                            final boolean isEnabled = "Y".equals(resultSet.getString(5));
                            final Timestamp lastSignedIn = resultSet.getTimestamp(6);
                            final User user = new User(userName, email, isAdmin, isBuiltIn, isEnabled, lastSignedIn == null ? null : lastSignedIn.getTime());
                            ret.add(user);
                        }
                    } finally {
                        resultSet.close();
                    }
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return ret;
    }

    @Override
    public List<UserEx> getUserExes() {
        final ArrayList<UserEx> ret = new ArrayList<UserEx>();
        final Connection connection = getConnection();
        try {
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("" +
                        "SELECT USER_NAME,USER_PASS,USER_EMAIL,USER_ADMIN,USER_BUILT_IN,USER_ENABLED,USER_LAST_SIGNED_IN" +
                        "   FROM BUG4J_USER" +
                        "   ORDER BY USER_NAME");
                try {
                    final ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            final String userName = resultSet.getString(1);
                            final String userPass = resultSet.getString(2);
                            final String email = resultSet.getString(3);
                            final boolean isAdmin = "Y".equals(resultSet.getString(4));
                            final boolean isBuiltIn = "Y".equals(resultSet.getString(5));
                            final boolean isEnabled = "Y".equals(resultSet.getString(6));
                            final Timestamp lastSignedIn = resultSet.getTimestamp(7);
                            final UserEx user = new UserEx(userName, userPass, email, isAdmin, isBuiltIn, isEnabled, lastSignedIn == null ? null : lastSignedIn.getTime());
                            ret.add(user);
                        }
                    } finally {
                        resultSet.close();
                    }
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return ret;
    }

    @Override
    public void deleteUser(String userName) {
        final Connection connection = getConnection();
        try {
            {
                final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM USER_READ WHERE USER_NAME=?");
                try {
                    preparedStatement.setString(1, userName);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    preparedStatement.close();
                }
            }

            {
                final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM USER_PREFS WHERE USER_NAME=?");
                try {
                    preparedStatement.setString(1, userName);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    preparedStatement.close();
                }
            }

            {
                final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM BUG4J_AUTHORITIES WHERE USER_NAME=?");
                try {
                    preparedStatement.setString(1, userName);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    preparedStatement.close();
                }
            }

            {
                final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM BUG4J_USER WHERE USER_NAME=?");
                try {
                    preparedStatement.setString(1, userName);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    preparedStatement.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public void deleteUsers(Collection<String> userNames) {
        for (String userName : userNames) {
            deleteUser(userName);
        }
    }

    @Override
    public void updateUser(User user) {
        try {
            final Connection connection = getConnection();
            try {
                {
                    final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE BUG4J_USER SET USER_EMAIL=?,USER_ADMIN=?,USER_BUILT_IN=?,USER_ENABLED=? WHERE USER_NAME=?");
                    try {
                        preparedStatement.setString(1, user.getEmail());
                        preparedStatement.setString(2, user.isAdmin() ? "Y" : "N");
                        preparedStatement.setString(3, user.isBuiltIn() ? "Y" : "N");
                        preparedStatement.setString(4, user.isEnabled() ? "Y" : "N");
                        preparedStatement.setString(5, user.getUserName());
                        preparedStatement.executeUpdate();
                    } finally {
                        preparedStatement.close();
                    }
                }

                {
                    final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM BUG4J_AUTHORITIES WHERE USER_NAME=?");
                    try {
                        preparedStatement.setString(1, user.getUserName());
                        preparedStatement.executeUpdate();
                    } finally {
                        preparedStatement.close();
                    }
                }

                insertAuthorities(connection, user);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void createUser(User user, String password) {
        final String encodedPassword = new Md5PasswordEncoder().encodePassword(password, user.getUserName());
        createUserWithEncryptedPassword(user, encodedPassword);
    }

    public void createUserWithEncryptedPassword(User user, String encodedPassword) {
        final Connection connection = getConnection();
        try {
            {
                final PreparedStatement preparedStatement = connection.prepareStatement("" +
                        "INSERT INTO BUG4J_USER(USER_NAME,USER_PASS,USER_EMAIL,USER_ADMIN,USER_BUILT_IN,USER_ENABLED,USER_LAST_SIGNED_IN) VALUES (?,?,?,?,?,'Y',NULL)");
                try {
                    preparedStatement.setString(1, user.getUserName());
                    preparedStatement.setString(2, encodedPassword);
                    preparedStatement.setString(3, user.getEmail());
                    preparedStatement.setString(4, user.isAdmin() ? "Y" : "N");
                    preparedStatement.setString(5, user.isBuiltIn() ? "Y" : "N");
                    preparedStatement.executeUpdate();
                } finally {
                    preparedStatement.close();
                }
            }

            insertAuthorities(connection, user);

        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public void updatePassword(String userName, String oldPassword, String newPassword) {
        try {
            final Connection connection = getConnection();
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE BUG4J_USER SET USER_PASS=? WHERE USER_NAME=? AND USER_PASS=?");
                try {
                    final String oldEncodedPassword = new Md5PasswordEncoder().encodePassword(oldPassword, userName);
                    final String newEncodedPassword = new Md5PasswordEncoder().encodePassword(newPassword, userName);
                    preparedStatement.setString(1, newEncodedPassword);
                    preparedStatement.setString(2, userName);
                    preparedStatement.setString(3, oldEncodedPassword);
                    final int count = preparedStatement.executeUpdate();
                    if (count != 1) {
                        throw new UserException(1, "Invalid password");
                    }
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void createApplication(String applicationName) {
        final Connection connection = getConnection();
        try {
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO APP(APP)VALUES(?)");
                try {
                    preparedStatement.setString(1, applicationName);
                    preparedStatement.executeUpdate();
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteApplication(String applicationName) {
        try {
            final Connection connection = getConnection();
            try {
                deleteAppTable(connection, applicationName, "DELETE FROM STACK_TEXT WHERE STACK_ID IN (SELECT S.STACK_ID FROM STACK S, BUG B WHERE B.APP=? AND S.BUG_ID=B.BUG_ID)");
                deleteAppTable(connection, applicationName, "DELETE FROM STACK WHERE BUG_ID IN (SELECT BUG_ID FROM BUG WHERE APP=?)");
                deleteAppTable(connection, applicationName, "DELETE FROM HIT WHERE BUG_ID IN (SELECT BUG_ID FROM BUG WHERE APP=?)");
                deleteAppTable(connection, applicationName, "DELETE FROM STRAIN WHERE BUG_ID IN (SELECT BUG_ID FROM BUG WHERE APP=?)");
                deleteAppTable(connection, applicationName, "DELETE FROM USER_READ WHERE BUG_ID IN (SELECT BUG_ID FROM BUG WHERE APP=?)");
                deleteAppTable(connection, applicationName, "DELETE FROM BUG WHERE APP=?");
                deleteAppTable(connection, applicationName, "DELETE FROM APP_PACKAGES WHERE APP=?");
                deleteAppTable(connection, applicationName, "DELETE FROM APP WHERE APP=?");
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void deleteAppTable(Connection connection, String applicationName, @Language("SQL92") String statement) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(statement);
            try {
                preparedStatement.setString(1, applicationName);
                preparedStatement.executeUpdate();
            } finally {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void insertAuthorities(Connection connection, User user) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement("" +
                "INSERT INTO BUG4J_AUTHORITIES(USER_NAME,AUTHORITY_NAME) VALUES (?,?)");
        try {
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.isAdmin() ? "admin" : "user");
            preparedStatement.executeUpdate();
        } finally {
            preparedStatement.close();
        }
    }

    @Override
    public void resetPassword(String userName, String password) {
        try {
            final Connection connection = getConnection();
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE BUG4J_USER SET USER_PASS=? WHERE USER_NAME=?");
                try {
                    final String encodedPassword = new Md5PasswordEncoder().encodePassword(password, userName);
                    preparedStatement.setString(1, encodedPassword);
                    preparedStatement.setString(2, userName);
                    preparedStatement.executeUpdate();
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteBug(long bugId) {
        try {
            final Connection connection = getConnection();
            try {
                final PreparedStatement deleteHitsStatement = connection.prepareStatement("DELETE FROM HIT WHERE BUG_ID=?");
                try {
                    deleteHitsStatement.setLong(1, bugId);
                    deleteHitsStatement.executeUpdate();
                } finally {
                    deleteHitsStatement.close();
                }

                final PreparedStatement deleteStackTextStatement = connection.prepareStatement("DELETE FROM STACK_TEXT WHERE STACK_ID IN (SELECT DISTINCT S.STACK_ID FROM STACK S WHERE BUG_ID=?)");
                try {
                    deleteStackTextStatement.setLong(1, bugId);
                    deleteStackTextStatement.executeUpdate();
                } finally {
                    deleteStackTextStatement.close();
                }

                final PreparedStatement deleteStackStatement = connection.prepareStatement("DELETE FROM STACK WHERE BUG_ID=?");
                try {
                    deleteStackStatement.setLong(1, bugId);
                    deleteStackStatement.executeUpdate();
                } finally {
                    deleteStackStatement.close();
                }

                final PreparedStatement deleteStrainStatement = connection.prepareStatement("DELETE FROM STRAIN WHERE BUG_ID=?");
                try {
                    deleteStrainStatement.setLong(1, bugId);
                    deleteStrainStatement.executeUpdate();
                } finally {
                    deleteStrainStatement.close();
                }

                final PreparedStatement deleteBugStatement = connection.prepareStatement("DELETE FROM BUG WHERE BUG_ID=?");
                try {
                    deleteBugStatement.setLong(1, bugId);
                    deleteBugStatement.executeUpdate();
                } finally {
                    deleteBugStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public Map<Bug, int[]> getTopHits(String app, int daysBack, int max) {
        final Map<Bug, int[]> ret = new HashMap<Bug, int[]>();
        try {
            final Connection connection = getConnection();
            try {
                final long now = System.currentTimeMillis();
                final long tFrom = adjustToMidnight(now - daysBack * DAY);
                final long tTo = adjustToMidnight(now) + DAY;
                final Date from = new Date(tFrom);
                final Date to = new Date(tTo);
                final List<Bug> bugs = getTopBugs(connection, app, max, from, to);
                final PreparedStatement datesReportedStatement = connection.prepareStatement("" +
                        "SELECT DATE_REPORTED" +
                        " FROM HIT " +
                        " WHERE BUG_ID=? " +
                        " AND DATE_REPORTED BETWEEN ? AND ?");
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
                connection.close();
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
        final PreparedStatement countStatement = connection.prepareStatement("SELECT H.BUG_ID, B.TITLE, COUNT(*) \"CNT\" " +
                "  FROM HIT H,BUG B" +
                "  WHERE H.BUG_ID=B.BUG_ID" +
                "  AND H.DATE_REPORTED BETWEEN ? AND ?" +
                "  AND B.APP=?" +
                "  GROUP BY H.BUG_ID,B.TITLE" +
                "  ORDER BY CNT DESC");
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
                    final Bug bug = new Bug(app, bugId, title, count);
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
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO BUG (APP, TITLE, EXTINCT) VALUES(?,?,NULL)", Statement.RETURN_GENERATED_KEYS);
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

            final Long stackId = ret.getStackId();
            insertStackText(connection, stackId, stackText);
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
    public void reportHitOnStack(Long sessionId, String appVersion, String message, long dateReported, String user, Stack stack, Long buildDate, boolean devBuild, Integer buildNumber) {
        try {
            final Connection connection = getConnection();

            try {
                final PreparedStatement insertHitStatement = connection.prepareStatement(
                        "INSERT INTO HIT (SESSION_ID,BUG_ID,STACK_ID,APP_VER,DATE_REPORTED,MESSAGE,REPORTED_BY,DATE_BUILT,DEV_BUILD,BUILD_NUMBER) VALUES(?,?,?,?,?,?,?,?,?,?)");
                try {
                    final long bugId = stack.getBugId();
                    final Long stackId = stack.getStackId();
                    final Timestamp now = new Timestamp(dateReported);

                    if (sessionId != null) {
                        insertHitStatement.setLong(1, sessionId);
                    } else {
                        insertHitStatement.setNull(1, Types.INTEGER);
                    }
                    insertHitStatement.setLong(2, bugId);
                    if (stackId != null) {
                        insertHitStatement.setLong(3, stackId);
                    } else {
                        insertHitStatement.setNull(3, Types.INTEGER);
                    }
                    insertHitStatement.setString(4, appVersion);
                    insertHitStatement.setTimestamp(5, now);
                    insertHitStatement.setString(6, truncate(message, 1024));
                    insertHitStatement.setString(7, truncate(user, 1024));

                    if (buildDate != null) {
                        final Timestamp buildDateTimestamp = new Timestamp(buildDate);
                        insertHitStatement.setTimestamp(8, buildDateTimestamp);
                    } else {
                        insertHitStatement.setNull(8, Types.TIMESTAMP);
                    }

                    if (devBuild) {
                        insertHitStatement.setString(9, "Y");
                    } else {
                        insertHitStatement.setNull(9, Types.CHAR);
                    }

                    if (buildNumber != null) {
                        insertHitStatement.setInt(10, buildNumber);
                    } else {
                        insertHitStatement.setNull(10, Types.INTEGER);
                    }

                    insertHitStatement.executeUpdate();
                } finally {
                    insertHitStatement.close();
                }

                final PreparedStatement updateExtinctStatement = connection.prepareStatement("UPDATE BUG SET EXTINCT=NULL, UNEXTINCT=? WHERE BUG_ID=? AND EXTINCT IS NOT NULL");
                try {
                    final Timestamp now = new Timestamp(System.currentTimeMillis());
                    final long bugId = stack.getBugId();
                    updateExtinctStatement.setTimestamp(1, now);
                    updateExtinctStatement.setLong(2, bugId);
                    final int updatedCount = updateExtinctStatement.executeUpdate();
                    if (updatedCount == 1) {
                        LOGGER.info("Bug " + bugId + " was resurected");
                    }
                } finally {
                    updateExtinctStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String getStack(long hitId) {
        String ret = null;
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "SELECT S.STACK_TEXT" +
                    "   FROM HIT H " +
                    "       LEFT OUTER JOIN STACK_TEXT S ON H.STACK_ID=S.STACK_ID " +
                    "   WHERE H.HIT_ID=?");
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
                    "SELECT H.APP_VER,H.DATE_REPORTED,H.REPORTED_BY,H.MESSAGE,S.STACK_TEXT\n" +
                    "  FROM HIT H " +
                    "   LEFT OUTER JOIN STACK_TEXT S ON H.STACK_ID=S.STACK_ID \n" +
                    "  WHERE H.HIT_ID=?");
            try {
                preparedStatement.setLong(1, hitId);
                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        final String appVer = resultSet.getString(1);
                        final Timestamp timestamp = resultSet.getTimestamp(2);
                        final long dateReported = timestamp.getTime();
                        final String user = resultSet.getString(3);
                        final String message = resultSet.getString(4);
                        final String stack = resultSet.getString(5);
                        ret = new BugHitAndStack(hitId, appVer, dateReported, user, message, stack);
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
    public long createSession(String app, String version, long now, String remoteAddr) {
        long ret;
        try {
            final Connection connection = getConnection();
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO CLIENT_SESSION(APP,APP_VER,HOST_NAME,FIRST_HIT) VALUES(?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setString(1, app);
                    preparedStatement.setString(2, version);
                    preparedStatement.setString(3, remoteAddr);
                    preparedStatement.setTimestamp(4, new Timestamp(now));
                    preparedStatement.executeUpdate();
                    final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (!generatedKeys.next()) {
                        throw new IllegalStateException("Failed to get the generated bug ID");
                    }
                    ret = generatedKeys.getLong(1);
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return ret;
    }

    public void updateExtinctStatus() {
        try {
            final Connection connection = getConnection();
            try {
                final Collection<Long> extinctBugIds = collectExtinctBugs(connection);

                final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE BUG SET EXTINCT=?, UNEXTINCT=NULL WHERE BUG_ID=?");
                try {
                    final Timestamp now = new Timestamp(System.currentTimeMillis());
                    for (Long bugId : extinctBugIds) {
                        preparedStatement.setTimestamp(1, now);
                        preparedStatement.setLong(2, bugId);
                        preparedStatement.executeUpdate();
                    }
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Find bugs that are extinct.
     */
    private Collection<Long> collectExtinctBugs(Connection connection) throws SQLException {
        final Collection<Long> extinctBugIds = new ArrayList<Long>();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final PreparedStatement preparedStatement = connection.prepareStatement("" +
                "SELECT\n" +
                "        BUG.BUG_ID," +
                "        MIN(HIT.DATE_REPORTED)," +
                "        MAX(HIT.DATE_REPORTED)," +
                "        COUNT(HIT.BUG_ID)" +
                "  FROM BUG LEFT JOIN HIT ON BUG.BUG_ID=HIT.BUG_ID" +
                "  WHERE BUG.EXTINCT IS NULL" +
                "  GROUP BY BUG.BUG_ID" +
                "  ORDER BY COUNT(HIT.BUG_ID) DESC");
        try {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final long now = System.currentTimeMillis();
            while (resultSet.next()) {
                final long bugId = resultSet.getLong(1);
                final Timestamp minTimestamp = resultSet.getTimestamp(2);
                final Timestamp maxTimestamp = resultSet.getTimestamp(3);
                final int count = resultSet.getInt(4);

                final long DAY = 1000 * 60 * 60 * 24;
                final long min = minTimestamp.getTime();
                final long max = maxTimestamp.getTime();

                if (max + 3 * DAY < now) { // If it has been more than 3 days since the last hit
                    // Calculate the average elapsed time between two hits. Consider that anything below 3 days is irrelevant
                    final long avgFreq = Math.max((max - min) / count, 3 * DAY);
                    if (max + avgFreq * 3 < now) { // If it has been more than 3 times the average hit frequency
                        LOGGER.info(
                                String.format("Extinct bug:%d (%s <-> %s/%d)",
                                        bugId,
                                        simpleDateFormat.format(minTimestamp),
                                        simpleDateFormat.format(maxTimestamp),
                                        count
                                )
                        );
                        extinctBugIds.add(bugId);
                    }
                }
            }
        } finally {
            preparedStatement.close();
        }
        return extinctBugIds;
    }

    /**
     * Add the following columns to the HIT table:
     * DATE_BUILT TIMESTAMP
     * DEV_BUILD CHAR(1)
     * BUILD_NUMBER INT
     */
    public void migrate_addBuildDetailColumns() {

        try {
            final Connection connection = getConnection();
            try {
                final Statement statement = connection.createStatement();
                try {
                    statement.execute("ALTER TABLE HIT ADD COLUMN DATE_BUILT TIMESTAMP");
                    statement.execute("ALTER TABLE HIT ADD COLUMN DEV_BUILD CHAR(1)");
                    statement.execute("ALTER TABLE HIT ADD COLUMN BUILD_NUMBER INT");
                    statement.execute("UPDATE HIT SET BUILD_NUMBER=0");
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            final String sqlState = e.getSQLState();
            if (!"X0Y32".equals(sqlState)) { // X0Y32: Column 'XXX' already exists in Table/View '"YYY"."ZZZ"'.
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
}

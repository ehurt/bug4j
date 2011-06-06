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
import org.bug4j.server.gwt.client.data.*;
import org.bug4j.server.gwt.client.data.Stack;
import org.bug4j.server.store.Store;

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
                        " TITLE VARCHAR(256) NOT NULL" +
                        ")"
                );
            }

            if (!doesTableExist(statement, "STRAIN")) {
                statement.execute("" +
                        "CREATE TABLE STRAIN (" +
                        " STRAIN_ID INT GENERATED ALWAYS AS IDENTITY," +
                        " BUG_ID INT," +
                        " HASH VARCHAR(64)" +
                        ")"
                );
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
            }

            if (!doesTableExist(statement, "STACK_TEXT")) {
                statement.execute("" +
                        "CREATE TABLE STACK_TEXT (" +
                        " STACK_ID INT," +
                        " STACK_TEXT CLOB(" + STACK_SIZE_LIMIT + ")" +
                        ")"
                );
            }

            if (!doesTableExist(statement, "HIT")) {
                statement.execute("" +
                        "CREATE TABLE HIT (" +
                        " HIT_ID INT GENERATED ALWAYS AS IDENTITY," +
                        " BUG_ID INT," +
                        " STACK_ID INT," +
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

    public Bug getBug(String app, long bugId) {
        String title = null;
        int hitCount = 0;
        final Connection connection = getConnection();
        try {
            final PreparedStatement bugStatement = connection.prepareStatement("select TITLE from BUG where BUG_ID=?");
            try {
                bugStatement.setLong(1, bugId);

                final ResultSet resultSet = bugStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        title = resultSet.getString(1);
                    }
                } finally {
                    DbUtils.closeQuietly(resultSet);
                }
            } finally {
                DbUtils.closeQuietly(bugStatement);
            }

            final PreparedStatement countStatement = connection.prepareStatement("select count(*) from HIT where BUG_ID=?");
            try {
                countStatement.setLong(1, bugId);

                final ResultSet resultSet = countStatement.executeQuery();
                try {
                    if (resultSet.next()) {
                        hitCount = resultSet.getInt(1);
                    }
                } finally {
                    DbUtils.closeQuietly(resultSet);
                }
            } finally {
                DbUtils.closeQuietly(countStatement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
        return new Bug(bugId, title, hitCount);
    }

    /**
     * Get a set of bugs
     *
     * @param orderBy can be a combination of i(d), t(itle) or h(its)
     * @return
     */
    @Override
    public List<Bug> getBugs(String app, int offset, int max, String orderBy) {
        final List<Bug> ret = new ArrayList<Bug>();
        final Connection connection = getConnection();
        try {
            StringBuilder sql = new StringBuilder("" +
                    "select BUG.BUG_ID,BUG.TITLE,(select count(*) from HIT where HIT.BUG_ID=BUG.BUG_ID) HITCOUNT from BUG\n");
            sql.append(" where BUG.APP=?");
            String sep = " order by ";
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
            preparedStatement.setString(1, app);
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

    @Override
    public List<Hit> getHits(long bugId, int offset, int max, String orderBy) {
        final List<Hit> ret = new ArrayList<Hit>();
        final Connection connection = getConnection();
        try {
            StringBuilder sql = new StringBuilder("select ID,APP_VER,DATE_REPORTED from HIT WHERE BUG_ID=?\n");
            String sep = "order by ";
            for (int i = 0; i < orderBy.length(); i++) {
                final char c = orderBy.charAt(i);
                final char lc = Character.toLowerCase(c);
                final int columnPos = "iadb".indexOf(lc) + 1;
                final boolean asc = Character.isLowerCase(c);
                sql
                        .append(sep)
                        .append(columnPos)
                        .append(asc ? " ASC" : " DESC");
                sep = ", ";
            }
            final PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
            try {
                preparedStatement.setLong(1, bugId);

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
                        final Hit hit = new Hit(
                                hitId,
                                bugId,
                                appVer,
                                dateReported
                        );
                        ret.add(hit);
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
    public List<Long> getHitIds(long bugId) {
        final List<Long> ret = new ArrayList<Long>();
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("select HIT_ID from HIT WHERE BUG_ID=? ORDER BY DATE_REPORTED DESC");
            try {
                preparedStatement.setLong(1, bugId);

                final ResultSet resultSet = preparedStatement.executeQuery();
                try {
                    while (resultSet.next()) {
                        final long hitId = resultSet.getLong(1);
                        ret.add(hitId);
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
                    "SELECT S.BUG_ID, S.STRAIN_ID, S.STACK_ID" +
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
                        final long strainId = resultSet.getLong(2);
                        final long stackId = resultSet.getLong(3);
                        ret = new Stack(bugId, strainId, stackId);
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
    public Strain createStrain(String app, long bugid, String strainHash) {
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
    public Stack createStack(String app, long bugId, long strainId, String fullHash, String stackText) {
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
            ret = new Stack(bugId, strainId, stackId);
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
    public void reportHitOnStack(String app, String appVersion, Stack stack) {
        final Connection connection = getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO HIT (BUG_ID,STACK_ID,APP_VER,DATE_REPORTED) VALUES(?,?,?,?)");
            try {
                final long bugId = stack.getBugId();
                final long stackId = stack.getStackId();
                final Timestamp now = new Timestamp(System.currentTimeMillis());

                preparedStatement.setLong(1, bugId);
                preparedStatement.setLong(2, stackId);
                preparedStatement.setString(3, appVersion);
                preparedStatement.setTimestamp(4, now);
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
                    "SELECT H.HIT_ID, H.APP_VER, H.DATE_REPORTED" +
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
                        ret = new BugHit(hitId, appVer, dateReported);
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
                    "SELECT H.APP_VER,H.DATE_REPORTED,S.STACK_TEXT" +
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
                        final String stack = resultSet.getString(3);
                        ret = new BugHitAndStack(hitId, appVer, dateReported, stack);
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

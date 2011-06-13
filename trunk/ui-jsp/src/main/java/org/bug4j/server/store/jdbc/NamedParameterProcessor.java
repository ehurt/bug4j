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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedParameterProcessor {
    private Map<String, List<Integer>> _paramPos = new HashMap<String, List<Integer>>();
    private String _jdbcSql;

    public NamedParameterProcessor(String sql) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Pattern pattern = Pattern.compile(":[a-zA-Z0-9]+");
        final Matcher matcher = pattern.matcher(sql);
        int paramNum = 0;
        int pos = 0;
        while (matcher.find(pos)) {
            final String prefix = sql.substring(pos, matcher.start());
            final String paramName = sql.substring(matcher.start() + 1, matcher.end());
            List<Integer> integerList = _paramPos.get(paramName);
            if (integerList == null) {
                integerList = new ArrayList<Integer>();
                _paramPos.put(paramName, integerList);
            }
            integerList.add(++paramNum);
            stringBuilder.append(prefix).append("?");
            pos = matcher.end();
        }
        stringBuilder.append(sql.substring(pos));
        _jdbcSql = stringBuilder.toString();
    }

    public String getJdbcSql() {
        return _jdbcSql;
    }

    public List<Integer> getParameterPositions(String name) {
        final List<Integer> integerList = _paramPos.get(name);
        return integerList == null ? Collections.<Integer>emptyList() : Collections.unmodifiableList(integerList);
    }

    public void setParameter(PreparedStatement preparedStatement, String parameterName, String value) throws SQLException {
        final List<Integer> parameterPositions = getParameterPositions(parameterName);
        for (Integer parameterPosition : parameterPositions) {
            preparedStatement.setString(parameterPosition, value);
        }
    }

    public void setParameter(PreparedStatement preparedStatement, String parameterName, int value) throws SQLException {
        final List<Integer> parameterPositions = getParameterPositions(parameterName);
        for (Integer parameterPosition : parameterPositions) {
            preparedStatement.setInt(parameterPosition, value);
        }
    }

    public void setParameter(PreparedStatement preparedStatement, String parameterName, long value) throws SQLException {
        final List<Integer> parameterPositions = getParameterPositions(parameterName);
        for (Integer parameterPosition : parameterPositions) {
            preparedStatement.setLong(parameterPosition, value);
        }
    }

    public void setParameter(PreparedStatement preparedStatement, String parameterName, java.sql.Date value) throws SQLException {
        final List<Integer> parameterPositions = getParameterPositions(parameterName);
        for (int parameterPosition : parameterPositions) {
            preparedStatement.setDate(parameterPosition, value);
        }
    }

    public void setParameter(PreparedStatement preparedStatement, String parameterName, Timestamp value) throws SQLException {
        final List<Integer> parameterPositions = getParameterPositions(parameterName);
        for (int parameterPosition : parameterPositions) {
            preparedStatement.setTimestamp(parameterPosition, value);
        }
    }
}

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

package org.dandoy.bug4j.server.gwt.client.bugs;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import org.dandoy.bug4j.server.gwt.client.data.Hit;

import java.util.Date;

public class HitViewColumn extends TextColumn<Hit> {
    public static final HitViewColumn ID = new HitViewColumn("I");
    public static final HitViewColumn APP_VER = new HitViewColumn("V");
    public static final Column<Hit, Date> DATE_REPORTED;

    static {
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_FULL);
        final DateCell dateCell = new DateCell(dateTimeFormat);
        DATE_REPORTED = new Column<Hit, Date>(dateCell) {
            @Override
            public Date getValue(Hit hit) {
                return new Date(hit.getDateReported());
            }
        };
        DATE_REPORTED.setSortable(true);
    }

    private final String _id;

    public HitViewColumn(String id) {
        _id = id;
        setSortable(true);
    }

    @Override
    public String getValue(Hit hit) {
        if (ID._id.equals(_id)) {
            return Long.toString(hit.getId());
        } else if (APP_VER._id.equals(_id)) {
            return hit.getAppVer();
        } else {
            throw new IllegalStateException("Invalid id: " + _id);
        }
    }

    @Override
    public void render(Cell.Context context, Hit object, SafeHtmlBuilder sb) {
        super.render(context, object, sb);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public static String sortBy(ColumnSortList columnSortList) {
        final StringBuilder ret = new StringBuilder();
        final int size = columnSortList.size();
        for (int i = 0; i < size; i++) {
            final ColumnSortList.ColumnSortInfo columnSortInfo = columnSortList.get(i);
            final String columnId = getColumnId(columnSortInfo.getColumn());
            final String id;
            if (columnSortInfo.isAscending()) {
                id = columnId.toLowerCase();
            } else {
                id = columnId.toUpperCase();
            }
            ret.append(id);
        }
        return ret.toString();
    }

    private static String getColumnId(Column<?, ?> column) {
        if (ID == column) {
            return "I";
        } else if (APP_VER == column) {
            return "A";
        } else if (DATE_REPORTED == column) {
            return "D";
        } else {
            return "I";
        }
    }
}

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

package org.bug4j.server.gwt.client.bugs;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import org.bug4j.server.gwt.client.Bug4jService;
import org.bug4j.server.gwt.client.data.Hit;

import java.util.List;

public class HitsView {

    private CellTable<Hit> _cellTable;
    private long _bugId;

    public HitsView() {
    }

    public Widget createWidget() {
        _cellTable = new CellTable<Hit>(100);
        _cellTable.addColumn(HitViewColumn.ID, "ID");
        _cellTable.addColumn(HitViewColumn.APP_VER, "Version");
        _cellTable.addColumn(HitViewColumn.DATE_REPORTED, "Date Reported");
        _cellTable.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(HitViewColumn.DATE_REPORTED, false));

        _cellTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(_cellTable) {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                refreshData();
            }
        });
        _cellTable.setSelectionModel(new NoSelectionModel<Hit>());

        return new ScrollPanel(_cellTable);
    }

    public void setBug(long bugId) {
        _bugId = bugId;
        refreshData();
    }

    private void refreshData() {
        final ColumnSortList sortList = _cellTable.getColumnSortList();
        final String sortBy = HitViewColumn.sortBy(sortList);
        Bug4jService.App.getInstance().getHits(_bugId, 0, 100, sortBy, new AsyncCallback<List<Hit>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(List<Hit> hits) {
                _cellTable.setRowData(hits);
            }
        });
    }
}

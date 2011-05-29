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

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.dandoy.bug4j.server.gwt.client.Bug4j;
import org.dandoy.bug4j.server.gwt.client.Bug4jService;
import org.dandoy.bug4j.server.gwt.client.data.Bug;

import java.util.List;

public class BugView implements DisplaysBugs {

    private static final int PAGE_SIZE = 100;
    private SingleSelectionModel<Bug> _selectionModel;
    private BugDetailView _bugDetailView;
    private CellTable<Bug> _cellTable;

    public BugView() {
    }

    public Widget createWidget() {
        final CellTable<Bug> bugCellTable = createTable();
        final ScrollPanel scrollPanel = new ScrollPanel(bugCellTable);

        _bugDetailView = new BugDetailView(this);

        final SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel(5);
        splitLayoutPanel.addWest(scrollPanel, 500);
        splitLayoutPanel.add(_bugDetailView.createWidget());
        return splitLayoutPanel;
    }

    public void whenBugListChanges() {
        refreshBugs();
    }

    private CellTable<Bug> createTable() {
        _cellTable = new CellTable<Bug>(PAGE_SIZE);
        //noinspection GWTStyleCheck
        _cellTable.addStyleName("bug-table");
        _cellTable.addColumn(BugViewColumn.ID, "ID");
        _cellTable.setColumnWidth(BugViewColumn.TITLE, "300px");
        _cellTable.addColumn(BugViewColumn.TITLE, "Title");
        _cellTable.addColumn(BugViewColumn.HIT, "#");

        _cellTable.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(BugViewColumn.HIT, false));

        AsyncDataProvider<Bug> dataProvider = new AsyncDataProvider<Bug>() {
            @Override
            protected void onRangeChanged(HasData<Bug> display) {
                refreshBugs();
            }
        };
        dataProvider.addDataDisplay(_cellTable);
        _cellTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(_cellTable));
        _selectionModel = new SingleSelectionModel<Bug>();
        _cellTable.setSelectionModel(_selectionModel);

        _selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                whenTableSelectionChanges();
            }
        });

        return _cellTable;
    }

    private void refreshBugs() {
        final ColumnSortList sortList = _cellTable.getColumnSortList();
        final String sortBy = BugViewColumn.sortBy(sortList);
        Bug4jService.App.getInstance().getBugs(Bug4j.APP, sortBy, new AsyncCallback<List<Bug>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(List<Bug> bugs) {
                _cellTable.setRowData(bugs);
                if (!bugs.isEmpty()) {
                    final Bug firstBug = bugs.get(0);
                    _selectionModel.setSelected(firstBug, true);
                }
            }
        });
    }

    private void whenTableSelectionChanges() {
        final Bug bug = _selectionModel.getSelectedObject();
        final long bugId = bug.getId();
        _bugDetailView.displayBug(bugId);
    }
}

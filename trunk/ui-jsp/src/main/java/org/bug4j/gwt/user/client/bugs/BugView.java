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

package org.bug4j.gwt.user.client.bugs;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.user.client.Bug4jService;
import org.bug4j.gwt.user.client.BugModel;
import org.bug4j.gwt.user.client.data.Bug;
import org.bug4j.gwt.user.client.data.Filter;
import org.bug4j.gwt.user.client.event.ApplicationChangedEvent;
import org.bug4j.gwt.user.client.event.ApplicationChangedEventHandler;
import org.bug4j.gwt.user.client.event.BugListChanged;
import org.bug4j.gwt.user.client.event.BugListChangedHandler;

import java.util.List;

public class BugView {

    private static final int PAGE_SIZE = 100;
    private final BugModel _bugModel;
    private SingleSelectionModel<Bug> _selectionModel;
    private BugDetailView _bugDetailView;
    private CellTable<Bug> _cellTable;
    private final Filter _filter = new Filter();
    private MenuItem _filterMenuItem;
    private Bug _lastSelectedBug;

    public BugView(BugModel bugModel) {
        _bugModel = bugModel;

        final EventBus eventBus = _bugModel.getEventBus();
        eventBus.addHandler(ApplicationChangedEvent.TYPE, new ApplicationChangedEventHandler() {
            @Override
            public void onApplicationChanged(ApplicationChangedEvent event) {
                refreshBugs();
            }
        });
        eventBus.addHandler(BugListChanged.TYPE, new BugListChangedHandler() {
            @Override
            public void onBugListChanged(BugListChanged event) {
                refreshBugs();
            }
        });

        Bug4jService.App.getInstance().getDefaultFilter(new AdvancedAsyncCallback<Filter>() {
            @Override
            public void onSuccess(Filter result) {
                result.copyTo(_filter);
                refreshBugs();
            }
        });
    }

    public Widget createWidget() {
        final CellTable<Bug> bugCellTable = createTable();
        final ScrollPanel scrollPanel = new ScrollPanel(bugCellTable);
        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.EM);
        final MenuBar menuBar = new MenuBar();
        _filterMenuItem = menuBar.addItem("<<<filter>>>", new Command() {
            @Override
            public void execute() {
                whenFilter();
            }
        });
        updateFilterMenuItem();

        dockLayoutPanel.addNorth(menuBar, 2);

        dockLayoutPanel.add(scrollPanel);

        _bugDetailView = new BugDetailView(_bugModel);

        final SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel(5);
        splitLayoutPanel.addWest(dockLayoutPanel, 600);
        splitLayoutPanel.add(_bugDetailView.createWidget());
        return splitLayoutPanel;
    }

    private void whenFilter() {
        final FilterDialog filterDialog = new FilterDialog(_filter);
        filterDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                if (!popupPanelCloseEvent.isAutoClosed()) {
                    final Filter newFilter = filterDialog.getFilter();
                    newFilter.copyTo(_filter);
                    refreshBugs();
                    updateFilterMenuItem();
                    Bug4jService.App.getInstance().setDefaultFilter(newFilter, new AdvancedAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            // ignore
                        }
                    });
                }
            }
        });
        filterDialog.show();
    }

    private void updateFilterMenuItem() {
        _filterMenuItem.setText("Filter" + (_filter.isFiltering() ? "*" : ""));
    }

    private CellTable<Bug> createTable() {
        _cellTable = new CellTable<Bug>(PAGE_SIZE);
        final Label noDataLabel = new Label("No bugs");
        noDataLabel.getElement().getStyle().setFontSize(20, Style.Unit.PT);
        _cellTable.setEmptyTableWidget(noDataLabel);
        _cellTable.setWidth("100%", true);
        _cellTable.addStyleName("BugView-table");
        _cellTable.addColumn(BugViewColumn.ID, "ID");
        _cellTable.addColumn(BugViewColumn.TITLE, "Title");
        _cellTable.addColumn(BugViewColumn.HIT, "#");

        _cellTable.setColumnWidth(BugViewColumn.ID, "5em");
        _cellTable.setColumnWidth(BugViewColumn.HIT, "5em");

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

        _cellTable.setRowStyles(new RowStyles<Bug>() {
            @Override
            public String getStyleNames(Bug row, int rowIndex) {
                if (row.isRead()) {
                    return "BugView-row-read";
                } else {
                    return "BugView-row-unread";
                }
            }
        });

        return _cellTable;
    }

    private void refreshBugs() {
        if (_cellTable != null && _selectionModel != null) {
            final ColumnSortList sortList = _cellTable.getColumnSortList();
            final String sortBy = BugViewColumn.sortBy(sortList);
            final String application = _bugModel.getApplication();
            if (application != null) {
                Bug4jService.App.getInstance().getBugs(application, _filter, sortBy, new AdvancedAsyncCallback<List<Bug>>() {
                    @Override
                    public void onSuccess(List<Bug> bugs) {
                        _cellTable.setRowData(bugs);
                        if (!bugs.isEmpty()) {
                            final Bug firstBug = bugs.get(0);
                            _selectionModel.setSelected(firstBug, true);
                        } else {
                            _selectionModel.setSelected(null, true);
                        }
                    }
                });
            } else {
                _cellTable.setRowCount(0);
                _selectionModel.setSelected(null, true);
            }
        }
    }

    private void whenTableSelectionChanges() {
        if (_lastSelectedBug != null) {
            if (!_lastSelectedBug.isRead()) {
                _lastSelectedBug.setRead(true);
                Bug4jService.App.getInstance().markRead(_lastSelectedBug.getId(), new AdvancedAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                    }
                });
            }
        }
        _lastSelectedBug = _selectionModel.getSelectedObject();
        if (_lastSelectedBug != null) {
            _bugDetailView.displayBug(_lastSelectedBug);
        } else {
            _bugDetailView.clear();
        }
    }
}

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
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
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

/**
 * The view that shows bugs on the left and hits on the right.
 */
public class BugView extends SplitLayoutPanel {

    private static final int PAGE_SIZE = 100;
    private final BugModel _bugModel;
    private SingleSelectionModel<Bug> _selectionModel;
    private BugDetailView _bugDetailView;
    private DataGrid<Bug> _dataGrid;
    private final Filter _filter;
    private MenuItem _filterMenuItem;
    private Bug _lastSelectedBug;

    public BugView(BugModel bugModel, Filter filter) {
        super(5);

        _bugModel = bugModel;
        _filter = filter;
        _bugDetailView = new BugDetailView(_bugModel);

        createTable();
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

        dockLayoutPanel.add(_dataGrid);

        addWest(dockLayoutPanel, 600);
        add(_bugDetailView);

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

    private DataGrid<Bug> createTable() {
        _dataGrid = new DataGrid<Bug>(PAGE_SIZE);
        _dataGrid.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        final Label noDataLabel = new Label("No bugs");
        noDataLabel.getElement().getStyle().setFontSize(20, Style.Unit.PT);
        _dataGrid.setEmptyTableWidget(noDataLabel);
        _dataGrid.setWidth("100%");
        _dataGrid.addStyleName("BugView-table");
        _dataGrid.addColumn(BugViewColumn.ID, "ID");
        _dataGrid.addColumn(BugViewColumn.TITLE, "Title");
        _dataGrid.addColumn(BugViewColumn.HIT, "#");

        _dataGrid.setColumnWidth(BugViewColumn.ID, "5em");
        _dataGrid.setColumnWidth(BugViewColumn.HIT, "5em");

        _dataGrid.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(BugViewColumn.HIT, false));

        _dataGrid.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(_dataGrid));
        _selectionModel = new SingleSelectionModel<Bug>();
        _dataGrid.setSelectionModel(_selectionModel);

        _selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                whenTableSelectionChanges();
            }
        });

        _dataGrid.setRowStyles(new RowStyles<Bug>() {
            @Override
            public String getStyleNames(Bug row, int rowIndex) {
                if (row.isRead()) {
                    return "BugView-row-read";
                } else {
                    return "BugView-row-unread";
                }
            }
        });

        AsyncDataProvider<Bug> dataProvider = new AsyncDataProvider<Bug>() {
            @Override
            protected void onRangeChanged(HasData<Bug> display) {
                final Range range = display.getVisibleRange();
                final ColumnSortList sortList = _dataGrid.getColumnSortList();
                final String sortBy = BugViewColumn.sortBy(sortList);

                final String application = _bugModel.getApplication();
                Bug4jService.App.getInstance().getBugs(application, _filter, sortBy, new AdvancedAsyncCallback<List<Bug>>() {
                    @Override
                    public void onSuccess(List<Bug> bugs) {
                        _dataGrid.setRowData(range.getStart(), bugs);
                        _dataGrid.setRowCount(range.getStart() + bugs.size(), true);
                        if (!bugs.isEmpty()) {
                            final Bug firstBug = bugs.get(0);
                            _selectionModel.setSelected(firstBug, true);
                        } else {
                            _selectionModel.setSelected(null, true);
                        }
                    }
                });
            }
        };
        dataProvider.addDataDisplay(_dataGrid);

        return _dataGrid;
    }

    private void refreshBugs() {
        final Range visibleRange = _dataGrid.getVisibleRange();
        _dataGrid.setVisibleRangeAndClearData(visibleRange, true);
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

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

package org.bug4j.gwt.admin.client;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.common.client.BaseDialog;
import org.bug4j.gwt.common.client.CommonService;

import java.util.List;

/**
 */
public class ApplicationsView extends AdminView {

    private CellTable<String> _cellTable;
    private final Admin _admin;

    protected ApplicationsView(Admin admin) {
        super("Applications");
        _admin = admin;
    }

    @Override
    protected Widget createWidget() {
        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.EM);

        final Widget buttonPanel = createButtonPanel();
        dockLayoutPanel.addNorth(buttonPanel, 2.5);


        _cellTable = createCellTable();

        final ScrollPanel scrollPanel = new ScrollPanel(_cellTable);
        {
            final Style style = scrollPanel.getElement().getStyle();
            style.setPadding(10, Style.Unit.PX);
        }

        dockLayoutPanel.add(scrollPanel);

        refreshData();

        return dockLayoutPanel;
    }

    private Widget createButtonPanel() {
        final Grid grid = new Grid(1, 2);
        grid.setCellPadding(2);
        final Button newButton = new Button("New Application");
        newButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenNewApplication();
            }
        });
        grid.setWidget(0, 0, newButton);
        return grid;
    }

    private CellTable<String> createCellTable() {
        final CellTable<String> cellTable = new CellTable<String>();
        {
            final Label emptyTableWidget = new Label("No Applications");
            emptyTableWidget.getElement().getStyle().setFontSize(20, Style.Unit.PT);
            cellTable.setEmptyTableWidget(emptyTableWidget);
        }

        {
            final ClickableTextCell clickableTextCell = new ClickableTextCell(new AbstractSafeHtmlRenderer<String>() {
                @Override
                public SafeHtml render(String object) {
                    return new SafeHtmlBuilder()
                            .appendHtmlConstant("<a href=\"#\">")
                            .appendEscaped(object)
                            .appendHtmlConstant("</a>")
                            .toSafeHtml();
                }
            });

            final IdentityColumn<String> column = new IdentityColumn<String>(clickableTextCell);

            column.setFieldUpdater(new FieldUpdater<String, String>() {
                @Override
                public void update(int index, String applicationName, String value) {
                    whenEdit(applicationName);
                }
            });

            cellTable.addColumn(column, "Application Name");

            cellTable.setColumnWidth(column, 300, Style.Unit.PX);
        }

        {
            final ClickableTextCell clickableTextCell = new ClickableTextCell(new AbstractSafeHtmlRenderer<String>() {
                @Override
                public SafeHtml render(String object) {
                    return new SafeHtmlBuilder()
                            .appendHtmlConstant("<a href=\"#\">Delete</a>")
                            .toSafeHtml();
                }
            });

            final IdentityColumn<String> column = new IdentityColumn<String>(clickableTextCell);

            column.setFieldUpdater(new FieldUpdater<String, String>() {
                @Override
                public void update(int index, String applicationName, String value) {
                    whenDelete(applicationName);
                }
            });

            cellTable.addColumn(column);
        }

        return cellTable;
    }

    void refreshData() {
        CommonService.App.getInstance().getApplications(new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Server failure.");
            }

            @Override
            public void onSuccess(List<String> applicationNames) {
                _cellTable.setRowData(applicationNames);
            }
        });
    }

    private void whenNewApplication() {
        final BaseDialog baseDialog = new BaseDialog("New Application") {

            private TextBox _textBox;

            @Override
            protected void whenOk() {
                final String applicationName = _textBox.getText();
                if (!applicationName.isEmpty()) {
                    AdminService.App.getInstance().createApplication(applicationName, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Server failure");
                        }

                        @Override
                        public void onSuccess(Void result) {
                            _admin.whenApplicationsChanged();
                            refreshData();
                            hide();
                        }
                    });
                }
            }

            @Override
            protected Widget createContent() {
                final Grid grid = new Grid(1, 2);
                grid.setWidget(0, 0, new Label("Name:"));
                _textBox = new TextBox();
                _textBox.setWidth("242px");
                grid.setWidget(0, 1, _textBox);

                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        _textBox.setFocus(true);
                    }
                });
                return grid;
            }

            @Override
            protected Widget createButtons() {
                final Widget ret = super.createButtons();
                ret.setWidth("300px");
                return ret;
            }
        };
        baseDialog.show();
    }

    private void whenEdit(String applicationName) {
        _admin.edit(applicationName);
    }

    private void whenDelete(String applicationName) {
        final boolean confirm = Window.confirm("" +
                "This operation will delete the application including the associated bugs.\n" +
                "This operation is not reversible\n" +
                "Are you sure you want  to delete '" + applicationName + "'?");
        if (confirm) {
            AdminService.App.getInstance().deleteApplication(applicationName, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Server failure");
                }

                @Override
                public void onSuccess(Void result) {
                    _admin.whenApplicationsChanged();
                    refreshData();
                }
            });
        }
    }
}

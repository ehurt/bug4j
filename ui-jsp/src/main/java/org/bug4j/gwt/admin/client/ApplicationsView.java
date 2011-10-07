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
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.admin.client.event.ApplicationsChangedEvent;
import org.bug4j.gwt.admin.client.event.ApplicationsChangedEventHandler;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.BaseDialog;

import java.util.List;

/**
 * The admin view with the list of applications which allows to create and delete applications.
 */
public class ApplicationsView extends DockLayoutPanel implements ApplicationsChangedEventHandler {

    private CellTable<String> _cellTable;
    private final Admin _admin;
    private final AdminModel _adminModel;

    protected ApplicationsView(Admin admin, AdminModel adminModel) {
        super(Style.Unit.EM);
        _admin = admin;
        _adminModel = adminModel;

        final Widget buttonPanel = createButtonPanel();
        addNorth(buttonPanel, 2.5);

        _cellTable = createCellTable();

        final ScrollPanel scrollPanel = new ScrollPanel(_cellTable);
        {
            final Style style = scrollPanel.getElement().getStyle();
            style.setPadding(10, Style.Unit.PX);
        }

        add(scrollPanel);
        _adminModel.getEventBus().addHandler(ApplicationsChangedEvent.TYPE, this);
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

    private void whenNewApplication() {
        final BaseDialog baseDialog = new BaseDialog("New Application") {

            private TextBox _textBox;

            @Override
            protected void whenOk() {
                final String applicationName = _textBox.getText();
                if (!applicationName.isEmpty()) {
                    AdminService.App.getInstance().createApplication(applicationName, new AdvancedAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            _adminModel.add(applicationName);
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
        _admin.editApplication(applicationName);
    }

    private void whenDelete(final String applicationName) {
        final boolean confirm = Window.confirm("" +
                "This operation will delete the application including the associated bugs.\n" +
                "This operation is not reversible\n" +
                "Are you sure you want  to delete '" + applicationName + "'?");
        if (confirm) {
            AdminService.App.getInstance().deleteApplication(applicationName, new AdvancedAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    _adminModel.delete(applicationName);
                }
            });
        }
    }

    @Override
    public void onApplicationsChanged(ApplicationsChangedEvent event) {
        final List<String> applications = event.getApplications();
        _cellTable.setRowData(applications);
    }
}

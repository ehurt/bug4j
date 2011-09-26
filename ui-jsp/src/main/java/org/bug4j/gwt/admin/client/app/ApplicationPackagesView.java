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

package org.bug4j.gwt.admin.client.app;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import org.bug4j.gwt.admin.client.AdminService;
import org.bug4j.gwt.admin.client.ApplicationView;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.CommonService;
import org.bug4j.gwt.common.client.data.AppPkg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class ApplicationPackagesView {
    private final ApplicationView _applicationView;
    private CellTable<AppPkg> _cellTable;
    private List<AppPkg> _pkgs;

    public ApplicationPackagesView(ApplicationView applicationView) {
        _applicationView = applicationView;
    }

    public Widget createWidget() {
        _cellTable = createCellTable();

        final ScrollPanel scrollPanel = new ScrollPanel(_cellTable);
        {
            final Style style = scrollPanel.getElement().getStyle();
            style.setPadding(10, Style.Unit.PX);
        }

        refreshData();

        return scrollPanel;
    }

    private void refreshData() {
        final String applicationName = _applicationView.getApplicationName();
        CommonService.App.getInstance().getPackages(applicationName, new AdvancedAsyncCallback<List<AppPkg>>() {
            @Override
            public void onSuccess(List<AppPkg> pkgs) {
                _pkgs = pkgs;
                updateTableModel();
            }
        });
    }

    private CellTable<AppPkg> createCellTable() {
        final CellTable<AppPkg> cellTable = new CellTable<AppPkg>();

        {
            final Label emptyTableWidget = new Label("No Packages");
            emptyTableWidget.getElement().getStyle().setFontSize(20, Style.Unit.PT);
            cellTable.setEmptyTableWidget(emptyTableWidget);
        }

        {
            final TextInputCell textInputCell = new TextInputCell();
            final Column<AppPkg, String> column = new Column<AppPkg, String>(textInputCell) {
                @Override
                public String getValue(AppPkg appPkg) {
                    return appPkg.getPkg();
                }
            };
            column.setFieldUpdater(new FieldUpdater<AppPkg, String>() {
                @Override
                public void update(int index, AppPkg appPkg, String value) {
                    appPkg.setPkg(value);
                    whenDataChanges();
                }
            });
            cellTable.addColumn(column, "Package");
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

            final Column<AppPkg, String> column = new Column<AppPkg, String>(clickableTextCell) {
                @Override
                public String getValue(AppPkg appPkg) {
                    final String pkg = appPkg.getPkg();
                    final String ret = pkg.isEmpty() ? "Add" : "Delete";
                    return ret;
                }
            };

            column.setFieldUpdater(new FieldUpdater<AppPkg, String>() {
                @Override
                public void update(int index, AppPkg object, String value) {
                    final int i = _pkgs.size() - 1;
                    if (index == i) {
                        if (!object.getPkg().isEmpty()) {
                            Collections.sort(_pkgs);
                            whenDataChanges();
                            updateTableModel();
                        }
                    } else {
                        _pkgs.remove(index);
                        whenDataChanges();
                        updateTableModel();
                    }
                }
            });

            cellTable.addColumn(column);
        }

        return cellTable;
    }

    private void whenDataChanges() {
        final String applicationName = _applicationView.getApplicationName();
        AdminService.App.getInstance().setPackages(applicationName, _pkgs, new AdvancedAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }
        });
    }

    private void updateTableModel() {
        final TreeSet<AppPkg> newPkgs = new TreeSet<AppPkg>();
        newPkgs.addAll(_pkgs);
        final AppPkg empty = new AppPkg("");
        newPkgs.remove(empty);
        _pkgs = new ArrayList<AppPkg>(newPkgs);
        _pkgs.add(empty);
        _cellTable.setRowData(_pkgs);
    }
}

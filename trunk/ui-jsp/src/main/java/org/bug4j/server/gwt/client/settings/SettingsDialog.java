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

package org.bug4j.server.gwt.client.settings;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.bug4j.server.gwt.client.Bug4j;
import org.bug4j.server.gwt.client.Bug4jService;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SettingsDialog extends BaseDialog {
    private Grid _grid;
    private final List<String> _packages = new ArrayList<String>();
    private TextBox _textBox;

    public SettingsDialog(Bug4j bug4j) {
        super("Setup", bug4j);
    }

    @Override
    protected Widget createContent() {
        final TabLayoutPanel ret = new TabLayoutPanel(2, Style.Unit.EM);
        ret.setSize("400px", "400px");
        ret.add(createPackagesPanel(), "Packages");
        return ret;
    }

    @Override
    protected void onLoad() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                _textBox.setFocus(true);
            }
        });
    }

    private Widget createPackagesPanel() {
        final DockLayoutPanel ret = new DockLayoutPanel(Style.Unit.EM);
        final DockLayoutPanel addPanel = new DockLayoutPanel(Style.Unit.PX);
        {
            _textBox = new TextBox();
            final Button addButton = new Button("Add");
            addPanel.addEast(addButton, 75);
            addPanel.add(_textBox);

            addButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    whenAdd();
                }
            });
        }

        ret.addSouth(addPanel, 2);
        _grid = new Grid(0, 2);
        _grid.setWidth("396px");
        final HTMLTable.ColumnFormatter columnFormatter = _grid.getColumnFormatter();
        columnFormatter.setWidth(0, "380px");
        columnFormatter.setWidth(1, "16px");
        ret.add(_grid);
        loadPackageData();

        return ret;
    }

    private void whenAdd() {
        final String packageName = _textBox.getText();
        if (!packageName.isEmpty()) {
            if (!_packages.contains(packageName)) {
                _packages.add(packageName);
                refreshUI();
            }
            _textBox.setText("");
        }
    }

    private void whenDelete(String appPackage) {
        _packages.remove(appPackage);
        refreshUI();
    }

    private void loadPackageData() {
        final Bug4j bug4j = getBug4j();
        final String application = bug4j.getApplication();
        if (application != null) {
            Bug4jService.App.getInstance().getPackages(application, new AsyncCallback<List<String>>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(List<String> appPackages) {
                    _packages.addAll(appPackages);
                    refreshUI();
                }
            });
        }
    }

    private void refreshUI() {
        _grid.clear();
        for (int i = 0; i < _packages.size(); i++) {
            final String appPackage = _packages.get(i);
            _grid.insertRow(i);
            final Label packageLabel = new Label(appPackage);
            packageLabel.setWidth("370px");
            final PushButton deleteButton = new PushButton(new Image(Bug4j.IMAGES.binClosed()));
            deleteButton.setStylePrimaryName("pkg-button");
            deleteButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    whenDelete(appPackage);
                }
            });
            _grid.setWidget(i, 0, packageLabel);
            _grid.setWidget(i, 1, deleteButton);
        }
    }

    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        boolean doAdd = false;
        if (Event.ONKEYDOWN == event.getTypeInt()) {
            if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                final String text = _textBox.getText();
                if (!text.isEmpty()) {
                    doAdd = true;
                }
            }
        }
        if (doAdd) {
            whenAdd();
        } else {
            super.onPreviewNativeEvent(event);
        }
    }

    @Override
    protected void whenOk() {
        final Bug4j bug4j = getBug4j();
        final String application = bug4j.getApplication();
        if (application != null) {
            Bug4jService.App.getInstance().setPackages(application, _packages, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    hide(false);
                }
            });
        }
    }
}

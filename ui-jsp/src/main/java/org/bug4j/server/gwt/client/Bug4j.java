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

package org.bug4j.server.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.bug4j.server.gwt.client.bugs.BugView;
import org.bug4j.server.gwt.client.bugs.HotBugsGraphView;
import org.bug4j.server.gwt.client.settings.ApplicationDialog;
import org.bug4j.server.gwt.client.settings.SettingsDialog;
import org.bug4j.server.gwt.client.util.PropertyListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class Bug4j implements EntryPoint {
    public static final Resources IMAGES = GWT.create(Resources.class);
    private static List<String> _appPackages;
    private String _application;
    private final List<PropertyListener<String>> _applicationListeners = new ArrayList<PropertyListener<String>>();
    private MenuItem _appMenuButton;
    private String _userName;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.PX);
        final Widget northWidget = buildNorthWidget();
        dockLayoutPanel.addNorth(northWidget, 55);

        final TabLayoutPanel bugPanel = new TabLayoutPanel(2, Style.Unit.EM);
        final BugView bugView = new BugView(this);
        final HotBugsGraphView hotBugsGraphView = new HotBugsGraphView(this);

        bugPanel.add(bugView.createWidget(), "Bugs");
        bugPanel.add(hotBugsGraphView.createWidget(), "Top Chart");

        bugPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> integerSelectionEvent) {
                final int selectedItem = integerSelectionEvent.getSelectedItem();
                switch (selectedItem) {
                    case 0:
                        bugView.whenBugListChanges();
                        break;
                    case 1:
                        hotBugsGraphView.whenBugListChanges();
                        break;
                }
            }
        });

        dockLayoutPanel.add(bugPanel);

        RootLayoutPanel.get().add(dockLayoutPanel);
        final Element loadingElement = DOM.getElementById("loading");
        DOM.removeChild(DOM.getParent(loadingElement), loadingElement);
    }

    private Widget buildNorthWidget() {
        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.EM);

        final FlowPanel flowPanel = new FlowPanel();
        final MenuBar menuBar = new MenuBar();
        final MenuBar popup = new MenuBar(true);
        popup.addItem("Application...", new Command() {
            @Override
            public void execute() {
                whenApplication();
            }
        });
        popup.addItem("Settings...", new Command() {
            @Override
            public void execute() {
                whenSettings();
            }
        });
        popup.addSeparator();
        popup.addItem("Export", new Command() {
            @Override
            public void execute() {
                whenExport();
            }
        });
        popup.addItem("Import", new Command() {
            @Override
            public void execute() {
                whenImport();
            }
        });
        popup.addSeparator();
        popup.addItem("Logout", new Command() {
            @Override
            public void execute() {
                whenLogout();
            }
        });

        _appMenuButton = menuBar.addItem("-", popup);
        _appMenuButton.getElement().setId("dd-menu");

        final Style style = menuBar.getElement().getStyle();
        style.setFloat(Style.Float.RIGHT);
        style.setMarginRight(5, Style.Unit.PX);

        flowPanel.add(menuBar);

        dockLayoutPanel.addLineEnd(flowPanel, 20);
        final HTML html = new HTML("<img src=\"../icons/splat.png\"/><span class=\"logo\">Bug4J</span>");
        dockLayoutPanel.add(html);

        Bug4jService.App.getInstance().getDefaultApplication(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(final String applicationName) {
                setApplication(applicationName);
            }
        });
        Bug4jService.App.getInstance().getUserName(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(String userName) {
                setUserName(userName);
            }
        });
        return dockLayoutPanel;
    }

    private void whenImport() {
        Window.alert("Not implemented yet");
    }

    private void whenExport() {
        final String moduleBaseURL = GWT.getModuleBaseURL();
        final String application = getApplication();
        if (application != null) {
            Window.open(moduleBaseURL + "../bug/export?a=" + application, "_self", "");
        }
    }

    private void whenLogout() {
        Window.open("../ui/logout.jsp", "_self", "");
    }

    private void whenSettings() {
        final SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.show();
    }

    private void whenApplication() {
        final ApplicationDialog applicationDialog = new ApplicationDialog(this);
        applicationDialog.show();
    }

    public synchronized void withAppPackages(final AsyncCallback<List<String>> asyncCallback) {
        if (_appPackages == null) {
            Bug4jService.App.getInstance().getPackages(_application, new AsyncCallback<List<String>>() {
                @Override
                public void onFailure(Throwable caught) {
                    asyncCallback.onFailure(caught);
                }

                @Override
                public void onSuccess(List<String> result) {
                    _appPackages = result;
                    asyncCallback.onSuccess(_appPackages);
                }
            });
        } else {
            asyncCallback.onSuccess(_appPackages);
        }
    }

    public String getApplication() {
        return _application;
    }

    public void setApplication(String application) {
        _application = application;
        updateAppButtonText();
        for (PropertyListener<String> applicationListener : _applicationListeners) {
            applicationListener.propertyChanged("application", application);
        }
    }

    public void addApplicationListener(PropertyListener<String> listener) {
        _applicationListeners.add(listener);
    }

    private void setUserName(String userName) {
        _userName = userName;
        updateAppButtonText();
    }

    private void updateAppButtonText() {
        final StringBuilder stringBuilder = new StringBuilder();
        if (_application != null) {
            stringBuilder.append(_application);
        } else {
            stringBuilder.append("< No Application >");
        }

        stringBuilder.append(" - ");
        if (_userName != null) {
            stringBuilder.append(_userName);
        }

        _appMenuButton.setText(stringBuilder.toString());
    }
}

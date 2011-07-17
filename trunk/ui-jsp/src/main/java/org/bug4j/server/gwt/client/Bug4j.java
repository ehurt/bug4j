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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.bug4j.server.gwt.client.bugs.BugView;
import org.bug4j.server.gwt.client.graphs.TopGraphView;
import org.bug4j.server.gwt.client.settings.SettingsDialog;
import org.bug4j.server.gwt.client.util.PropertyListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class Bug4j implements EntryPoint {
    public static final Resources IMAGES = GWT.create(Resources.class);
    private static List<String> _appPackages;
    private String _application;
    private final List<PropertyListener<String>> _propertyListeners = new ArrayList<PropertyListener<String>>();
    private Label _applicationLabel;
    private Label _userLabel;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        Bug4jService.App.getInstance().getUserName(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                whenLogout();
            }

            @Override
            public void onSuccess(String username) {
                //TODO: Use that username
                try {
                    initialize();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initialize() {
        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.PX);
        final Widget northWidget = buildNorthWidget();
        dockLayoutPanel.addNorth(northWidget, 35);

        final TabLayoutPanel bugPanel = new TabLayoutPanel(2, Style.Unit.EM);
        final BugView bugView = new BugView(this);
        final TopGraphView topGraphView = new TopGraphView(this);

        bugPanel.add(bugView.createWidget(), "Bugs");
        bugPanel.add(topGraphView.createWidget(), "Charts");

        bugPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> integerSelectionEvent) {
                final int selectedItem = integerSelectionEvent.getSelectedItem();
                switch (selectedItem) {
                    case 0:
                        bugView.whenBugListChanges();
                        break;
                    case 1:
                        topGraphView.whenBugListChanges();
                        break;
                }
            }
        });

        dockLayoutPanel.add(bugPanel);

        RootLayoutPanel.get().add(dockLayoutPanel);
        final Element loadingElement = DOM.getElementById("loading");
        DOM.removeChild(DOM.getParent(loadingElement), loadingElement);

        Bug4jService.App.getInstance().getDefaultApplication(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to retrieve the default application");
            }

            @Override
            public void onSuccess(String appName) {
                setApplication(appName);
                Bug4jService.App.getInstance().getUserName(new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Failed to retrieve the user name");
                    }

                    @Override
                    public void onSuccess(String userName) {
                        setUserName(userName);
                    }
                });
            }
        });
    }

    private Widget buildNorthWidget() {
        final Image image = new Image(IMAGES.littleSplat());

        _applicationLabel = new Label("");
        _applicationLabel.setStylePrimaryName("headerDropDown");
        _applicationLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenAppClicked(_applicationLabel);
            }
        });

        _userLabel = new Label("");
        _userLabel.setStylePrimaryName("headerDropDown");
        _userLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenUserClicked(_userLabel);
            }
        });

        final FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(_applicationLabel);
        flowPanel.add(_userLabel);

        final LayoutPanel layoutPanel = new LayoutPanel();
        layoutPanel.add(image);
        layoutPanel.add(flowPanel);

        layoutPanel.setWidgetLeftWidth(image, 0, Style.Unit.PX, 110, Style.Unit.PX);
        layoutPanel.setWidgetHorizontalPosition(flowPanel, Layout.Alignment.END);
        return layoutPanel;
    }

    private void whenUserClicked(Label userLabel) {
        final PopupPanel popupPanel = new PopupPanel(true, true);
        final MenuBar popup = new MenuBar(true);
        popup.addItem("Settings...", new Command() {
            @Override
            public void execute() {
                whenSettings();
                popupPanel.hide();
            }
        });
        popup.addSeparator();
        popup.addItem("Export", new Command() {
            @Override
            public void execute() {
                whenExport();
                popupPanel.hide();
            }
        });
        popup.addItem("Import", new Command() {
            @Override
            public void execute() {
                whenImport();
                popupPanel.hide();
            }
        }).setEnabled(false);
        popup.addSeparator();
        popup.addItem("Logout", new Command() {
            @Override
            public void execute() {
                whenLogout();
                popupPanel.hide();
            }
        });
        popup.setWidth("9em");
        popupPanel.setWidget(popup);

        popupPanel.setPopupPosition(
                userLabel.getAbsoluteLeft() + userLabel.getOffsetWidth() - 75 - (12 + 4),
                userLabel.getAbsoluteTop() + userLabel.getOffsetHeight());
        popupPanel.setSize("75px", "9em");
        popupPanel.show();
    }

    private void whenAppClicked(final Label app) {

        Bug4jService.App.getInstance().getApplications(new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to get the list of applications");
            }

            @Override
            public void onSuccess(List<String> appNames) {
                showApplicationPopupPanel(app, appNames);
            }
        });
    }

    private void showApplicationPopupPanel(Label app, List<String> appNames) {
        final PopupPanel popupPanel = new PopupPanel(true, true);
        popupPanel.setPopupPosition(
                app.getAbsoluteLeft() + app.getOffsetWidth() - 150 - (12 + 4),
                app.getAbsoluteTop() + app.getOffsetHeight());
        popupPanel.setSize("200px", appNames.size() + 3 + "em");

        if (!appNames.isEmpty()) {
            final VerticalPanel verticalPanel = new VerticalPanel();
            final Label popupTitle = new Label("Application:");
            popupTitle.setStylePrimaryName("headerSelTitle");
            verticalPanel.add(popupTitle);
            for (final String appName : appNames) {
                final Label appLabel = new Label(appName);
                appLabel.setStylePrimaryName("headerSel");
                verticalPanel.add(appLabel);
                appLabel.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        whenAppSelectionChanges(appName);
                        popupPanel.hide();
                    }
                });
            }
            popupPanel.setWidget(verticalPanel);
        } else {
            popupPanel.setWidget(new Label("There are no applications configured in the system"));
        }
        popupPanel.show();
    }

    private void whenAppSelectionChanges(final String appName) {
        Bug4jService.App.getInstance().setDefaultApplication(appName, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                setApplication(appName);
            }
        });
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
        Window.open("j_spring_security_logout", "_self", "");
    }

    private void whenSettings() {
        final SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.show();
        settingsDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                refreshPackages();
            }
        });
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

    public void setApplication(final String application) {
        if (_application == null || !_application.equals(application)) {
            _application = application;
            updateAppButtonText();
            refreshPackages();
            firePropertyChange(PropertyListener.APPLICATION, application);
        }
    }

    private void refreshPackages() {
        Bug4jService.App.getInstance().getPackages(_application, new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(List<String> result) {
                setPackages(result);
            }
        });
    }

    private void setPackages(List<String> result) {
        _appPackages = result;
        firePropertyChange(PropertyListener.PACKAGES, null);
    }

    private void firePropertyChange(String property, @Nullable String value) {
        for (PropertyListener<String> applicationListener : _propertyListeners) {
            applicationListener.propertyChanged(property, value);
        }
    }

    public void addPropertyListener(PropertyListener<String> listener) {
        _propertyListeners.add(listener);
    }

    private void setUserName(String userName) {
        final String userName1 = userName;
        _userLabel.setText(userName1);
    }

    private void updateAppButtonText() {
        _applicationLabel.setText(_application == null ? "< No Application >" : _application);
    }
}

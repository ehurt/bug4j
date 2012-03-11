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

package org.bug4j.gwt.user.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.CommonService;
import org.bug4j.gwt.common.client.Header;
import org.bug4j.gwt.common.client.util.PopupMenu;
import org.bug4j.gwt.user.client.event.ApplicationChangedEvent;
import org.bug4j.gwt.user.client.event.ApplicationChangedEventHandler;
import org.bug4j.gwt.user.client.settings.UserDialog;

import java.util.List;

/**
 */
public class UserHeader extends Header {
    private final BugModel _bugModel;

    public UserHeader(BugModel bugModel) {
        super(bugModel, true);
        _bugModel = bugModel;
        _bugModel.getEventBus().addHandler(ApplicationChangedEvent.TYPE, new ApplicationChangedEventHandler() {
            @Override
            public void onApplicationChanged(ApplicationChangedEvent event) {
                final String applicationName = event.getApplicationName();
                setApplicationText(applicationName);
            }
        });
        setApplicationText(bugModel.getApplication());
    }

    @Override
    protected void whenAppClicked(final Label applicationLabel) {
        CommonService.App.getInstance().getApplications(new AdvancedAsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> appNames) {
                showApplicationPopupPanel(applicationLabel, appNames);
            }
        });
    }

    @Override
    protected void whenUserClicked(Label userLabel) {
        final PopupMenu popupMenu = new PopupMenu();
        popupMenu.addItem("User Settings...", new Command() {
            @Override
            public void execute() {
                whenUserSettings();
            }
        });
        if (_bugModel.isAdmin()) {
            popupMenu.addItem("Administration...", new Command() {
                @Override
                public void execute() {
                    whenAdministration();
                }
            });
        }
        popupMenu.addSeparator();
        popupMenu.addItem("Download", new Command() {
            @Override
            public void execute() {
                whenExport();
            }
        });
        popupMenu.addSeparator();
        popupMenu.addItem("Logout", new Command() {
            @Override
            public void execute() {
                whenLogout();
            }
        });
        popupMenu.show(userLabel);
    }

    private void showApplicationPopupPanel(Label app, List<String> appNames) {
        final PopupPanel popupPanel = new PopupPanel(true, true);
        popupPanel.setPopupPosition(
                app.getAbsoluteLeft() + app.getOffsetWidth() - 150 - (12 + 4),
                app.getAbsoluteTop() + app.getOffsetHeight());
        popupPanel.setSize("200px", appNames.size() + 3 + "em");

        if (!appNames.isEmpty()) {
            final FlowPanel panel = new FlowPanel();
            final Label popupTitle = new Label("Application:");
            popupTitle.setStylePrimaryName("headerSelTitle");
            panel.add(popupTitle);
            for (final String appName : appNames) {
                final Label appLabel = new Label(appName);
                appLabel.setStylePrimaryName("headerSel");
                panel.add(appLabel);
                appLabel.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        setApplicationName(appName);
                        popupPanel.hide();
                    }
                });
            }
            popupPanel.setWidget(panel);
        } else {
            popupPanel.setWidget(new Label("There are no applications configured in the system"));
        }
        popupPanel.show();
    }

    private void setApplicationName(final String appName) {
        Bug4jService.App.getInstance().setDefaultApplication(appName, new AdvancedAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }
        });
        _bugModel.setApplication(appName);
    }

    private void whenUserSettings() {
        final UserDialog userDialog = new UserDialog();
        userDialog.show();
    }

    private void whenAdministration() {
        final String moduleBaseURL = GWT.getModuleBaseURL();
        Window.open(moduleBaseURL + "../admin.html", "_self", "");
    }

    private void whenExport() {
        final String moduleBaseURL = GWT.getModuleBaseURL();
        final String application = _bugModel.getApplication();
        if (application != null) {
            Window.open(moduleBaseURL + "../user/download?a=" + application, "_self", "");
        }
    }

    private void whenLogout() {
        Window.open("j_spring_security_logout", "_self", "");
    }
}

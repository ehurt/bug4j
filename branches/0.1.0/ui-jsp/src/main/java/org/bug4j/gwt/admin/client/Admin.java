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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.admin.client.event.ApplicationsChangedEvent;
import org.bug4j.gwt.admin.client.event.ApplicationsChangedEventHandler;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.CommonService;
import org.bug4j.gwt.common.client.CommonServiceAsync;
import org.bug4j.gwt.common.client.data.UserAuthorities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main entry point of the Admin application.
 */
public class Admin implements EntryPoint {
    private SimpleLayoutPanel _centerLayoutPanel;
    private SimpleLayoutPanel _navigationHost;

    private UserView _userView;
    private ApplicationsView _applicationsView;
    private ApplicationView _applicationView;
    private Label _usersLabel = new Label("Users");
    private Label _applicationsLabel = new Label("Applications");
    private Label _selectedLabel;
    private final Map<String, Label> _applicationLabels = new HashMap<String, Label>();

    public void onModuleLoad() {
        final CommonServiceAsync app = CommonService.App.getInstance();
        app.getUserAuthorities(new AdvancedAsyncCallback<UserAuthorities>() {
            @Override
            public void onSuccess(final UserAuthorities userAuthorities) {
                app.getApplications(new AdvancedAsyncCallback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> applications) {
                        try {
                            init(userAuthorities, applications);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void init(UserAuthorities userAuthorities, List<String> applications) {
        if (!userAuthorities.isAdmin()) {
            // Not an admin, get out of there
            Window.open("/", "_self", "");
            return;
        }

        final AdminModel adminModel = new AdminModel(userAuthorities, applications);

        _userView = new UserView();
        _applicationsView = new ApplicationsView(this, adminModel);
        _applicationView = new ApplicationView();

        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.PX);
        final AdminHeader header = new AdminHeader(adminModel);
        header.setStyleName("admin-header");

        dockLayoutPanel.addNorth(header, 40);

        final SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel(4);

        _navigationHost = new SimpleLayoutPanel();
        splitLayoutPanel.addWest(_navigationHost, 200);

        _centerLayoutPanel = new SimpleLayoutPanel();
        splitLayoutPanel.add(_centerLayoutPanel);
        dockLayoutPanel.add(splitLayoutPanel);

        _usersLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editUsers();
            }
        });
        _usersLabel.setStylePrimaryName("admin-label");

        _applicationsLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editApplications();
            }
        });
        _applicationsLabel.setStylePrimaryName("admin-label");

        adminModel.getEventBus().addHandler(ApplicationsChangedEvent.TYPE, new ApplicationsChangedEventHandler() {
            @Override
            public void onApplicationsChanged(ApplicationsChangedEvent event) {
                final List<String> applications = event.getApplications();
                refreshNavigation(applications);
            }
        });

        adminModel.fireInitializationEvents();
        editUsers();

        final RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
        rootLayoutPanel.add(dockLayoutPanel);
        final Element loadingElement = DOM.getElementById("loading");
        DOM.removeChild(DOM.getParent(loadingElement), loadingElement);
    }

    private void editUsers() {
        setLabelSelected(_usersLabel);
        _centerLayoutPanel.setWidget(_userView);
    }

    private void editApplications() {
        setLabelSelected(_applicationsLabel);
        _centerLayoutPanel.setWidget(_applicationsView);
    }

    public void editApplication(String applicationName) {
        final Label applicationLabel = _applicationLabels.get(applicationName);
        editApplication(applicationLabel);
    }

    private void editApplication(Label label) {
        setLabelSelected(label);
        _centerLayoutPanel.setWidget(_applicationView);
        final String applicationName = label.getText();
        _applicationView.show(applicationName);
    }

    private void refreshNavigation(List<String> applicationNames) {
        final FlowPanel flowPanel = new FlowPanel();

        flowPanel.add(_usersLabel);
        flowPanel.add(_applicationsLabel);

        _applicationLabels.clear();
        for (final String applicationName : applicationNames) {
            final Label label = new Label(applicationName);
            label.setStylePrimaryName("admin-label");
            label.addStyleName("admin-label-app");
            label.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    editApplication(label);
                }
            });
            _applicationLabels.put(applicationName, label);
            flowPanel.add(label);
        }

        _navigationHost.setWidget(flowPanel);
    }

    private void setLabelSelected(Label label) {
        if (_selectedLabel != null) {
            _selectedLabel.setStyleDependentName("selected", false);
        }

        _selectedLabel = label;

        if (_selectedLabel != null) {
            _selectedLabel.setStyleDependentName("selected", true);
        }
    }
}

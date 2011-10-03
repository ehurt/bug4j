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
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.common.client.*;
import org.bug4j.gwt.common.client.data.UserAuthorities;
import org.bug4j.gwt.common.client.util.PopupMenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class Admin implements EntryPoint {
    public static final Resources IMAGES = GWT.create(Resources.class);
    private SimpleLayoutPanel _centerLayoutPanel;
    private AdminView _currentAdminView;
    private UserAuthorities _userAuthorities;
    private final Collection<ApplicationView> _applicationViews = new ArrayList<ApplicationView>();
    private SimpleLayoutPanel _navigationHost;
    private final UserView _userView = new UserView();
    private final ApplicationsView _applicationsView = new ApplicationsView(this);
    private Header _header;

    public void onModuleLoad() {
        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.PX);
        _header = new Header(false) {
            @Override
            protected void whenUserClicked(Label userLabel) {
                Admin.this.whenUserClicked(userLabel);
            }
        };
        _header.setStyleName("admin-header");

        dockLayoutPanel.addNorth(_header, 40);

        final SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel(4);

        _navigationHost = new SimpleLayoutPanel();
        splitLayoutPanel.addWest(_navigationHost, 200);
        refreshNavigation(_userView);

        _centerLayoutPanel = new SimpleLayoutPanel();
        splitLayoutPanel.add(_centerLayoutPanel);
        dockLayoutPanel.add(splitLayoutPanel);

        final RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
        rootLayoutPanel.add(dockLayoutPanel);
        final Element loadingElement = DOM.getElementById("loading");
        DOM.removeChild(DOM.getParent(loadingElement), loadingElement);

        CommonService.App.getInstance().getUserAuthorities(new AdvancedAsyncCallback<UserAuthorities>() {
            @Override
            public void onSuccess(UserAuthorities userAuthorities) {
                if (userAuthorities.isAdmin()) {
                    setUserAuthorities(userAuthorities);
                } else {
                    whenLogout();
                }
            }
        });
    }

    private Widget buildNavigation(final AdminView defaultView) {
        final FlowPanel flowPanel = new FlowPanel();

        CommonService.App.getInstance().getApplications(new AdvancedAsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> applicationNames) {
                addNavigationLabel(flowPanel, _userView);
                addNavigationLabel(flowPanel, _applicationsView);

                _applicationViews.clear();
                for (String applicationName : applicationNames) {
                    final ApplicationView applicationView = new ApplicationView(applicationName);
                    addNavigationLabel(flowPanel, applicationView);
                    _applicationViews.add(applicationView);
                }
                setView(defaultView);
            }
        });

        return flowPanel;
    }

    private void addNavigationLabel(final FlowPanel flowPanel, final AdminView adminView) {
        final Label label = adminView.getLabel();
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setView(adminView);
            }
        });
        flowPanel.add(adminView.getLabel());
    }

    private void setView(AdminView newView) {
        if (_currentAdminView != null) {
            final Widget widget = _currentAdminView.getWidget();
            widget.removeFromParent();

            _currentAdminView.setSelected(false);
        }

        _currentAdminView = newView;

        if (_currentAdminView != null) {
            final Widget widget = _currentAdminView.getWidget();
            _centerLayoutPanel.setWidget(widget);
            _currentAdminView.setSelected(true);
        }
    }

    private void whenUserNameChanged(String userName) {
        _header.setUserText(userName);
    }

    private void whenUserClicked(Label userLabel) {
        final PopupMenu popupMenu = new PopupMenu();
        popupMenu.addItem("Export", new Command() {
            @Override
            public void execute() {
                whenExport();
            }
        });
        popupMenu.addItem("Import", new Command() {
            @Override
            public void execute() {
                whenImport();
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

    private void whenImport() {

        final DialogBox dialogBox = new BaseDialog("Import") {

            private FormPanel _formPanel;
            private PopupPanel _loadingPanel;

            @Override
            protected Widget createContent() {
                final String moduleBaseURL = GWT.getModuleBaseURL();
                final FileUpload fileUpload = new FileUpload();
                fileUpload.setName("upload");
                _formPanel = new FormPanel();
                final String action = moduleBaseURL + "../admin/import";
                _formPanel.setAction(action);
                _formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
                _formPanel.setMethod(FormPanel.METHOD_POST);

                _formPanel.add(fileUpload);

                _formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
                    @Override
                    public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                        whenApplicationsChanged();
                        _loadingPanel.hide();
                        hide();
                    }
                });

                return _formPanel;
            }

            @Override
            protected void whenOk() {
                // Cannot hide the panel or the submit will fail.
                setPopupPosition(-1000, -1000);
                _formPanel.submit();

                _loadingPanel = new PopupPanel();
                final FlowPanel flowPanel = new FlowPanel();
                flowPanel.add(new Image(IMAGES.loading32x32()));
                flowPanel.add(new Label("Importing..."));
                _loadingPanel.setWidget(flowPanel);
                _loadingPanel.center();
            }
        };
        dialogBox.center();
    }

    private void whenExport() {
        final String moduleBaseURL = GWT.getModuleBaseURL();
        Window.open(moduleBaseURL + "../user/export", "_self", "");
    }

    private void whenLogout() {
        Window.open("j_spring_security_logout", "_self", "");
    }

    public UserAuthorities getUserAuthorities() {
        return _userAuthorities;
    }

    private void setUserAuthorities(UserAuthorities userAuthorities) {
        _userAuthorities = userAuthorities;
        final String userName = _userAuthorities.getUserName();
        whenUserNameChanged(userName);
    }

    public void edit(String applicationName) {
        for (ApplicationView applicationView : _applicationViews) {
            if (applicationView.getApplicationName().equals(applicationName)) {
                setView(applicationView);
                break;
            }
        }
    }

    private void refreshNavigation(AdminView defaultView) {
        final Widget navigationWidget = buildNavigation(defaultView);
        _navigationHost.setWidget(navigationWidget);
    }

    public void whenApplicationsChanged() {
        refreshNavigation(_applicationsView);
        _applicationsView.refreshData();
    }
}

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
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.common.client.BaseDialog;
import org.bug4j.gwt.common.client.CommonService;
import org.bug4j.gwt.common.client.Resources;
import org.bug4j.gwt.common.client.data.UserAuthorities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class Admin implements EntryPoint {
    public static final Resources IMAGES = GWT.create(Resources.class);
    private Label _userLabel;
    private SimpleLayoutPanel _centerLayoutPanel;
    private AdminView _currentAdminView;
    private UserAuthorities _userAuthorities;
    private final Collection<ApplicationView> _applicationViews = new ArrayList<ApplicationView>();
    private SimpleLayoutPanel _navigationHost;
    private final UserView _userView = new UserView();
    private final ApplicationsView _applicationsView = new ApplicationsView(this);

    public void onModuleLoad() {
        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.PX);
        final Widget northWidget = buildNorthWidget();
        dockLayoutPanel.addNorth(northWidget, 35);

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

        CommonService.App.getInstance().getUserAuthorities(new AsyncCallback<UserAuthorities>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to getUserName()");
            }

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

        CommonService.App.getInstance().getApplications(new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to get the list of applications");
            }

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
        _userLabel.setText(userName);
    }

    private Widget buildNorthWidget() {
        final Image image = new Image(IMAGES.littleSplat());

        _userLabel = new Label("");
        _userLabel.setStylePrimaryName("headerDropDown");
        _userLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenUserClicked(_userLabel);
            }
        });

        final FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(_userLabel);

        final LayoutPanel layoutPanel = new LayoutPanel();
        layoutPanel.add(image);
        layoutPanel.add(flowPanel);

        layoutPanel.setWidgetLeftWidth(image, 0, Style.Unit.PX, 110, Style.Unit.PX);
        layoutPanel.setWidgetHorizontalPosition(flowPanel, Layout.Alignment.END);
        layoutPanel.setStylePrimaryName("admin-header");

        return layoutPanel;
    }

    private void whenUserClicked(Label userLabel) {
        final PopupPanel popupPanel = new PopupPanel(true, true);
        final MenuBar popup = new MenuBar(true);
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
        });
        popup.addSeparator();
        popup.addItem("Logout", new Command() {
            @Override
            public void execute() {
                whenLogout();
                popupPanel.hide();
            }
        });
        popupPanel.setWidget(popup);

        popupPanel.show();

        final int offsetWidth = popupPanel.getOffsetWidth();
        popupPanel.setPopupPosition(
                userLabel.getAbsoluteLeft() + userLabel.getOffsetWidth() - offsetWidth,
                userLabel.getAbsoluteTop() + userLabel.getOffsetHeight());
    }

    private void whenImport() {

        final DialogBox dialogBox = new BaseDialog("Import") {

            private FormPanel _formPanel;

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
                        hide();
                    }
                });

                return _formPanel;
            }

            @Override
            protected void whenOk() {
                _formPanel.submit();
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
    }
}

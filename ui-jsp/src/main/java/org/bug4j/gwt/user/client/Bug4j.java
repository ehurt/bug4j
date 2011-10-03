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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.CommonService;
import org.bug4j.gwt.common.client.Header;
import org.bug4j.gwt.common.client.data.UserAuthorities;
import org.bug4j.gwt.user.client.bugs.BugDetailView;
import org.bug4j.gwt.user.client.bugs.BugView;
import org.bug4j.gwt.user.client.data.Bug;
import org.bug4j.gwt.user.client.data.Filter;
import org.bug4j.gwt.user.client.event.ApplicationChangedEvent;
import org.bug4j.gwt.user.client.event.ApplicationChangedEventHandler;
import org.bug4j.gwt.user.client.event.BugListChanged;
import org.bug4j.gwt.user.client.graphs.TopGraphView;
import org.bug4j.gwt.user.client.settings.UserDialog;

import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class Bug4j implements EntryPoint {
    private final BugModel _bugModel = new BugModel();
    private UserAuthorities _userAuthorities;
    private Header _header;

    public static String createBugLink(long bugId) {
        return Window.Location
                .createUrlBuilder()
                .setParameter("bug", Long.toString(bugId))
                .buildString();
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        CommonService.App.getInstance().getUserAuthorities(new AdvancedAsyncCallback<UserAuthorities>() {
            @Override
            public void onSuccess(final UserAuthorities userAuthorities) {
                Bug4jService.App.getInstance().getDefaultApplication(new AdvancedAsyncCallback<String>() {
                    @Override
                    public void onSuccess(final String appName) {
                        try {
                            initialize(userAuthorities, appName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void initialize(UserAuthorities userAuthorities, String appName) {

        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.PX);
        _header = new Header(true) {
            @Override
            protected void whenAppClicked(Label applicationLabel) {
                Bug4j.this.whenAppClicked(applicationLabel);
            }

            @Override
            protected void whenUserClicked(Label userLabel) {
                Bug4j.this.whenUserClicked(userLabel);
            }
        };

        dockLayoutPanel.addNorth(_header, 35);

        Long bugId = getBugIdParam();
        if (bugId == null) {
            createTabbedContent(dockLayoutPanel);
        } else {
            createOneBugContent(dockLayoutPanel, bugId);
        }

        RootLayoutPanel.get().add(dockLayoutPanel);
        final Element loadingElement = DOM.getElementById("loading");
        DOM.removeChild(DOM.getParent(loadingElement), loadingElement);

        _bugModel.getEventBus().addHandler(ApplicationChangedEvent.TYPE, new ApplicationChangedEventHandler() {
            @Override
            public void onApplicationChanged(ApplicationChangedEvent event) {
                final String applicationName = event.getApplicationName();
                _header.setApplicationText(applicationName);
            }
        });

        setUserAuthorities(userAuthorities);
        _bugModel.setApplication(appName);
    }

    private static Long getBugIdParam() {
        Long bugId = null;
        final String bug = Window.Location.getParameter("bug");
        if (bug != null) {
            try {
                bugId = Long.parseLong(bug);
            } catch (NumberFormatException ignored) {
            }
        }
        return bugId;
    }

    private void createOneBugContent(DockLayoutPanel dockLayoutPanel, long bugId) {
        final BugDetailView bugDetailView = new BugDetailView(_bugModel);
        final Widget widget = bugDetailView.createWidget();
        dockLayoutPanel.add(widget);

        final Filter filter = new Filter();
        filter.setBugId(bugId);
        Bug4jService.App.getInstance().getBugs(null, filter, "", new AdvancedAsyncCallback<List<Bug>>() {
            @Override
            public void onSuccess(List<Bug> result) {
                if (!result.isEmpty()) {
                    final Bug bug = result.get(0);
                    final String app = bug.getApp();
                    _bugModel.setApplication(app);
                    bugDetailView.displayBug(bug);
                }
            }
        });
    }

    private void createTabbedContent(DockLayoutPanel dockLayoutPanel) {
        final TabLayoutPanel tabLayoutPanel = new TabLayoutPanel(2, Style.Unit.EM);
        final BugView bugView = new BugView(_bugModel);
        final TopGraphView topGraphView = new TopGraphView(_bugModel);

        final Widget bugViewWidget = bugView.createWidget();
        final Widget topGraphViewWidget = topGraphView.createWidget();
        tabLayoutPanel.add(bugViewWidget, "Bugs");
        tabLayoutPanel.add(topGraphViewWidget, "Charts");

        // Refresh the data when the tab selection changes
        tabLayoutPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> integerSelectionEvent) {
                _bugModel.getEventBus().fireEvent(new BugListChanged());
            }
        });

        // TODO: Find a better fix
        if (getUserAgent().contains("msie")) {
            // IE bug: The bug title does not appear (tested in ie8)
            tabLayoutPanel.selectTab(1);
            final Scheduler scheduler = Scheduler.get();
            scheduler.scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    tabLayoutPanel.selectTab(0);
                }
            });
        }

        dockLayoutPanel.add(tabLayoutPanel);
    }

    public static native String getUserAgent() /*-{
        return navigator.userAgent.toLowerCase();
    }-*/;

    private void whenUserClicked(Label userLabel) {
        final PopupPanel popupPanel = new PopupPanel(true, true);
        final MenuBar popup = new MenuBar(true);
        popup.addItem("User Settings...", new Command() {
            @Override
            public void execute() {
                whenUserSettings();
                popupPanel.hide();
            }
        });
        if (_userAuthorities.isAdmin()) {
            popup.addItem("Administration...", new Command() {
                @Override
                public void execute() {
                    whenAdministration();
                }
            });
        }
        popup.addSeparator();
        popup.addItem("Download", new Command() {
            @Override
            public void execute() {
                whenExport();
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

    private void whenUserSettings() {
        final UserDialog userDialog = new UserDialog();
        userDialog.show();
    }

    private void whenAdministration() {
        final String moduleBaseURL = GWT.getModuleBaseURL();
        Window.open(moduleBaseURL + "../admin.html", "_self", "");
    }

    private void whenAppClicked(final Label app) {
        CommonService.App.getInstance().getApplications(new AdvancedAsyncCallback<List<String>>() {
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

    private void setUserName(String userName) {
        _header.setUserText(userName);
    }

    public UserAuthorities getUserAuthorities() {
        return _userAuthorities;
    }

    private void setUserAuthorities(UserAuthorities userAuthorities) {
        _userAuthorities = userAuthorities;
        final String userName = _userAuthorities.getUserName();
        setUserName(userName);
    }
}

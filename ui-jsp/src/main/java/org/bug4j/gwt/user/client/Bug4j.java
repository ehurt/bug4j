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
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.CommonService;
import org.bug4j.gwt.common.client.Header;
import org.bug4j.gwt.common.client.data.UserAuthorities;
import org.bug4j.gwt.user.client.bugs.AllBugsView;
import org.bug4j.gwt.user.client.bugs.BugDetailView;
import org.bug4j.gwt.user.client.data.Bug;
import org.bug4j.gwt.user.client.data.Filter;

import java.util.List;

/**
 * The main entry point for the User module.
 */
public class Bug4j implements EntryPoint {
    private BugModel _bugModel;

    public void onModuleLoad() {
        CommonService.App.getInstance().getUserAuthorities(new AdvancedAsyncCallback<UserAuthorities>() {
            @Override
            public void onSuccess(final UserAuthorities userAuthorities) {
                Bug4jService.App.getInstance().getDefaultApplication(new AdvancedAsyncCallback<String>() {
                    @Override
                    public void onSuccess(final String appName) {
                        try {
                            Bug4jService.App.getInstance().getDefaultFilter(new AdvancedAsyncCallback<Filter>() {
                                @Override
                                public void onSuccess(Filter filter) {
                                    initialize(userAuthorities, appName, filter);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void initialize(UserAuthorities userAuthorities, String appName, Filter filter) {
        _bugModel = new BugModel(userAuthorities);
        _bugModel.setApplication(appName);

        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.PX);
        final Header header = new UserHeader(_bugModel);

        dockLayoutPanel.addNorth(header, 35);

        final Long bugId = getBugIdParam();
        if (bugId == null) {
            final AllBugsView allBugsView = new AllBugsView(_bugModel, filter);
            dockLayoutPanel.add(allBugsView);
        } else {
            final Widget oneBugContent = createOneBugContent(bugId);
            dockLayoutPanel.add(oneBugContent);
        }

        RootLayoutPanel.get().add(dockLayoutPanel);
        final Element loadingElement = DOM.getElementById("loading");
        DOM.removeChild(DOM.getParent(loadingElement), loadingElement);
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

    private Widget createOneBugContent(long bugId) {
        final BugDetailView ret = new BugDetailView(_bugModel);

        final Filter filter = new Filter();
        filter.setBugId(bugId);
        Bug4jService.App.getInstance().getBugs(null, filter, "", new AdvancedAsyncCallback<List<Bug>>() {
            @Override
            public void onSuccess(List<Bug> result) {
                if (!result.isEmpty()) {
                    final Bug bug = result.get(0);
                    final String app = bug.getApp();
                    _bugModel.setApplication(app);
                    ret.displayBug(bug);
                }
            }
        });
        return ret;
    }
}

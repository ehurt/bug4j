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

package org.bug4j.gwt.common.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.bug4j.gwt.common.client.data.UserAuthorities;
import org.bug4j.gwt.common.client.event.UserChangedEvent;
import org.bug4j.gwt.common.client.event.UserChangedEventHandler;

/**
 * The component at the top of the screen with the icon on the left and the [application] and user name on the right
 */
public abstract class Header extends DockLayoutPanel {
    public static final Resources IMAGES = GWT.create(Resources.class);
    private final Label _applicationLabel = new Label("");
    private final Label _userLabel = new Label("");

    @SuppressWarnings({"GWTStyleCheck"})
    public Header(CommonModel commonModel, boolean showApplicationMenu) {
        super(Style.Unit.EM);

        commonModel.getEventBus().addHandler(UserChangedEvent.TYPE, new UserChangedEventHandler() {
            @Override
            public void onUserChanged(UserChangedEvent event) {
                final UserAuthorities userAuthorities = event.getUserAuthorities();
                final String userName = userAuthorities.getUserName();
                setUserText(userName);
            }
        });
        final FlowPanel flowPanel = new FlowPanel();
        flowPanel.setStyleName("headerMenuPanel");

        final Image image = new Image(IMAGES.littleSwatter());
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final String url = GWT.getHostPageBaseURL();
                Window.open(url, "_self", "");
            }
        });
        image.getElement().getStyle().setCursor(Style.Cursor.POINTER);

        if (showApplicationMenu) {
            _applicationLabel.setStylePrimaryName("headerDropDown");
            _applicationLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    whenAppClicked(_applicationLabel);
                }
            });
            flowPanel.add(_applicationLabel);
        }

        final String userName = commonModel.getUserName();
        if (userName != null) {
            _userLabel.setText(userName);
        }
        _userLabel.setStylePrimaryName("headerDropDown");
        _userLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenUserClicked(_userLabel);
            }
        });

        flowPanel.add(_userLabel);

        addEast(flowPanel, 30);
        add(image);
    }

    protected void whenAppClicked(Label applicationLabel) {
    }

    protected abstract void whenUserClicked(Label userLabel);

    private void setUserText(String userName) {
        _userLabel.setText(userName);
    }

    public void setApplicationText(String applicationText) {
        _applicationLabel.setText(applicationText);
    }
}

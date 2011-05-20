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

package org.dandoy.bug4j.server.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class Bug4j implements EntryPoint {

    private DockPanel _dockPanel;
    private Widget _content;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        _dockPanel = new DockPanel();
        _dockPanel.addStyleName("dockpanel");

        {
            final TabBar tabBar = new TabBar();
            tabBar.addStyleName("tabbar");
            tabBar.addTab("Bugs");
            tabBar.addTab("Administer");
            tabBar.addSelectionHandler(new SelectionHandler<Integer>() {
                @Override
                public void onSelection(SelectionEvent<Integer> integerSelectionEvent) {
                    final Integer selectedItem = integerSelectionEvent.getSelectedItem();
                    switch (selectedItem) {
                        case 0:
                            whenBugs();
                            break;
                        case 1:
                            whenAdminister();
                            break;
                    }
                }

            });
            tabBar.selectTab(0);
            _dockPanel.add(tabBar, DockPanel.NORTH);
        }

        final RootPanel main = RootPanel.get();
        main.add(_dockPanel);

        final RootPanel userWidget = RootPanel.get("user-widget");
        final Label label = new Label("cdandoy");
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final PopupPanel popupPanel = new PopupPanel(true, true);
                popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                    public void setPosition(int offsetWidth, int offsetHeight) {
                        final int left = label.getAbsoluteLeft();
                        final int top = label.getAbsoluteTop() + label.getOffsetHeight();
                        popupPanel.setPopupPosition(left, top);
                    }
                });
                final MenuBar fooMenu = new MenuBar(true);
                Command cmd = new Command() {
                    public void execute() {
                        popupPanel.hide();
                        Window.alert("You selected a menu item!");
                    }
                };
                fooMenu.addItem("the", cmd);
                fooMenu.addItem("foo", cmd);
                fooMenu.addItem("menu", cmd);
                popupPanel.setWidget(fooMenu);
                popupPanel.show();
            }
        });
        userWidget.add(label);
    }

    private void whenBugs() {
        final HTML content = new HTML("BUGS BUGS BUGS");
        setContent(content);
    }

    private void whenAdminister() {
        final Widget content;
        content = new HTML("ADMIN ADMIN ADMIN");
        setContent(content);
    }

    private void setContent(Widget content) {
        if (_content != null) {
            _content.removeFromParent();
        }
        _content = content;
        if (_content != null) {
            _dockPanel.add(_content, DockPanel.CENTER);
        }
    }
}

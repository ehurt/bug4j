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

package org.bug4j.gwt.common.client.util;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class PopupMenu extends PopupPanel {

    private MenuBar _popup;

    public PopupMenu() {
        super(true, true);

        _popup = new MenuBar(true) {
            @Override
            public void selectItem(MenuItem item) {
                super.selectItem(item);
                if (item == null) {
                    PopupMenu.this.hide();
                }
            }
        };
        setWidget(_popup);
    }

    public MenuItem addItem(String text, Command cmd) {
        return _popup.addItem(text, cmd);
    }

    public void show(Widget widget) {
        show();
        final int offsetWidth = getOffsetWidth();
        setPopupPosition(
                widget.getAbsoluteLeft() + widget.getOffsetWidth() - offsetWidth,
                widget.getAbsoluteTop() + widget.getOffsetHeight());
    }

    public void addSeparator() {
        _popup.addSeparator();
    }
}

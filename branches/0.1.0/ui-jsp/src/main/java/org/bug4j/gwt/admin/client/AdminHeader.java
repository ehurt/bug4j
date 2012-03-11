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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import org.bug4j.gwt.common.client.Header;
import org.bug4j.gwt.common.client.util.PopupMenu;

/**
 * The horizontal panel at the top of the Admin module
 */
public class AdminHeader extends Header {
    private final AdminModel _adminModel;

    public AdminHeader(AdminModel adminModel) {
        super(adminModel, false);
        _adminModel = adminModel;
    }

    @Override
    protected void whenUserClicked(Label userLabel) {
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
        final DialogBox dialogBox = new ImportDialog(_adminModel);
        dialogBox.center();
    }

    private void whenExport() {
        final String moduleBaseURL = GWT.getModuleBaseURL();
        Window.open(moduleBaseURL + "../user/export", "_self", "");
    }

    private void whenLogout() {
        Window.open("j_spring_security_logout", "_self", "");
    }
}

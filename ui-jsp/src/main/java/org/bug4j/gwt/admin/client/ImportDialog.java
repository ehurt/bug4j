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
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.common.client.BaseDialog;
import org.bug4j.gwt.common.client.Header;

/**
 * The dialog box to import bug4j.xml
 */
class ImportDialog extends BaseDialog {
    private FormPanel _formPanel;
    private PopupPanel _loadingPanel;
    private final AdminModel _adminModel;

    public ImportDialog(AdminModel adminModel) {
        super("Import");
        _adminModel = adminModel;
    }

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
                _adminModel.refreshApplications();
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
        flowPanel.add(new Image(Header.IMAGES.loading32x32()));
        flowPanel.add(new Label("Importing..."));
        _loadingPanel.setWidget(flowPanel);
        _loadingPanel.center();
    }
}

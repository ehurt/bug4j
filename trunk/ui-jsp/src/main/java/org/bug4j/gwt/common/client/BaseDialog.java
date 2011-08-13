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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import org.jetbrains.annotations.Nullable;

/**
 */
public abstract class BaseDialog extends DialogBox {

    protected BaseDialog(String title) {
        setText(title);

        final Widget widget = createWidget();
        setWidget(widget);
        center();
    }

    protected abstract void whenOk();

    private void whenCancel() {
        hide(true);
    }

    protected Widget createButtons() {
        return createOkCancelButtons();
    }

    protected final Widget createOkCancelButtons() {
        return createButtons("OK", "Cancel");
    }

    protected final Widget createOkButton() {
        return createButtons("OK", null);
    }

    private Widget createButtons(final String okText, @Nullable final String cancelText) {
        final HorizontalPanel horizontalPanel = new HorizontalPanel();

        final Button ok = new Button(okText);
        ok.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenOk();
            }
        });
        ok.setWidth("75px");

        final Button cancel;
        if (cancelText != null) {
            cancel = new Button(cancelText);
            cancel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    whenCancel();
                }
            });
            cancel.setWidth("75px");
        } else {
            cancel = null;
        }

        horizontalPanel.setSpacing(5);
        horizontalPanel.add(ok);
        if (cancel != null) {
            horizontalPanel.add(cancel);
        }

        final HorizontalPanel ret = new HorizontalPanel();
        ret.setWidth("400px");
        ret.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        ret.add(horizontalPanel);

        return ret;
    }

    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONKEYDOWN:
                switch (event.getNativeEvent().getKeyCode()) {
                    case KeyCodes.KEY_ESCAPE:
                        whenCancel();
                        break;
                    case KeyCodes.KEY_ENTER:
                        whenOk();
                        break;
                }
        }
    }

    private Widget createWidget() {
        final VerticalPanel ret = new VerticalPanel();

        ret.add(createContent());
        ret.add(createButtons());

        return ret;
    }

    protected abstract Widget createContent();
}

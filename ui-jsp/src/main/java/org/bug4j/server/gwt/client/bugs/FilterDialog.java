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

package org.bug4j.server.gwt.client.bugs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import org.bug4j.server.gwt.client.data.Filter;

public class FilterDialog extends DialogBox {

    private TextBox _hitDays;
    private final Filter _filter;
    private TextBox _title;
    private CheckBox _multipleReports;

    public FilterDialog(Filter filter) {
        _filter = new Filter(filter);
        setText("Filter");

        final Widget widget = createWidget();
        setWidget(widget);
        center();
    }

    private Widget createWidget() {
        final VerticalPanel ret = new VerticalPanel();

        ret.add(createContent());
        ret.add(createOkCancel());

        return ret;
    }

    private Widget createContent() {
        final VerticalPanel ret = new VerticalPanel();
        final Grid grid = new Grid(2, 3);

        { // Hit days
            _hitDays = new TextBox();
            if (_filter.hasHitWithinDays()) {
                _hitDays.setText(Integer.toString(_filter.getHitWithinDays()));
            }
            grid.setWidget(0, 0, new Label("Hit Within:"));
            grid.setWidget(0, 1, _hitDays);
            grid.setWidget(0, 2, new Label("days"));
        }

        {
            _title = new TextBox();
            if (_filter.hasTitle()) {
                _title.setText(_filter.getTitle());
            }
            grid.setWidget(1, 0, new Label("Title:"));
            grid.setWidget(1, 1, _title);
        }

        ret.add(grid);

        {
            _multipleReports = new CheckBox("Reported by multiple users");
            _multipleReports.setValue(_filter.isReportedByMultiple());
            ret.add(_multipleReports);
        }

        return ret;
    }

    private Widget createOkCancel() {
        final HorizontalPanel hp = new HorizontalPanel();

        final Button clear = new Button("Clear");
        clear.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenClear();
            }
        });
        clear.setWidth("75px");

        final Button ok = new Button("OK");
        ok.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenOk();
            }
        });
        ok.setWidth("75px");

        final Button cancel = new Button("Cancel");
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenCancel();
            }
        });
        cancel.setWidth("75px");

        hp.setSpacing(5);
        hp.add(clear);
        hp.add(ok);
        hp.add(cancel);

        final HorizontalPanel ret = new HorizontalPanel();
        ret.setWidth("300px");
        ret.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        ret.add(hp);

        return ret;
    }

    private void whenClear() {
        _filter.clear();
        hide(false);
    }

    private void whenCancel() {
        hide(true);
    }

    private void whenOk() {
        flushValues();
        hide(false);
    }

    private void flushValues() {
        final String hitDaysText = _hitDays.getText();
        Integer hitDays = null;
        try {
            hitDays = Integer.parseInt(hitDaysText);
        } catch (NumberFormatException e) {
            //
        }
        _filter.setHitWithinDays(hitDays);

        final String titleText = _title.getText();
        _filter.setTitle(titleText.isEmpty() ? null : titleText);

        final boolean multipleReportsValue = _multipleReports.getValue();
        _filter.setReportedByMultiple(multipleReportsValue);
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

    public Filter getFilter() {
        return _filter;
    }
}

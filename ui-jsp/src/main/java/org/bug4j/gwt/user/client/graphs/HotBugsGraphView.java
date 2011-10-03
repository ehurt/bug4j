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

package org.bug4j.gwt.user.client.graphs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.user.client.Bug4jService;
import org.bug4j.gwt.user.client.BugModel;
import org.bug4j.gwt.user.client.bugs.BugDetailView;
import org.bug4j.gwt.user.client.data.Bug;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.gwt.visualization.client.AbstractDataTable.ColumnType;

public class HotBugsGraphView extends GraphView {

    private static final int DAYS_BACK = 7;
    private static final int MAX_BUGS = 5;

    private static final long DAY = 1000L * 60 * 60 * 24;
    private LineChart _lineChart;
    private final List<Bug> _bugs = new ArrayList<Bug>();

    public HotBugsGraphView(BugModel bugModel) {
        super(bugModel, "Top bugs");
    }

    @Override
    protected void addOrReplaceGraph() {
        try {
            final String application = _bugModel.getApplication();
            if (application != null) {
                Bug4jService.App.getInstance().getTopHits(application, DAYS_BACK, MAX_BUGS, new AdvancedAsyncCallback<Map<Bug, int[]>>() {
                    @Override
                    public void onSuccess(Map<Bug, int[]> topHits) {
                        final Widget widget = createGraph(DAYS_BACK, topHits);
                        setGraphWidget(widget);
                    }
                });
            }
        } catch (Exception e) {
            GWT.log(e.getMessage(), e);
        }
    }

    private Widget createGraph(int daysBack, Map<Bug, int[]> topHits) {
        final Widget ret;
        if (!topHits.isEmpty()) {
            final Options options = LineChart.createOptions();
            options.setWidth(800);
            options.setHeight(400);
            options.setLegend(LegendPosition.NONE);
            final AbstractDataTable data = createData(daysBack, topHits);
            _lineChart = new LineChart(data, options);
            _lineChart.setSize("100%", "100%");
            _lineChart.addSelectHandler(new SelectHandler() {
                @Override
                public void onSelect(SelectEvent event) {
                    final JsArray<Selection> selections = _lineChart.getSelections();
                    if (selections.length() > 0) {
                        final Selection selection = selections.get(0);
                        final int column = selection.getColumn();
                        final Bug bug = _bugs.get(column - 1);
                        whenBugSelected(bug);
                    }
                }
            });
            final FlowPanel flowPanel = new FlowPanel();
            flowPanel.add(new HTML("<H1>Top " + MAX_BUGS + " bugs in the last " + DAYS_BACK + " days.</H1>"));
            flowPanel.add(_lineChart);
            ret = flowPanel;
        } else {
            final Label label = new Label("No hits within the last " + DAYS_BACK + " days");
            final Style style = label.getElement().getStyle();
            style.setFontSize(1.5, Style.Unit.EM);
            style.setPadding(1, Style.Unit.EM);
            ret = label;
        }
        return ret;
    }

    private void whenBugSelected(Bug bug) {
        final PopupPanel popupPanel = new PopupPanel(true, true);
        final int clientWidth = (int) (Window.getClientWidth() * .8);
        final int clientHeight = (int) (Window.getClientHeight() * .8);
        popupPanel.setSize(clientWidth + "px", clientHeight + "px");
        final BugDetailView bugDetailView = new BugDetailView(_bugModel);
        final Widget widget = bugDetailView.createWidget();
        popupPanel.setWidget(widget);
        popupPanel.center();
        bugDetailView.displayBug(bug);
    }

    private AbstractDataTable createData(int daysBack, Map<Bug, int[]> topHits) {
        _bugs.clear();

        DataTable data = DataTable.create();
        data.addColumn(ColumnType.STRING, "Date");
        data.addRows(daysBack + 1);

        int col = 1;
        for (Map.Entry<Bug, int[]> bugEntry : topHits.entrySet()) {
            final Bug bug = bugEntry.getKey();
            final int[] values = bugEntry.getValue();
            final String label = bug.getId() + "-" + bug.getTitle();
            data.addColumn(ColumnType.NUMBER, label);
            for (int i = 0; i < values.length; i++) {
                int value = values[i];
                data.setValue(i, col, value);
            }
            _bugs.add(bug);
            col++;
        }
        final long now = System.currentTimeMillis();
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("EEE");
        for (int i = 0; i < daysBack + 1; i++) {
            final long t = now - (daysBack - i) * DAY;
            final Date date = new Date(t);
            final String s = dateTimeFormat.format(date);
            data.setValue(i, 0, s);
        }
        return data;
    }
}

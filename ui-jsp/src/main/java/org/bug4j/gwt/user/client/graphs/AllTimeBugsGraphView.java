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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.user.client.Bug4jService;
import org.bug4j.gwt.user.client.BugModel;
import org.bug4j.gwt.user.client.bugs.DisplaysBugs;
import org.bug4j.gwt.user.client.data.BugCountByDate;

import java.util.Date;
import java.util.List;

import static com.google.gwt.visualization.client.AbstractDataTable.ColumnType;

public class AllTimeBugsGraphView extends GraphView implements DisplaysBugs {

    public AllTimeBugsGraphView(BugModel bugModel) {
        super(bugModel, "Number of hits per day");
    }

    protected void addOrReplaceGraph() {
        try {
            final String application = _bugModel.getApplication();
            if (application != null) {
                Bug4jService.App.getInstance().getBugCountByDate(application, new AdvancedAsyncCallback<List<BugCountByDate>>() {
                    @Override
                    public void onSuccess(List<BugCountByDate> bugCountByDates) {
                        final Widget widget = createGraph(bugCountByDates);
                        setGraphWidget(widget);
                    }
                });
            }
        } catch (Exception e) {
            GWT.log(e.getMessage(), e);
        }
    }

    private Widget createGraph(List<BugCountByDate> bugCountByDates) {
        final Options options = LineChart.createOptions();
        options.setWidth(800);
        options.setHeight(400);
        options.setLegend(LegendPosition.NONE);
        final AbstractDataTable data = createData(bugCountByDates);
        final LineChart lineChart = new LineChart(data, options);
        lineChart.setSize("100%", "100%");
        final FlowPanel panel = new FlowPanel();
        panel.add(new HTML("<H1>Number of hits per day.</H1>"));
        panel.add(lineChart);
        return panel;
    }

    private AbstractDataTable createData(List<BugCountByDate> bugCountByDates) {

        DataTable data = DataTable.create();
        data.addColumn(ColumnType.STRING, "Date");
        data.addColumn(ColumnType.NUMBER, "Hits");
        data.addRows(bugCountByDates.size() + 1);

        data.setValue(0, 0, "");
        data.setValue(0, 1, 0);
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
        for (int i = 0; i < bugCountByDates.size(); i++) {
            final BugCountByDate bugCountByDate = bugCountByDates.get(i);
            final String formatedDate = dateTimeFormat.format(new Date(bugCountByDate.getDate()));
            data.setValue(i + 1, 0, formatedDate);
            data.setValue(i + 1, 1, bugCountByDate.getCount());
        }

        return data;
    }
}

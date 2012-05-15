%{--
  - Copyright 2012 Cedric Dandoy
  -
  -    Licensed under the Apache License, Version 2.0 (the "License");
  -    you may not use this file except in compliance with the License.
  -    You may obtain a copy of the License at
  -
  -        http://www.apache.org/licenses/LICENSE-2.0
  -
  -    Unless required by applicable law or agreed to in writing, software
  -    distributed under the License is distributed on an "AS IS" BASIS,
  -    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  -    See the License for the specific language governing permissions and
  -    limitations under the License.
  --}%
<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name='layout' content='main'/>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript">
        google.load('visualization', '1', {packages:['imagesparkline']});
    </script>
    <script type="text/javascript">
        function drawVisualization() {
        <g:if test="${appStats}">
            // Create and populate the data table.
            var data = google.visualization.arrayToDataTable([
                ['Bugs'],
                ${appStats.hits.collect{'['+it+']'}.join(',') }
            ]);

            // Create and draw the visualization.
            var imageSparkLine = new google.visualization.ImageSparkLine(document.getElementById('app-div-hit-graph'));
            imageSparkLine.draw(data, { showAxisLines:false, showValueLabels:false });
        </g:if>
        }

        google.setOnLoadCallback(drawVisualization);
    </script>
    <style type="text/css">
    .app-section {
        margin: 10px 0 30px 10px;
    }

    #app-title {
        border-bottom: 1px solid #3e3e3e;
    }

    #app-title-span {
        font-size: large;
    }

    .app-title-browse-link {
        font-size: small;
        margin-left: 5px;
        text-decoration: underline;
    }

    #app-daysback {
        margin-top: 5px;
    }

    .app-section-content {
        margin-left: 30px;
    }

    .app-div {
        width: 500px;
        height: 50px;
    }

    .app-hits {
        margin-top: 10px;
    }

    .app-stats {
        margin-top: 5px;
    }

    .app-stats-table {
        width: 514px;
    }

    .google-visualization-sparkline-default {
        border: none;
    }

    .google-visualization-sparkline-selected {
        background-color: #ffffff;
    }

    .google-visualization-sparkline-over {
        background-color: #ffffff;
    }

    .google-visualization-sparkline-image {
        display: block;
    }

    #app-sel-div {
        width: 10em;
        float: left;
        border-right: 1px solid #ffe4c4;
        height: 500px;
    }

    .app-sel {
        margin: 5px;
        text-decoration: none;
    }

    .app-sel-selected {
        background: #E1F2B6;
        font-weight: bold;
    }

    .app-sel-link {
        text-decoration: none;
    }

    #stats {
        margin-left: 10em;
        padding-left: 3px;
    }

    .stat-hot {
        color: red !important;
        font-weight: bold;
    }
    </style>
</head>

<body>
<div id="app-sel-div">
    <ul>
        <g:each in="${apps}" var="app">
            <li class="app-sel ${appStats?.app?.id == app.id ? 'app-sel-selected' : ''}">
                <g:link params="[appCode: app.code, daysBack: daysBack]" class="app-sel-link">
                    ${app.label}
                </g:link>
            </li>
        </g:each>
    </ul>
</div>

<g:if test="${appStats}">
    <div id="stats">

        <div class="app-section">
            <div id="app-title">
                <span id="app-title-span">${appStats.app.label}</span>
                <g:link controller="bug" params="[appCode: appStats.app.code]" class="app-title-browse-link">(browse)</g:link>
            </div>

            <div id="app-daysback">
                Statistics over the last:
                <g:each in="${[7, 14, 30, 360]}" var="it" status="lineno">
                    <g:if test="${it != daysBack}">
                        ${lineno ? ',' : ''}<g:link params="[appCode: appStats.app.code, daysBack: it]">${it}</g:link>
                    </g:if>
                    <g:else>
                        ${lineno ? ',' : ''}<span style="font-weight: bold;">${it}</span>
                    </g:else>
                </g:each>
                days
            </div>

            <div class="app-section-content">

                <div class="app-hits">
                    <div>Hits</div>

                    <div id="app-div-hit-graph" class="app-div"></div>
                </div>

                <div class="app-stats">
                    <table class="app-stats-table">
                        <thead>
                        <tr>
                            <th></th>
                            <th>Today</th>
                            <th>Yesterday</th>
                            <th>${daysBack} days average</th>
                            <th>${daysBack} days total</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>New Bugs:</td>
                            <td>
                                <g:link controller="bug" params="[appCode: appStats.app.code, fromDay: 0]" class="${appStats.bugCount.todayHot ? 'stat-hot' : ''}">${appStats.bugCount.today}</g:link>
                            </td>
                            <td>
                                <g:link controller="bug" params="[appCode: appStats.app.code, fromDay: 1, toDay: 0]" class="${appStats.bugCount.yesterdayHot ? 'stat-hot' : ''}">${appStats.bugCount.yesterday}</g:link>
                            </td>
                            <td>${appStats.bugCount.avg}</td>
                            <td>
                                <g:link controller="bug" params="[appCode: appStats.app.code, fromDay: daysBack]">${appStats.bugCount.total}</g:link>
                            </td>
                        </tr>
                        <tr>
                            <td>New Hits:</td>
                            <td>
                                <g:link controller="bug" params="[appCode: appStats.app.code, fromDay: 0]" class="${appStats.hitCount.todayHot ? 'stat-hot' : ''}">${appStats.hitCount.today}</g:link>
                            </td>
                            <td class="${appStats.hitCount.yesterdayHot ? 'stat-hot' : ''}">
                                <g:link controller="bug" params="[appCode: appStats.app.code, fromDay: 1, toDay: 0]" class="${appStats.hitCount.yesterdayHot ? 'stat-hot' : ''}">${appStats.hitCount.yesterday}</g:link>
                            </td>
                            <td>${appStats.hitCount.avg}</td>
                            <td>
                                <g:link controller="bug" params="[appCode: appStats.app.code, fromDay: daysBack]">${appStats.hitCount.total}</g:link>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</g:if>
</body>
</html>
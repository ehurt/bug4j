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
<html>
<head>
    <meta name='layout' content='main'/>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript">
        google.load('visualization', '1', {packages:['imagesparkline']});
    </script>
    <script type="text/javascript">
        function drawVisualization() {
        <g:each in="${appStats}" var="appStat">
        <g:if test="${appStat.stats}">
            // Create and populate the data table.
            var data = google.visualization.arrayToDataTable([
                ['Bugs'],
                <%
                    appStat.stats.each{out.println("[${it}],")}
                %>
            ]);

            // Create and draw the visualization.
            var imageSparkLine = new google.visualization.ImageSparkLine(document.getElementById('app-div-${appStat.app.id}'));
            imageSparkLine.draw(data, { showAxisLines:false, showValueLabels:false });
        </g:if>
        </g:each>
        }

        google.setOnLoadCallback(drawVisualization);
    </script>
    <style type="text/css">
    .app-div {
        width: 500px;
        height: 100px;
        margin-left: 15em;
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
    </style>
</head>

<body>
<div>
    days:${daysBack}
</div>

<g:each in="${appStats}" var="appStat">
    <div style="float: left;width: 15em;">${appStat.app.label}</div>

    <div id="app-div-${appStat.app.id}" class="app-div"></div>
</g:each>
</body>
</html>
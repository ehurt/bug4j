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

<%@ page import="org.bug4j.server.util.DateUtil; org.bug4j.server.util.StringUtil; org.apache.commons.lang.StringUtils; org.bug4j.Hit; java.text.DateFormat; java.text.SimpleDateFormat; org.bug4j.Bug" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>Bugs - ${app.code} - ${app.label}</title>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript" src="../js/CalendarPopup.js"></script>
    <script type="text/javascript">

        var calFilter = new CalendarPopup("filterCal");

        function drawVisualization(data) {
            $("#bugInfo-count").text(data.count)
            $("#bugInfo-reportedByCount").text(data.reportedByCount)
            $("#bugInfo-remoteAddrCount").text(data.remoteAddrCount)
            $("#bugInfo-minDateReported").text(data.minDateReported)
            $("#bugInfo-maxDateReported").text(data.maxDateReported)
            var dataTable = new google.visualization.DataTable(data.timeLineData, 0.6);

            var annotatedtimeline = new google.visualization.AnnotatedTimeLine(document.getElementById('hit-graph'));
        <%
                def now = DateUtil.adjustToDayBoundary(new Date(), DateUtil.TimeAdjustType.END_OF_DAY)
            %>
            annotatedtimeline.draw(dataTable, {
                'displayAnnotations':false,
                'zoomStartTime':new Date(${(now-5).time}),
                'zoomEndTime':new Date(${now.time})
            });
        }

        function whenBugClicked(elm, bugId) {
            $(".bug-row-selected").removeClass("bug-row-selected");
            $(elm).addClass('bug-row-selected');
        ${remoteFunction(action:'bugInfoData', onSuccess: 'drawVisualization(data)', params: '\'id=\' + bugId + \'\'')}
        }

        function showFilterForm() {
            $("#filter-form").toggle('fast');
        }

        function clearWhenFilter() {
            $("#filterFrom").val('');
            $("#filterTo").val('');
            $("#filterIncludeSingleHost").attr('checked', false)
        }

        function whenHitClicked(elm, hitId) {
            $(".hit-row-selected").removeClass('hit-row-selected');
            $(elm).addClass('hit-row-selected');
        ${remoteFunction(action:'hit', update:'hit', params: '\'hitid=\' + hitId')}
        }
        google.load('visualization', '1', {packages:['annotatedtimeline']});

    </script>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'CalendarPopup.css')}" type="text/css">
</head>

<body>
<div>
    <div id="bugs" style="width: 50%; float: left; overflow-x: hidden;">
        <div>
            <div id="filter-div" style="float:left;" onclick="showFilterForm();">
                <span style="text-decoration: underline">Filter:</span>
                <g:if test="${filter}">
                    <span id="filter-display">${filter}</span>
                </g:if>
            </div>

            <div class="clear"></div>
        </div>

        <div id="filter-form">
            <g:form id="filter-form-form" action="filter" params="${params}" method="post">
                <table style="width: auto;">
                    <tr>
                        <td><label for="filterFrom">From</label></td>
                        <td>
                            <g:textField id="filterFrom" name="filterFrom" value="${params.from}"/>
                            <a href="#" onclick="calFilter.select(document.getElementById('filterFrom'), 'filterFromCalLink', 'MM/dd/yyyy');
                            return false;" id="filterFromCalLink">
                                <g:img dir="images" file="calendar.png"/>

                            </a>
                        </td>
                        <td><label for="filterTo">to</label></td>
                        <td>
                            <g:textField id="filterTo" name="filterTo" value="${params.to}"/>
                            <a href="#" onclick="calFilter.select(document.getElementById('filterTo'), 'filterToCalLink', 'MM/dd/yyyy');
                            return false;" id="filterToCalLink">
                                <g:img dir="images" file="calendar.png"/>

                            </a>
                        </td>
                    </tr>
                    <g:if test="${app.isMultiHost()}">
                        <tr>
                            <td></td>
                            <td colspan="3">
                                <g:checkBox name="filterIncludeSingleHost" value="${params.includeSingleHost}"/>
                                <label for="filterIncludeSingleHost">Include hits reported from a single host</label>
                            </td>
                        </tr>
                    </g:if>
                </table>

                <div style="margin: 5px 0 0 5px;">
                    %{--<input type="reset" value="Clear" onclick="clearWhenFilter();return false;"/>--}%
                    <g:submitButton name="Clear" onclick="clearWhenFilter();"/>
                    <g:submitButton name="Apply"/>
                </div>
            </g:form>
        </div>
        <g:if test="${bugs}">
            <table style="cursor: pointer;">
                <tr>
                    <g:sortableColumn property="bug_id" title="${message(code: 'bug.id.label', default: 'ID')}" defaultOrder="desc" params="${params}"/>
                    <g:sortableColumn property="bug_title" title="${message(code: 'bug.title.label', default: 'Title')}" params="${params}"/>
                    <g:sortableColumn property="hitCount" title="${message(code: 'bug.hitCount.label', default: 'Hits')}" defaultOrder="desc" params="${params}"/>
                    <g:sortableColumn property="firstHitDate" title="${message(code: 'bug.firstHitDate.label', default: 'First Hit')}" defaultOrder="desc" params="${params}"/>
                    <g:sortableColumn property="lastHitDate" title="${message(code: 'bug.lastHitDate.label', default: 'Last Hit')}" defaultOrder="desc" params="${params}"/>
                    <g:sortableColumn property="hot" title="${message(code: 'bug.heat.label', default: 'Heat')}" defaultOrder="desc" params="${params}"/>
                </tr>
                <%
                    DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
                %>
                <g:each in="${bugs}" var="bug" status="lineno">
                    <tr class="bug-row" ${lineno == 0 ? 'id="row0"' : ''} onclick="whenBugClicked(this, '${bug.bug_id}');">
                        <td>${bug.bug_id}</td>
                        <td>
                            <%
                                String bugTitle = StringUtil.chopLenghtyString((String) bug.bug_title, 60)
                            %>
                            <g:if test="${true}">
                                <g:link action="bug" params="[id: bug.bug_id, sort: 'id', order: 'desc']">
                                    ${bugTitle}
                                </g:link>
                            </g:if>
                            <g:else>
                                ${bugTitle}
                            </g:else>
                        </td>
                        <td>${bug.hitCount}</td>
                        <td>${dateFormat.format(bug.firstHitDate)}</td>
                        <td>${dateFormat.format(bug.lastHitDate)}</td>
                        <td>${(int) (bug.hot * 100)}%</td>
                    </tr>
                </g:each>
            </table>
            <g:if test="${bugs.size() < total}">
                <div class="pagination">
                    <g:link params="${params + [max: (params.max as int) + 10]}">More</g:link>
                </div>
            </g:if>
        </g:if>
        <g:else>
            <h1 style="margin: 15px;">No bugs!</h1>
        </g:else>
    </div>

    <g:if test="${bugs}">
        <div id="info-section" style="width: 49%; float: right;">
            <table>
                <tr>
                    <td style="width: 10em;">Hits:</td>
                    <td id="bugInfo-count"></td>
                </tr>
                <tr>
                    <td>Reported by:</td>
                    <td id="bugInfo-reportedByCount"></td>
                </tr>
                <tr>
                    <td>Reported from:</td>
                    <td id="bugInfo-remoteAddrCount"></td>
                </tr>
                <tr>
                    <td>First report:</td>
                    <td id="bugInfo-minDateReported"></td>
                </tr>
                <tr>
                    <td>Last report:</td>
                    <td id="bugInfo-maxDateReported"></td>
                </tr>
            </table>

            <div id="hit-graph" style="width: 95%;height: 400px;"></div>
        </div>
    </g:if>

    <div class="clear"></div>
</div>
<g:if test="${bugs}">
    <g:javascript>
        whenBugClicked($("#row0"), ${bugs[0].bug_id});
    </g:javascript>
</g:if>

<div id="filterCal" style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></div>

</body>
</html>
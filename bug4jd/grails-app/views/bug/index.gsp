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
    <script type="text/javascript" src="${resource(dir: 'js', file: 'CalendarPopup.js')}"></script>
    <script type="text/javascript">

        var calFilter = new CalendarPopup("filterCal");

        function showFilterForm() {
        ${remoteFunction(
                action: 'filterForm',
                update: 'filter-form',
                onSuccess: 'onSuccessShowFilterForm()',
                params:[
                        from:params.from?params.from:'',
                        to:params.to?params.to:'',
                        includeIgnored:params.includeIgnored?params.includeIgnored:'',
                        includeSingleHost:params.includeSingleHost?params.includeSingleHost:'',
                        appCode:app.code,
                ]
         )}
        }

        function onSuccessShowFilterForm() {
            $("#filter-form").toggle('fast');
        }

        function clearWhenFilter() {
            $("#filterFrom").val('');
            $("#filterTo").val('');
            $("#filterIncludeSingleHost").attr('checked', false)
            $("#filterIncludeIgnored").attr('checked', false)
        }

    </script>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'CalendarPopup.css')}" type="text/css">
    <style type="text/css">
    table {
        border: none;
    }
    </style>
</head>

<body>
<div id="bugs">
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
    </div>

    <g:if test="${bugs}">
        <div class="pagination">
            <g:paginate total="${total}" params="${params}"/>
        </div>
        <table class="table-hover">
            <tr>
                <g:sortableColumn property="id" title="${message(code: 'bug.id.label', default: 'ID')}" defaultOrder="desc" params="${params.subMap(paramNames)}"/>
                <g:sortableColumn property="title" title="${message(code: 'bug.title.label', default: 'Title')}" params="${params.subMap(paramNames)}"/>
                <g:sortableColumn property="hitCount" title="${message(code: 'bug.hitCount.label', default: 'Hits')}" defaultOrder="desc" params="${params.subMap(paramNames)}"/>
                <g:sortableColumn property="firstHitDate" title="${message(code: 'bug.firstHitDate.label', default: 'First Hit')}" defaultOrder="desc" params="${params.subMap(paramNames)}"/>
                <g:sortableColumn property="lastHitDate" title="${message(code: 'bug.lastHitDate.label', default: 'Last Hit')}" defaultOrder="desc" params="${params.subMap(paramNames)}"/>
                <g:sortableColumn property="stackCount" title="${message(code: 'bug.stackCount.label', default: 'Stack')}" defaultOrder="desc" params="${params.subMap(paramNames)}"/>
                <g:sortableColumn property="hot" title="${message(code: 'bug.heat.label', default: 'Heat')}" defaultOrder="desc" params="${params.subMap(paramNames)}"/>
            </tr>
            <%
                DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
            %>
            <g:each in="${bugs}" var="bug" status="lineno">
                <tr class="bug-row" ${lineno == 0 ? 'id="row0"' : ''}>
                    <td>${bug.id}</td>
                    <td>
                        <%
                            String bugTitle = StringUtil.chopLenghtyString((String) bug.title, 60)
                        %>
                        <g:link controller="detail" action="index" params="${params.subMap(paramNames) + [offset: lineno + (params.offset as int)]}">
                            ${bugTitle}
                        </g:link>
                    </td>
                    <td>${bug.hitCount}</td>
                    <td>${dateFormat.format(bug.firstHitDate)}</td>
                    <td>${dateFormat.format(bug.lastHitDate)}</td>
                    <td>${bug.stackCount ? 'Y' : ''}</td>
                    <td>${(int) (bug.hot * 100)}%</td>
                </tr>
            </g:each>
        </table>

        <div class="pagination">
            <g:paginate total="${total}" params="${params}"/>
        </div>
    </g:if>
    <g:else>
        <h1 style="margin: 15px;">No bugs!</h1>
    </g:else>
</div>

<div id="filterCal" style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></div>

</body>
</html>
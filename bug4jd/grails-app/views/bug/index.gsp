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

<%@ page import="org.bug4j.server.util.StringUtil; org.apache.commons.lang.StringUtils; org.bug4j.Hit; java.text.DateFormat; java.text.SimpleDateFormat; org.bug4j.Bug" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>Bugs - ${app.code} - ${app.label}</title>
    <script type="text/javascript">
        function whenBugClicked(elm, bugId) {
            if (${showHits}) {
                $(".bug-row-selected").removeClass("bug-row-selected");
                $(elm).addClass('bug-row-selected');
            ${remoteFunction(action:'hits', update:'hits', params: '\'id=\' + bugId + \'&sort=id&order=desc\'')}
            }
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

    </script>
    <style type="text/css">
    .bug-row:hover {
    ${      showHits?'cursor: pointer;':''      }
    }

    </style>
</head>

<body>
<div>
    <div id="bugs" class="${showHits ? 'bugs-with-hits' : 'bugs-without-hits'}">
        <div>
            <div id="filter-div" style="float:left;" onclick="showFilterForm();">
                <span style="text-decoration: underline">Filter:</span>
                <g:if test="${filter}">
                    <span id="filter-display">${filter}</span>
                </g:if>
            </div>
            <g:if test="${!showHits}">
                <div style="float: right;width: 16px;margin-top: 3px;">
                    <g:link params="${params + [sh: 'y']}">
                        <g:img dir="images/skin" file="arrow_left.png" title="Show Hits"/>
                    </g:link>
                </div>
            </g:if>
            <div class="clear"></div>
        </div>

        <div id="filter-form">
            <g:form id="filter-form-form" action="filter" params="${params}" method="post">
                <table style="width: auto;">
                    <tr>
                        <td><label for="filterFrom">From</label></td>
                        <td><g:textField name="filterFrom" value="${params.from}"/></td>
                        <td><label for="filterTo">to</label></td>
                        <td><g:textField id="filterTo" name="filterTo" value="${params.to}"/></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td colspan="3">
                            <g:checkBox name="filterIncludeSingleHost" value="${params.includeSingleHost}"/>
                            <label for="filterIncludeSingleHost">Include hits reported from a single host</label>
                        </td>
                    </tr>
                </table>

                <div style="margin: 5px 0 0 5px;">
                    %{--<input type="reset" value="Clear" onclick="clearWhenFilter();return false;"/>--}%
                    <g:submitButton name="Clear" onclick="clearWhenFilter();"/>
                    <g:submitButton name="Apply"/>
                </div>
            </g:form>
        </div>
        <table>
            <tr>
                <g:sortableColumn property="bug_id" title="${message(code: 'bug.id.label', default: 'ID')}" defaultOrder="desc" params="${params}"/>
                <g:sortableColumn property="bug_title" title="${message(code: 'bug.title.label', default: 'Title')}" params="${params}"/>
                <g:sortableColumn property="hitCount" title="${message(code: 'bug.hitCount.label', default: 'Hits')}" defaultOrder="desc" params="${params}"/>
                <g:sortableColumn property="firstHitDate" title="${message(code: 'bug.firstHitDate.label', default: 'First Hit')}" defaultOrder="desc" params="${params}"/>
                <g:sortableColumn property="lastHitDate" title="${message(code: 'bug.lastHitDate.label', default: 'Last Hit')}" defaultOrder="desc" params="${params}"/>
            </tr>
            <%
                DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
            %>
            <g:each in="${bugs}" var="bug" status="lineno">
                <tr class="bug-row ${lineno == 0 && showHits ? ' bug-row-selected' : ''}" onclick="whenBugClicked(this, '${bug.bug_id}');">
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
                </tr>
            </g:each>
        </table>
        <g:if test="${bugs.size() < total}">
            <div class="pagination">
                <g:link params="${params + [max: (params.max as int) + 10]}">More</g:link>
            </div>
        </g:if>
    </div>

    <g:if test="${showHits}">
        <div id="hits-section">
            <div id="hits-header" style="margin:5px;height: 1.5em;">
                <g:link params="${params - [sh: 'y']}">
                    <g:img dir="images/skin" file="arrow_right.png" title="Hide Hits"/>
                </g:link>
            </div>

            <%
                def hits = null
                if (bugs) {
                    def firstBug = bugs[0]
                    long bugId = firstBug.bug_id as long
                    def bug = Bug.get(bugId)
                    hits = Hit.findAllByBug(bug, [sort: 'id', order: 'desc'])
                }
            %>
            <div id="hits">
                <g:render template="hits" model="[hits: hits]"/>
            </div>
        </div>
    </g:if>

    <div class="clear"></div>
</div>
</body>
</html>
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

<%@ page import="java.text.DateFormat; java.text.SimpleDateFormat; org.bug4j.Bug" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>Bugs - ${application.code} - ${application.label}</title>
    <script type="text/javascript">
        function whenBugClicked(elm, bugId) {
            $(".bug-row-selected").removeClass("bug-row-selected");
            $(elm).addClass('bug-row-selected');
        ${remoteFunction(action:'hits', update:'hits', params: '\'bugid=\' + bugId')}
        }

        function whenHitClicked(elm, hitId) {
            $(".hit-row-selected").removeClass('hit-row-selected');
            $(elm).addClass('hit-row-selected');
        ${remoteFunction(action:'hit', update:'hit', params: '\'hitid=\' + hitId')}
        }
    </script>
    <style type="text/css">

    #bugs {
        width: 50%;
        float: left;
    }

    #hits {
        width: 49%;
        float: right;
    }

    #hit-table {
        height: 300px;
        overflow-x: hidden;
        overflow-y: scroll;
    }

    #stack {
        border-top: 1px solid #E1F2B6;
        max-height: 500px;
        overflow-x: scroll;
        overflow-y: scroll;
        white-space: nowrap;
    }

    .clear {
        clear: both;
    }

    tr:hover {
        background: inherit;
    }

    .bug-row:hover {
        background: #E1F2B6;
        cursor: pointer;
    }

    .bug-row-selected {
        background: #eeffc3;
        font-weight: bold;
    }

    .hit-row:hover {
        background: #E1F2B6;
        cursor: pointer;
    }

    .hit-row-selected {
        background: #eeffc3;
        font-weight: bold;
    }

    .hit {
        border: 1px solid #E1F2B6;
        padding: 3px;
    }

    th {
        white-space: nowrap;
    }

    .hit-message {
        overflow-x: hidden;
    }

    #hit-row, td {
        white-space: nowrap;
    }
    </style>
</head>

<body>
<div>
    <div id="bugs">
        <g:if test="${bugs.size() < total}">
            <div class="pagination">
                <g:paginate total="${total}"/>
            </div>
        </g:if>
        <table>
            <tr>
                <g:sortableColumn property="bug_id" title="${message(code: 'bug.id.label', default: 'ID')}"/>
                <g:sortableColumn property="bug_title" title="${message(code: 'bug.title.label', default: 'Title')}"/>
                <g:sortableColumn property="hitCount" title="${message(code: 'bug.hitCount.label', default: 'Hits')}"/>
                <g:sortableColumn property="firstHitDate" title="${message(code: 'bug.firstHitDate.label', default: 'First Hit')}"/>
                <g:sortableColumn property="lastHitDate" title="${message(code: 'bug.lastHitDate.label', default: 'Last Hit')}"/>
            </tr>
            <%
                DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
            %>
            <g:each in="${bugs}" var="bug" status="lineno">
                <tr class="bug-row ${lineno == 0 ? ' bug-row-selected' : ''}" onclick="whenBugClicked(this, '${bug.bug_id}');">
                    <td>${bug.bug_id}</td>
                    <td>${bug.bug_title}</td>
                    <td>${bug.hitCount}</td>
                    <td>${dateFormat.format(bug.firstHitDate)}</td>
                    <td>${dateFormat.format(bug.lastHitDate)}</td>
                </tr>
            </g:each>
        </table>
    </div>

    <div id="hits">
        <%
            def hits = []
            if (bugs) {
                long bugId = bugs[0].bug_id as long
                hits = Bug.get(bugId).hits
            }
        %>
        <g:render template="hits" model="[hits: hits]"/>
    </div>

    <div class="clear"></div>
</div>
</body>
</html>
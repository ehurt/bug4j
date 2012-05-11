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


<%@ page import="org.bug4j.Bug" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>Bugs - ${application.code} - ${application.label}</title>
    <script type="text/javascript">
        function whenBugClicked(elm, bugId) {
            $(".bug-row-selected").removeClass("bug-row-selected");
            $(elm).addClass('bug-row-selected');
            $('#hit').text('');
        ${remoteFunction(action:'hits', update:'hits', params: '\'bugid=\' + bugId')}
        }

        function whenHitClicked(elm, hitId) {
            $(".hit-row-selected").removeClass('hit-row-selected');
            $(elm).addClass('hit-row-selected');
        ${remoteFunction(action:'hit', update:'hit', params: '\'hitid=\' + hitId')}
        }
    </script>
    <style type="text/css">
    body {
        height: 100%;
        width: 100%;
        margin: 0;
        padding: 0;
    }

    tr:hover {
        background: inherit;
    }

    .bug-row:hover {
        background: #E1F2B6;
        cursor: pointer;
    }

    .bug-row-selected {
        background: #E1F2B6;
        font-weight: bold;
    }

    .hit-row:hover {
        background: #E1F2B6;
        cursor: pointer;
    }

    .hit-row-selected {
        background: #E1F2B6;
        font-weight: bold;
    }

    .hit {
        border: 1px solid #E1F2B6;
        padding: 3px;
    }
    </style>
</head>

<body>
<table>
    <tr>
        <td style="width: 50%;">
            <div>
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
                    </tr>
                    <g:each in="${bugs}" var="bug" status="lineno">
                        <tr class="bug-row ${lineno == 0 ? ' bug-row-selected' : ''}" onclick="whenBugClicked(this, '${bug.bug_id}');">
                            <td>${bug.bug_id}</td>
                            <td>${bug.bug_title}</td>
                            <td>${bug.hitCount}</td>
                        </tr>
                    </g:each>
                </table>
            </div>
        </td>
        <td>
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
        </td>
    </tr>
</table>
</body>
</html>
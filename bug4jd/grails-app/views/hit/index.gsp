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
<%@ page import="java.text.DateFormat; java.text.SimpleDateFormat" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>${bug.id} - ${bug.title}</title>
    <style type="text/css">
    #bug-title {
        padding: 10px;
        font-size: large;
    ${                 bug.ignore?'text-decoration: line-through;':''                 }
    }
    </style>
</head>

<body>
%{--
<div class="nav" role="navigation">
    <ul>
        <li>
            <g:link class="action-back" controller="detail" action="index" params="[id: bug.id, a:bug.app.code]">
                <g:message code="detail.action.back.label" default="Back"/>
            </g:link>
        </li>
    </ul>
</div>
--}%
<div class="content" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <div id="bug-title">Hits on bug ${bug.id} - ${bug.title}</div>

    <div class="pagination">
        <g:paginate total="${total}" params="[id: bug.id]"/>
    </div>

    <table>
        <thead>
        <tr>
            <g:sortableColumn property="id" title="ID" params="${params}"/>
            <g:sortableColumn property="reportedBy" title="User" params="${params}"/>
            <g:sortableColumn property="remoteAddr" title="Host" params="${params}"/>
            <g:sortableColumn property="clientSession.appVersion" title="Version" params="${params}"/>
            <g:sortableColumn property="clientSession.dateBuilt" title="Date Built" params="${params}"/>
            <g:sortableColumn property="clientSession.buildNumber" title="Build Number" params="${params}"/>
            <g:sortableColumn property="clientSession.devBuild" title="Dev.Build" params="${params}"/>
            <g:sortableColumn property="stack.strain.id" title="Stack" params="${params}"/>
        </tr>
        </thead>
        <g:render template="hitsRows" model="[hits: hits]"/>
    </table>

    <div class="pagination">
        <g:paginate total="${total}" params="[id: bug.id]"/>
    </div>
</div>
</body>
</html>

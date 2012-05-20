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
</head>

<body>

<%
    DateFormat sessionDateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
%>
<table>
    <tr><td style="width: 5em;">Session:</td><td>${clientSession.id}</td></tr>
    <tr><td>Host:</td><td>${clientSession.hostName}</td></tr>
    <tr><td>Version:</td><td>${clientSession.appVersion}</td></tr>
    <tr><td>Built:</td><td>${clientSession.dateBuilt ? sessionDateFormat.format(clientSession.dateBuilt) : ''}</td></tr>
    <tr><td>Build#:</td><td>${clientSession.buildNumber}</td></tr>
    <tr><td>Dev.Build:</td><td>${clientSession.devBuild ? 'Yes' : 'No'}</td></tr>
    <tr><td>First Hit:</td><td>${clientSession.firstHit ? sessionDateFormat.format(clientSession.firstHit) : ''}</td></tr>
</table>

<div id="hits">
    <table>
        <thead>
        <tr>
            <th class="sortable">Bug ID</th>
            <th class="sortable sorted desc">${message(code: 'hit.dateReported.label', default: 'Date Reported')}</th>
            <th class="sortable">${message(code: 'hit.message.label', default: 'Message')}</th>
        </tr>
        </thead>
        <g:each in="${hits}" var="hit" status="lineno">
            <tr class="hit-row ${lineno % 2 ? 'odd' : 'even'}">
                <td>
                    <g:link action="bug" id="${hit.bug.id}">${hit.bug.id}</g:link>
                </td>
                <td style="white-space: nowrap;">${sessionDateFormat.format(hit.dateReported)}</td>
                <td class="hit-message">${hit.message}</td>
            </tr>
        </g:each>
    </table>
</div>
</body>
</html>
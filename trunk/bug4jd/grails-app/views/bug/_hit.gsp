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
<%@ page import="java.text.SimpleDateFormat; java.text.DateFormat; org.bug4j.BugService" %>

<% if (!hitTab) hitTab = 'stack' %>
<div class="tab">
    <g:remoteLink id="${hit?.id}" update="hit" action="stack" class="tab-item${hitTab == 'stack' ? ' active-tab-item' : ''}">Stack</g:remoteLink>
    <g:remoteLink id="${hit?.id}" update="hit" action="session" class="tab-item${hitTab == 'session' ? ' active-tab-item' : ''}">Session</g:remoteLink>
</div>

<div style="border: solid #48802C;border-top-width: 0;border-right-width: 1px;border-bottom-width: 1px;border-left-width: 1px;">
    <g:if test="${hitTab == 'stack'}">
        <g:if test="${hit?.stack?.stackText}">
            <div id="stack">
                <%
                    String stackText = hit.stack?.stackText?.readStackString()
                    String stackHtml = BugService.stackToHtml(stackText, hit.bug.app.appPackages*.packageName)
                %>
                ${stackHtml}
            </div>
        </g:if>
    </g:if>
    <g:if test="${hitTab == 'session'}">
        <% def clientSession = hit.clientSession %>
        <g:if test="${clientSession}">
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
        </g:if>
        <g:else>
            <P>Session information not available</P>
        </g:else>
    </g:if>
</div>

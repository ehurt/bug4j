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

<%@ page import="java.text.DateFormat; java.text.SimpleDateFormat" %>
<div id="hit-table">
    <table>
        <thead>
        <tr>
            <util:remoteSortableColumn update="hits" action="hits" params="${params}" property="id" defaultOrder="desc" title="${message(code: 'hit.id.label', default: 'ID')}" style="width: 5em;"/>
            <util:remoteSortableColumn update="hits" action="hits" params="${params}" property="dateReported" title="${message(code: 'hit.dateReported.label', default: 'Date Reported')}" style="width: 20em;"/>
            <util:remoteSortableColumn update="hits" action="hits" params="${params}" property="reportedBy" title="${message(code: 'hit.reportedBy.label', default: 'Reported By')}" style="width: 15em;"/>
            <util:remoteSortableColumn update="hits" action="hits" params="${params}" property="message" title="${message(code: 'hit.message.label', default: 'Message')}" style="width: 100%;"/>
        </tr>
        </thead>
        <tbody id="hit-tbody">
        <%
            DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        %>
        <g:each in="${hits}" var="hit" status="lineno">
            <tr class="hit-row ${lineno ? "" : "hit-row-selected"}" onclick="whenHitClicked(this, '${hit.id}')">
                <td>${hit.id}</td>
                <td style="white-space: nowrap;">${dateFormat.format(hit.dateReported)}</td>
                <td>${hit.reportedBy}</td>
                <td class="hit-message">${hit.message}</td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>

<div id="hit">
    <g:render template="hit" model="[hit: hits ? hits.iterator().next() : []]"/>
</div>

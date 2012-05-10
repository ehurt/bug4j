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


<table>
    <tr style="height: 50%;"><td>
        <table>
            <tr>
                <g:sortableColumn property="id" title="${message(code: 'hit.id.label', default: 'ID')}"/>
                <g:sortableColumn property="dateReported" title="${message(code: 'hit.dateReported.label', default: 'Date Reported')}"/>
                <g:sortableColumn property="reportedBy" title="${message(code: 'hit.reportedBy.label', default: 'Reported By')}"/>
                <g:sortableColumn property="message" title="${message(code: 'hit.message.label', default: 'Message')}"/>
            </tr>
            <g:each in="${hits}" var="hit" status="lineno">
                <tr class="hit-row ${lineno ? "" : "hit-row-selected"}" onclick="whenHitClicked(this, '${hit.id}')">
                    <td>${hit.id}</td>
                    <td>${hit.dateReported}</td>
                    <td>${hit.reportedBy}</td>
                    <td>${hit.message}</td>
                </tr>
            </g:each>
        </table>
    </td></tr>
    <tr>
        <td>
            <div id="hit" class="hit">
                <g:render template="hit" model="[hit: hits ? hits.iterator().next() : []]"/>
            </div>
        </td>
    </tr>
</table>

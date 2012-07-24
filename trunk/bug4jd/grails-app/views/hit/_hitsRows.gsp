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
<%
    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
    def stackIds = (hits*.stack?.strain?.id).unique()
    Map<Long, String> stackNames = [:]

    if (params.sort == 'stack.strain.id' && params.order == 'desc') {
        stackIds = stackIds.reverse()
    }

    stackIds.eachWithIndex {Long stackId, Integer i ->
        String name
        if (i < 26) {
            name = ('A'..'Z')[i]
        } else {
            name = '='
        }
        stackNames.put(stackId, name)
    }
%>
<tbody>
<g:each in="${hits}" var="hit">
    <tr>
        <td>${hit.id}</td>
        <td>${hit.reportedBy}</td>
        <td>${hit.remoteAddr}</td>
        <td>${hit.clientSession.appVersion}</td>
        <td>${dateFormat.format(hit.clientSession.dateBuilt)}</td>
        <td>${hit.clientSession.buildNumber}</td>
        <td>${hit.clientSession.devBuild ? 'Yes' : ''}</td>
        <td>
            <g:if test="${hit.stack}">
                <g:link controller="stack" params="[id: hit.stackId]" target="_blank">
                    ${stackNames[hit.stack.strainId]}
                </g:link>
            </g:if>
        </td>
    </tr>
</g:each>
</tbody>
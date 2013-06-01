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
<%@ page import="org.bug4j.MergePattern; org.bug4j.server.util.StringUtil" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>Merge</title>
    <style type="text/css">
    .form-label {
        vertical-align: middle;
        white-space: nowrap;
    }

    .form-field {
        width: 100%;
    }

    .hilight-bug {
        background-color: #f0f8ff;
    }
    </style>
</head>

<body>
<div class="content">
    <g:if test="${flash.error}">
        <div class="errors" role="status">${flash.error}</div>
    </g:if>
    <H1>Merge</H1>

    <div style="margin-left: 15px;">
        <div>Merge bugs based on a regular expression pattern.</div>

        <g:form action="merge" autocomplete="off">
            <g:hiddenField name="id" value="${bug.id}"/>
            <table style="width: 100%;margin-top: 15px;">
                <tr>
                    <td class="form-label">
                        Merge into:
                    </td>
                    <td class="form-field">
                        <g:link controller="detail" params="[id: bug.id]">
                            ${bug.id} - ${bug.title}
                        </g:link>
                    </td>
                </tr>
                <tr>
                    <td class="form-label">
                        Pattern:
                    </td>
                    <td class="form-field">
                        <g:textField name="pat" value="${pat}" style="width: 100%;font-family: monospace;" maxlength="${MergePattern.PATTERN_SIZE}"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <g:submitButton name="test" value="Test"/>
                    </td>
                </tr>
            </table>
            <g:if test="${matchingBugs != null}">
                <g:if test="${matchingBugs}">
                    <table style="width: 100%;">
                        <tr>
                            <th style="width: 5em;">${message(code: 'bug.id.label', default: 'ID')}</th>
                            <th>${message(code: 'bug.title.label', default: 'Title')}</th>
                        </tr>
                        <g:each in="${matchingBugs}" var="matchingBug" status="lineno">
                            <%
                                String bugTitle = StringUtil.chopLenghtyString((String) matchingBug.title, 60)
                                String cls = bug == matchingBug ? " hilight-bug" : ""
                            %>
                            <tr class="bug-row ${cls}" ${lineno == 0 ? 'id="row0"' : ''}>
                                <td>${matchingBug.id}</td>
                                <td>
                                    <g:link controller="detail" params="[id: matchingBug.id]">
                                        ${bugTitle}
                                    </g:link>
                                </td>
                            </tr>
                        </g:each>
                    </table>
                    <g:if test="${matchingBugs.size() < total}">
                        <div class="pagination">
                            And ${total - matchingBugs.size()} more
                        </div>
                    </g:if>

                    <g:submitButton name="create" value="Merge"/>
                </g:if>
                <g:else>
                    <h1 style="margin: 15px;">No matches!</h1>
                </g:else>
            </g:if>
        </g:form>
    </div>

</div>
</body>
</html>
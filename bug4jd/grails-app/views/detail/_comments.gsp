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
<%@ page import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils; java.text.DateFormat; java.text.SimpleDateFormat" %>
<%
    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
%>
<table>
    <g:each in="${comments}" var="comment" status="lineno">
        <tr id="comment-${lineno}">
            <td style="padding: 5px;">
                <table style="margin: 0;">
                    <tr style="background-color: #f0f8ff">
                        <td style="padding: 0;">${comment.addedBy} - ${dateFormat.format(comment.dateAdded)}</td>
                        <td style="padding: 0;text-align: right;">
                            <g:if test="${sec.loggedInUserInfo(field: "username").equals(comment.addedBy)}">
                                <g:img dir="images/skin"
                                       file="delete.png"
                                       onclick="
                                       ${remoteFunction(controller: 'bug', action: 'removeComment', params: [id: comment.id])};
                                       \$('#comment-${lineno}').hide('fast');
                                       "/>
                            </g:if>
                        </td>
                    </tr>
                </table>

                <div>
                </div>

                <div>${comment.text}</div>
            </td>
        </tr>
    </g:each>

    <tr id="newCommentDiv" style="display: none">
        <td style="padding: 5px;">
            <g:form action="addComment">
                <div style="background-color: #E1F2B6"><sec:username/> - ${dateFormat.format(new Date())}</div>
                <g:hiddenField name="bugId" value="${bug.id}"/>
                <textarea id="newCommentTextArea" name="newComment" rows="4" cols="80" style="width: 100%;height: 4em;"></textarea>

                <div>
                    <g:submitToRemote url="[controller: 'bug', action: 'addComment']" value="Add" update="bugComments"/>
                </div>
            </g:form>
        </td>
    </tr>
</table>


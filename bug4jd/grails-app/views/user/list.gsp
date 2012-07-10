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

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>Users</title>
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
    <style type="text/css">
    .create-user {
        background: url('${resource(dir:'/images/skin', file:'user_add.png')}') no-repeat 0.7em center;
        text-indent: 25px;
    }

    .create-bulk {
        background: url('${resource(dir:'/images/skin', file:'table_add.png')}') no-repeat 0.7em center;
        text-indent: 25px;
    }
    </style>
</head>

<body>
<div class="nav" role="navigation">
    <ul>
        <li>
            <g:link class="admin-home" controller="admin">
                <g:message code="default.adminHome.label"/>
            </g:link>
        </li>
        <li>
            <g:link class="create-user" action="create">
                <g:message code="default.new.label" args="[entityName]"/>
            </g:link>
        </li>
        <li>
            <g:link class="create-bulk" action="bulk">
                <g:message code="default.bulk.label" default="Bulk Create"/>
            </g:link>
        </li>
    </ul>
</div>

<div class="content" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <h1>Users</h1>

    <table>
        <tr>
            <th>User Name</th>
        </tr>
        <g:each in="${users}" var="user">
            <tr><td><g:link action="edit" id="${user.id}">${user.username.encodeAsHTML()}</g:link></td></tr>
        </g:each>
    </table>

    <div class="pagination">
        <g:paginate total="${total}"/>
    </div>
</div>
</body>
</html>
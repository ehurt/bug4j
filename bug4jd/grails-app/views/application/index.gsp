%{--
  - Copyright 2013 Cedric Dandoy
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
    <title>Applications</title>
    <g:set var="entityName" value="${message(code: 'app.label', default: 'Application')}"/>
    <style type="text/css">
    .create-application {
        background: url('${resource(dir:'/images/skin', file:'application_add.png')}') no-repeat 0.7em center;
        text-indent: 25px;
    }

    .autocreate-application {
        background: url('${resource(dir:'/images/skin', file:'application_lightning.png')}') no-repeat 0.7em center;
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
            <g:link class="create-application" action="create">
                <g:message code="default.new.label" args="[entityName]"/>
            </g:link>
        </li>
        <li>
            <g:link class="autocreate-application" action="autocreate">
                <g:message code="application.autocreate-label" args="[appAutoCreate ? 'on' : 'off']"/>
            </g:link>
        </li>
    </ul>
</div>

<div id="list-apps" class="content scaffold-list" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <h1>Applications</h1>
    <table>
        <tr>
            <g:sortableColumn property="label" title="${message(code: 'app.label.label', default: 'Name')}" params="${params}"/>
            <g:sortableColumn property="code" title="${message(code: 'app.code.label', default: 'Code')}" params="${params}"/>
        </tr>
        <g:each in="${apps}" var="app">
            <tr>
                <td><g:link action="edit" id="${app.id}">${app.label.encodeAsHTML()}</g:link></td>
                <td>${app.code.encodeAsHTML()}</td>
            </tr>
        </g:each>
        <g:paginate total="${total}"/>
    </table>
</div>
</body>
</html>
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
    <title>Automatic application creation</title>
    <style type="text/css">
    .list-application {
        background: url('${resource(dir:'/images/skin', file:'application_cascade.png')}') no-repeat 0.7em center;
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
            <g:link class="list-application" action="index"><g:message code="default.list.label" args="['Application']"/></g:link>
        </li>
    </ul>
</div>
<g:if test="${flash.message}">
    <div class="message">
        ${flash.message}
    </div>
</g:if>

<div class="content" style="margin-left: 15px;">
    <H1><g:img dir="/images/skin" file="application_lightning.png" style="padding-right: 3px;"/>Automatic application creation</H1>
    <g:form action="updateAutoCreate" style="margin: 20px 0 0 50px;">
        <div>
            <g:checkBox id="autoCreate" name="autoCreate" value="${appAutoCreate}"/>
            <label for="autoCreate">Automatically creates the application requested by the client.</label>
        </div>

        <div style="margin-top: 15px;">
            <g:submitButton name="apply" value="Apply"/>
        </div>
    </g:form>
</div>
</body>
</html>
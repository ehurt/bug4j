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
    <title>Edit User</title>
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
    <style type="text/css">
    .list-user {
        background: url('${resource(dir:'/images/skin', file:'user.png')}') no-repeat 0.7em center;
        text-indent: 25px;
    }

    .delete-user {
        background: url('${resource(dir:'/images/skin', file:'user_delete.png')}') no-repeat 0.7em center;
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
            <g:link class="list-user" action="list">
                <g:message code="default.list.label" args="[entityName]"/>
            </g:link>
        </li>

        <li>
            <g:link class="delete-user" action="delete" id="${userInstance.id}">
                <g:message code="default.button.delete.label" args="['User']"/>
            </g:link>
        </li>
    </ul>
</div>

<g:if test="${flash.message}">
    <div class="message" role="status">${flash.message}</div>
</g:if>
<g:hasErrors bean="${userInstance}">
    <ul class="errors" role="alert">
        <g:eachError bean="${userInstance}" var="error">
            <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
        </g:eachError>
    </ul>
</g:hasErrors>

<g:form action="save" role="main">
    <fieldset class="form">
        <g:render template="form"/>
    </fieldset>
    <fieldset class="buttons">
        <g:submitButton name="create" class="save" value="${
            userInstance.id ?
            message(code: 'default.button.update.label', default: 'Update') :
            message(code: 'default.button.create.label', default: 'Create')
        }"/>
    </fieldset>
</g:form>

</body>
</html>
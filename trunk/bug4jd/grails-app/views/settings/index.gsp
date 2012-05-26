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
    <title>Settings</title>
</head>

<body>
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
<h1>Settings</h1>

<div style="width: 500px;margin-left: 100px;">
    <g:form name="edit" method="post">

        <div class="fieldcontain">
            <label for="username">
                <g:message code="user.username.label" default="Name:"/>
            </label>
            ${userInstance?.username}
        </div>

        <div class="fieldcontain ${hasErrors(bean: userInstance, field: 'password', 'error')} ">
            <label for="password">
                <g:message code="user.password.label" default="Password:"/>
            </label>
            <g:passwordField name="password" value="${displayPassword}"/>
        </div>

        <div class="fieldcontain ${hasErrors(bean: userInstance, field: 'email', 'error')} ">
            <label for="email">
                <g:message code="user.email.label" default="Email:"/>
            </label>
            <g:textField name="email" value="${userInstance.email}"/>
        </div>

        <g:actionSubmit action="index" value="Update"/>
    </g:form>
</div>
</body>
</html>
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



<%@ page import="org.bug4j.Role; org.bug4j.User" %>
<g:hiddenField name="id" value="${userInstance?.id}"/>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'username', 'error')} ">
    <label for="username">
        <g:message code="user.username.label" default="Name"/>
    </label>
    <g:textField name="username" value="${userInstance?.username}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'password', 'error')} ">
    <label for="password">
        <g:message code="user.password.label" default="Password"/>
    </label>
    <g:passwordField name="password" value="${org.bug4j.bug4jd.UserController.DUMMY_PASSWORD}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'enabled', 'error')} ">
    <label for="enabled">
        <g:message code="user.enabled.label" default="Enabled"/>
    </label>
    <g:checkBox name="enabled" value="${userInstance?.enabled}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'enabled', 'error')} ">
    <label for="admin">
        <g:message code="user.admin.label" default="Administrator"/>
    </label>
    <g:checkBox name="admin" value="${authorities.contains(Role.ADMIN)}"/>
</div>


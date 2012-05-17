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
    <title>Application</title>
    <g:set var="entityName" value="${message(code: 'app.label', default: 'Application')}"/>
    <script type="text/javascript">
        function addPkg() {
            var clone = $("#add-pkg-tr-template").clone();
            clone.attr("id", "")
                    .insertBefore("#add-pkg-tr-template")
                    .show();
            clone.find("input").focus()
        }
        function deletePkg(elm) {
            elm.parent().parent().remove();
        }
    </script>
</head>

<body>
<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(controller: 'admin')}"><g:message code="default.adminHome.label"/></a></li>
        <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <g:if test="${appInstance.id}">
            <li><g:link class="delete" action="delete" id="${appInstance.id}"><g:message code="default.button.delete.label" args="['Application']"/></g:link></li>
        </g:if>
    </ul>
</div>

<g:if test="${flash.message}">
    <div class="message" role="status">${flash.message}</div>
</g:if>
<g:hasErrors bean="${appInstance}">
    <ul class="errors" role="alert">
        <g:eachError bean="${appInstance}" var="error">
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
            appInstance.id ?
            message(code: 'default.button.update.label', default: 'Update') :
            message(code: 'default.button.create.label', default: 'Create')
        }"/>
    </fieldset>
</g:form>

</body>
</html>
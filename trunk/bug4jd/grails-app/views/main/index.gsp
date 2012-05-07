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
    <title>Main</title>
</head>

<body>
<g:each in="${applications}" var="application">
    <h1>${application.code} - ${application.label}</h1>
    <table>
        <tr>
            <g:sortableColumn property="id" title="${message(code: 'bug.id.label', default: 'ID')}"/>
            <g:sortableColumn property="title" title="${message(code: 'bug.title.label', default: 'Title')}"/>
        </tr>
        <g:each in="${application.bugs}" var="bug">
            <tr>
                <td>${bug.id}</td>
                <td>${bug.title}</td>
            </tr>
        </g:each>
    </table>
</g:each>
</body>
</html>
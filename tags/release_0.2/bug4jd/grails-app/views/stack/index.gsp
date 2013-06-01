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
<%@ page import="org.bug4j.server.BugService" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>${bug.id} - ${bug.title}</title>
    <style type="text/css">
    #bug-title {
        padding: 10px;
        font-size: large;
    ${  bug.ignore?'text-decoration: line-through;':''  }
    }

    #stack-text {
        margin: 5px;
        padding: 5px;
        border: 1px solid #eeeeee;
        height: 600px;
        overflow-x: scroll;
    }
    </style>
</head>

<body>
<div id="bug-title">${bug.id} - ${bug.title}</div>

<div id="stack-text">
    <%
        String stackText = stack.stackText.readStackString()
        String stackHtml = BugService.stackToHtml(stackText, bug.app.appPackages*.packageName)
    %>
    <pre>${stackHtml}</pre>
</div>

</body>
</html>
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
    <title>Administration</title>
    <script type="text/javascript">
        function toggleDiv(elmId) {
            var isVisible = $(elmId).is(":visible");
            $(".expandable").hide('fast');
            if (isVisible) {
                $(elmId).hide('fast');
            } else {
                $(elmId).show('fast');
            }
        }

        function whenImp() {
            $("#imp-div").hide();
            $("#imp-running").show();
        }
    </script>
    <style type="text/css">
    .content {
        margin-left: 2em;
    }

    .content >ul {
        margin-left: 2em;
        padding-left: .25em;
    }

    .expandable {
        display: none;
    }

    #imp-div {
        padding: 1em;
    }

    #imp-running {
        vertical-align: middle;
        line-height: 16px;
        margin-left: 16px;
    }

    #exp-div {
        margin-left: 2em;
    }

    #exp-div>li {
        padding: 2px 0;
    }
    </style>
</head>

<body>
<div class="content">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <h1>Administration</h1>
    <ul>
        <li><g:link controller="application">Applications</g:link></li>
        <li>
            <g:link onclick="toggleDiv('#imp-div');return false;">Import</g:link>
            <div id="imp-div" class="expandable">
                <g:form action="doimp" method="post" enctype="multipart/form-data" onsubmit="whenImp();">
                    <input type="file" name="file">
                    <input type="submit">
                </g:form>
            </div>

            <div id="imp-running" class="expandable">
                <g:img uri="/images/spinner.gif"/><span style="margin-left: 1em;">Importing...</span>
            </div>
        </li>
        <li>
            <g:link action="exp" onclick="toggleDiv('#exp-div');return false;">Export</g:link>
            <ul id="exp-div" class="expandable">
                <li>
                    <g:link action="expAll">All</g:link>
                </li>
                <g:each in="${apps}" var="app">
                    <li>
                        <g:link action="exp" params="[id: app.id]">${app.label}</g:link>
                    </li>
                </g:each>
            </ul>
        </li>
    </ul>
</div>
</body>
</html>
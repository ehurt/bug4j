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
<%@ page import="org.bug4j.Hit" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>${bug.id} - ${bug.title}</title>
    <script type="text/javascript">
        function whenHitClicked(elm, hitId) {
            $(".hit-row-selected").removeClass('hit-row-selected');
            $(elm).addClass('hit-row-selected');
        ${remoteFunction(action:'hit', update:'hit', params: '\'hitid=\' + hitId')}
        }
    </script>
    <style type="text/css">
    #bug-title {
        font-size: large;
    }

    </style>
</head>

<body>
<div style="margin:5px 10px;">
    <div id="bug-title">${bug.id} - ${bug.title}</div>

    <div id="hits">
        <g:render template="hits" model="[hits: hits]"/>
    </div>
</div>
</body>
</html>

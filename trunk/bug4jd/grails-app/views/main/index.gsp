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
    <script type="text/javascript">
        function preRun(ref, text) {
            $(".result").text('')
            $("#" + ref).text(text)
        }
    </script>
    <style type="text/css">
    .result {
        margin-left: 10px;
    }
    </style>
</head>

<body>
<div style="margin: 30px 0 0 30px">
    <g:remoteLink action="testImport" before="preRun('testImport','Importing');" update="testImport">Import D:/bug4j/bugs.zip</g:remoteLink>
    <span id="testImport" class="result"></span>
</div>

<div style="margin: 30px 0 0 30px">
    <g:remoteLink action="testImport2" before="preRun('testImport2','Importing');" update="testImport2">Import C:/Users/dandoy/Downloads/bug4j/Discovery Manager.xml</g:remoteLink>
    <span id="testImport2" class="result"></span>
</div>

<div style="margin: 30px 0 0 30px">
    <g:remoteLink action="generateStats" before="preRun('generateStats','Running');" update="generateStats">Generate statistics</g:remoteLink>
    <span id="generateStats" class="result"></span>
</div>

<div style="margin: 30px 0 0 30px">
    <g:remoteLink action="generateTestData" before="preRun('generateTestData','Running');" update="generateTestData">Generate test data</g:remoteLink>
    <span id="generateTestData" class="result"></span>
</div>

<div style="margin: 30px 0 0 30px">
    <g:remoteLink action="deleteDemoBugs" before="preRun('deleteDemoBugs','Running');" update="deleteDemoBugs">Clear demo bugs</g:remoteLink>
    <span id="deleteDemoBugs" class="result"></span>
</div>
</body>
</html>
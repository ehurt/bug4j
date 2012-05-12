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
        function preImport(ref) {
            $("#" + ref).text('Importing')
        }
    </script>
</head>

<body>
<div style="margin: 30px 0 0 30px">
    <g:remoteLink action="testImport" before="preImport('testImport');" update="testImport">Import D:/bug4j/bugs.zip</g:remoteLink>
    <span id="testImport" style="margin-left: 10px;"></span>
</div>

<div style="margin: 30px 0 0 30px">
    <g:remoteLink action="testImport2" before="preImport('testImport2');" update="testImport2">Import C:/Users/dandoy/Downloads/bug4j/Discovery Manager.xml</g:remoteLink>
    <span id="testImport2" style="margin-left: 10px;"></span>
</div>
</body>
</html>
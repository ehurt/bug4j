<%--
  ~ Copyright 2011 Cedric Dandoy
  ~
  ~    Licensed under the Apache License, Version 2.0 (the "License");
  ~    you may not use this file except in compliance with the License.
  ~    You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~    Unless required by applicable law or agreed to in writing, software
  ~    distributed under the License is distributed on an "AS IS" BASIS,
  ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~    See the License for the specific language governing permissions and
  ~    limitations under the License.
  --%>

<html>
<head>
    <title>Login Page</title>
    <style type="text/css">
        body {
            background: url('icons/grdback.png') repeat-x;
        }

        td {
            padding: 0;
        }

        #d {
            margin-left: 30%;
            margin-top: 100px;
            width: 300px;
            border: 1px solid #bbbaba;
            background: #ffffff;
            padding: 10px;
        }

        #t {
            width: 100%;
        }

        #h {
            color: #ff6600;
            font-size: larger;
            font-weight: bold;
            text-align: center;
            padding-bottom: 5px;
            border-bottom: 2px solid #ff6600;
            margin-bottom: 5px;
        }

        #e {
            color: #ff6600;
            padding-bottom: 5px;
            border-top: 2px solid #ff6600;
            margin-bottom: 5px;
        }

        #s:hover {
        }

        .btn {
            white-space: nowrap;
            padding: 1px 15px;
            color: #333;
            background-color: #f9f9f9;
            border-width: 1px;
            border-style: solid;
            border-color: #ccc #bbb #a0a0a0;
            text-decoration: none;
            background-image: -moz-linear-gradient(0% 100% 90deg, #e3e3e3, #f9f9f9);
            background-image: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#f9f9f9), to(#e3e3e3));
            border-radius: 3px;
            -moz-border-radius: 3px;
            -webkit-border-radius: 3px;
        }

        .btn:hover {
            border-color: #a0a0a0;
            text-decoration: none;
            background: transparent -moz-linear-gradient(0% 100% 90deg, #d9d9d9, #f5f5f5);
            background: transparent -webkit-gradient(linear, 0% 0%, 0% 100%, to(#d9d9d9), from(#f5f5f5));
        }

        label {
            color: #ff6600;
        }

        #footer {
            background-color: #ffffff;
            position: absolute;
            bottom: 0;
            right: 1px;
            height: 1.5em;
            text-align: right;
            padding: 1px 5px;
        }

        #footer a:visited {
            color: #ff6600;
        }

        #login_back {
            position: absolute;
            bottom: 0;
            left: 0;
            width: 100%;
            height: 642px;
            z-index: -1;
            background: url("icons/login_back.png") top left no-repeat;
        }
    </style>
</head>
<body onload="document.f.j_username.focus();">
<form name="f" action="../j_spring_security_check" method="POST">

    <div id="d">
        <table id="t" border="0">
            <tr>
                <td colspan="2">
                    <div id="h">bug4j login</div>
                </td>
            </tr>
            <tr>
                <td><label for="u">Username:</label></td>
                <td><input id="u" type="text" name="j_username" value=""></td>
            </tr>
            <tr>
                <td><label for="p">Password:</label></td>
                <td><input id="p" type="password" name="j_password"></td>
            </tr>
            <tr>
                <td colspan="2" align="right">
                    <input id="s" class="btn" name="submit" type="submit" value="Login">
                </td>
            </tr>
        </table>
        <%
            if ("1".equals(request.getParameter("login_error"))) {
        %>
        <div id="e">Invalid username/password</div>
        <%
            }
        %>
    </div>
    <div id="footer"><a href="http://www.bug4j.org">bug4j</a></div>
</form>
<div id="login_back"></div>
</body>
</html>
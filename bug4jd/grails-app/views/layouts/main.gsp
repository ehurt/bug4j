<!doctype html>
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

<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title><g:layoutTitle default="Bug4j"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
    <link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
    <link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
    <g:javascript library="jquery" plugin="jquery"/>

    <style>
    #banner-table {
        margin: 0;
    }

    #banner-tr, #banner-tr:hover {
        padding: 0;
        background-color: transparent;
    }

    #banner-logo-td {
        padding: 0;
    }

    #banner-appname-td {
        text-align: left;
        padding-left: 3em;
        white-space: nowrap;
        font-size: 1.5em;
    }

    #banner-last-td {
        text-align: right;
        padding-right: 2em;
        vertical-align: middle;
    }

    #banner-user-menu {
        display: none;
        position: absolute;
        top: 2em;
        right: 10em;
        border: 1px solid #000000;
        height: 5em;
        background: #ffffff;
    }
    </style>
    <g:layoutHead/>
    <r:layoutResources/>
</head>

<body>
<div id="banner" role="banner">
    <table id="banner-table">
        <tr id="banner-tr">
            <td id="banner-logo-td">
                <a href="${createLink(uri: '/')}"><img src="${resource(dir: 'images', file: 'little_swatter.png')}" alt="Bug4j"/></a>
            </td>
            <td id="banner-appname-td">
                ${app?.label}
            </td>
            <td id="banner-last-td" style="width:100%;">
                <sec:ifLoggedIn>
                    Hi <g:link controller="settings"><sec:username/></g:link>

                    <div id="banner-user-menu">
                        <g:link controller="logout">Log out</g:link>
                    </div>
                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        |
                        <g:link controller="admin">
                            Administration
                        </g:link>
                    </sec:ifAllGranted>
                </sec:ifLoggedIn>
                <sec:ifNotLoggedIn>
                    <g:link controller="login" action="auth" params="['spring-security-redirect': createLink(params: params)]">Log in</g:link>
                </sec:ifNotLoggedIn>
            </td>
        </tr>
    </table>
</div>
<g:layoutBody/>
<div class="footer" role="contentinfo"></div>

<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
<g:javascript library="application"/>
<r:layoutResources/>
</body>
</html>
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



<%@ page import="org.bug4j.App" %>
<g:hiddenField name="id" value="${appInstance?.id}"/>

<div class="fieldcontain ${hasErrors(bean: appInstance, field: 'label', 'error')} ">
    <label for="label" style="width: auto;">
        <g:message code="app.label.label" default="Label"/>
    </label>
    <g:textField name="label" value="${appInstance?.label}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: appInstance, field: 'code', 'error')} ">
    <label for="code" style="width: auto;">
        <g:message code="app.code.label" default="Code"/>
    </label>
    <g:textField name="code" value="${appInstance?.code}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: appInstance, field: 'multiHost', 'error')} ">
    <g:checkBox name="multiHost" value="${appInstance.multiHost}"/>
    <label for="multiHost" style="width: auto;">
        <g:message code="app.multiHost.label" default="Expects reports from multiple hosts"/>
    </label>
</div>

<div id="app-pkg-div">
    <table style="width: auto;">
        <tr><td></td><td>Packages</td></tr>
        <g:each in="${appInstance.appPackages?.sort {it.packageName}}" var="appPackage" status="lineno">
            <tr>
                <td>
                    <g:img dir="images/skin" file="delete.png" onclick="deletePkg(\$(this));"/>
                </td>
                <td>
                    <g:textField name="appPackageValue" value="${appPackage.packageName}" size="40"/>
                </td>
            </tr>
        </g:each>
        <tr id="add-pkg-tr-template" style="display: none;">
            <td>
                <g:img dir="images/skin" file="delete.png" onclick="deletePkg(\$(this));"/>
            </td>
            <td>
                <g:textField name="appPackageValue" value="" size="40"/>
            </td>
        </tr>
        <tr id="add-pkg-tr">
            <td></td>
            <td>
                <span style="border-bottom: 1px dotted #000000;cursor: pointer;" onclick="addPkg();">Add</span>
            </td>
        </tr>
    </table>
</div>
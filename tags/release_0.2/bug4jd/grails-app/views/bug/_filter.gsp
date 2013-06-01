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
<g:form id="filter-form-form" action="filter" method="post">
    <table style="width: auto;">
        <tr>
            <td><label for="filterFrom">From</label></td>
            <td>
                <g:textField id="filterFrom" name="filterFrom" value="${from ? from : ''}"/>
                <a href="#" onclick="calFilter.select(document.getElementById('filterFrom'), 'filterFromCalLink', 'MM/dd/yyyy');
                return false;" id="filterFromCalLink">
                    <g:img dir="images" file="calendar.png"/>

                </a>
            </td>
            <td><label for="filterTo">to</label></td>
            <td>
                <g:textField id="filterTo" name="filterTo" value="${to ? to : ''}"/>
                <a href="#" onclick="calFilter.select(document.getElementById('filterTo'), 'filterToCalLink', 'MM/dd/yyyy');
                return false;" id="filterToCalLink">
                    <g:img dir="images" file="calendar.png"/>

                </a>
            </td>
        </tr>
        <tr>
            <td></td>
            <td colspan="3">
                <g:checkBox name="filterIncludeIgnored" checked="${'on' == includeIgnored}"/>
                <label for="filterIncludeIgnored">Include bugs marked as ignored</label>
            </td>
        </tr>
        <g:if test="${app.isMultiHost()}">
            <tr>
                <td></td>
                <td colspan="3">
                    <g:checkBox name="filterIncludeSingleHost" checked="${'on' == includeSingleHost}"/>
                    <label for="filterIncludeSingleHost">Include hits reported from a single host</label>
                </td>
            </tr>
        </g:if>
    </table>

    <div style="margin: 5px 0 0 5px;">
        <g:submitButton name="Clear" onclick="clearWhenFilter();"/>
        <g:submitButton name="Apply"/>
    </div>
</g:form>

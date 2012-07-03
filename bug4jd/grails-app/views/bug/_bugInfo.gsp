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
<div style="margin: 10px 10px;">
    <div style="border: 1px solid #DFDFDF;padding: 5px;margin-top: 5px;">
        <div>${bugData.count} hits</div>

        <div>
            Reported by
            <span class="info-span" title="${bugData.reportedBy}">${bugData.reportedByCount}</span>
            from
            <span class="info-span" title="${bugData.remoteAddr}">${bugData.remoteAddrCount}</span>
        </div>

        <div>
            Reported between
            ${bugData.minDateReported}
            and
            ${bugData.maxDateReported}
        </div>

    </div>

    <g:if test="${sec.loggedInUserInfo(field: "username") || comments}">
        <div id="bugComments" style="border: 1px solid #DFDFDF;padding: 5px;margin-top: 5px;">
            <g:render template="comments" model="[bugId: bugId, comment: bugData.comment]"/>
        </div>
    </g:if>
</div>

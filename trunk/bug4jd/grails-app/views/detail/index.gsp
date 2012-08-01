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
<%@ page import="org.bug4j.server.BugController; java.text.SimpleDateFormat; java.text.DateFormat; grails.converters.JSON; org.bug4j.server.util.DateUtil" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    <title>${bug.id} - ${bug.title}</title>

    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ZeroClipboard.js')}"></script>
    <script type="text/javascript">

        google.load('visualization', '1', {packages:['annotatedtimeline']});
        google.setOnLoadCallback(drawVisualization);

        function drawVisualization() {
            var dataTable = new google.visualization.DataTable(${(timelineData as JSON).toString(true)}, 0.6);

            var annotatedtimeline = new google.visualization.AnnotatedTimeLine(document.getElementById('hit-graph'));
        <% def now = DateUtil.adjustToDayBoundary(new Date(), DateUtil.TimeAdjustType.END_OF_DAY) %>
            annotatedtimeline.draw(dataTable, {
                'displayAnnotations':false,
                'zoomStartTime':new Date(${(now-5).time}),
                'zoomEndTime':new Date(${now.time})
            });
        }

        function addComment() {
            expandSection("comments-section");
            $('#newCommentDiv').show('fast');
            $('#comments-section').show('fast');
            $('#newCommentTextArea').focus();
            return false;
        }

        function collapseSection(sectionId) {
            $("#" + sectionId + "> .section-content").hide('fast');
            $("#" + sectionId + "> div > .collapse-section-img").hide();
            $("#" + sectionId + "> div > .expand-section-img").show();
            setExpandedStatePref(sectionId, 'collapsed');
        }

        function expandSection(sectionId) {
            if ('history-section' == sectionId) {
                $("#" + sectionId + "> .section-content").show();
                drawVisualization();
            } else {
                $("#" + sectionId + "> .section-content").show('fast');
            }
            $("#" + sectionId + "> div > .collapse-section-img").show();
            $("#" + sectionId + "> div > .expand-section-img").hide();
            setExpandedStatePref(sectionId, 'expanded');
        }

        function setExpandedStatePref(sectionId, state) {
            jQuery.ajax({
                type:'POST',
                data:{'section':sectionId, 'state':state},
                url:'${createLink(controller: 'detail',action: 'setExpandedState')}',
                success:function (data, textStatus) {
                },
                error:function (XMLHttpRequest, textStatus, errorThrown) {
                }
            });
        }

        function selectStack(tdId, strainId) {
            $(".stack-id-sel").removeClass("stack-id-sel");
            $(tdId).addClass("stack-id-sel")

            jQuery.ajax({
                type:'POST',
                data:{'id':strainId},
                url:'${createLink(controller: 'stack',action: 'stackOfStrain')}',
                success:function (data, textStatus) {
                    jQuery('#stack-text').html(data);
                    var txt = $("#stack-text-unf").html();
                    clip.setText(txt);
                },
                error:function (XMLHttpRequest, textStatus, errorThrown) {
                }
            });
        }

    </script>

    <style type="text/css">
    table {
        border: none;
    }

    #bug-title {
        font-size: large;
    ${                  bug.ignore?'text-decoration: line-through;':''                  }
    }

    .section {
        margin: 30px;
        padding-left: 15px;
    }

    .action-comment, .action-ignore, .action-unignore, .action-merge {
        background: no-repeat 0.7em center;
        text-indent: 25px;
    }

    .action-comment {
        background-image: url(${resource(dir: 'images/skin', file: 'comment_add.png')});
    }

    .action-ignore {
        background-image: url(${resource(dir: 'images/skin', file: 'delete.png')});
    }

    .action-unignore {
        background-image: url(${resource(dir: 'images/skin', file: 'accept.png')});
    }

    .action-merge {
        background-image: url(${resource(dir: 'images/skin', file: 'arrow_join.png')});
    }

    .info-span {
        background-color: #EFEFEF;
        -moz-border-radius: 0.3em;
        -webkit-border-radius: 0.3em;
        border-radius: 0.3em;
        padding: 0 .3em
    }

    .stack-id {
        width: 11em;
        cursor: pointer;
        white-space: nowrap;
        padding: 5px 0 5px 5px;
    }

    .stack-id-sel {
        background: #f0f8ff;
    }

    #stack-id-div {
        float: left;
        max-height: 400px;
        overflow-y: hidden;
    }

    #stack-text {
        padding: 3px;
        overflow-x: hidden;
        max-height: 400px;
    }

    #stack-copy-container {
        text-align: right;
        margin-right: 25px;
    }

    #stack-copy-button {
        color: #000000;
        border-bottom: dotted 1px #000000;
    }
    </style>
</head>

<body>
<g:if test="${flash.message}">
    <div class="message" role="status">${flash.message}</div>
</g:if>
<div class="nav" role="navigation">
    <ul>
        <li>
            <g:link class="action-back" controller="bug" params="${params.subMap(BugController.PARAM_NAMES - 'offset')}">
                <g:message code="detail.action.back.label" default="Back"/>
            </g:link>
        </li>
        <li>
            <g:link class="action-comment" onclick="return addComment();">
                <g:message code="detail.action.comment.label" default="Comment"/>
            </g:link>
        </li>
        <li>
            <g:if test="${bug.ignore}">
                <g:link class="action-unignore" action="unignore" params="[id: bug.id]">
                    <g:message code="detail.action.unignore.label" default="Un-ignore"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link class="action-ignore" action="ignore" params="[id: bug.id]">
                    <g:message code="detail.action.ignore.label" default="Ignore"/>
                </g:link>
            </g:else>
        </li>
        <li>
            <g:link class="action-merge" controller="merge" action="merge" params="[id: bug.id]">
                <g:message code="detail.action.merge.label" default="Merge"/>
            </g:link>
        </li>
    </ul>
</div>

<div style="margin: 10px 10px 10px 45px;">
    <table style="width: inherit;">
        <tr>
            <td style="padding: 0;">
                <g:if test="${offset}">
                    <g:link params="${params + [offset: offset] + [next: -1]}">
                        <g:img dir="images/skin" file="control_up_16.png"/>
                    </g:link>
                </g:if>
                <g:else>
                    &nbsp;
                </g:else>
            </td>
            <td id="bug-title" rowspan="2" style="padding-top: 4px;">
                ${bug.id} - ${bug.title}
                <g:link params="[id: bug.id]">
                    <g:img dir="images/skin" file="link.png"/>
                </g:link>
            </td>
        </tr>
        <tr>
            <td style="padding: 0;">
                <g:if test="${hasNext}">
                    <g:link params="${params + [offset: offset] + [next: +1]}">
                        <g:img dir="images/skin" file="control_down_16.png"/>
                    </g:link>
                </g:if>
                <g:else>
                    &nbsp;
                </g:else>
            </td>
        </tr>
    </table>
</div>

<div id="history-section" class="section">
    <g:expansionToggle section="history-section">History</g:expansionToggle>

    <g:expansionSection section="history-section" class="section-content">
        <div id="hit-graph" style="width: 95%;height: 300px;"></div>
    </g:expansionSection>
</div>

<% String display = bugInfo.comments ? "" : " display:none;"; %>
<div id="comments-section" class="section" style="${display}">
    <g:expansionToggle section="comments-section">Comments</g:expansionToggle>

    <div id="bugComments" class="section-content" style="border: 1px solid #DFDFDF;padding: 5px;margin-top: 5px;">
        <g:render template="comments" model="[bug: bug, comments: bugInfo.comments]"/>
    </div>
</div>

<div id="stat-section" class="section">
    <g:expansionToggle section="stat-section" expand="false">Stats</g:expansionToggle>

    <g:expansionSection section="stat-section" class="section-content" style="border: 1px solid #DFDFDF;padding: 5px;margin-top: 5px;">
        <div>${bugInfo.count} hits</div>

        <div>
            Reported by
            <span class="info-span" title="${bugInfo.reportedBy}">${bugInfo.reportedByCount}</span>
            from
            <span class="info-span" title="${bugInfo.remoteAddr}">${bugInfo.remoteAddrCount}</span>
        </div>

        <div>
            Reported between
            ${bugInfo.minDateReported}
            and
            ${bugInfo.maxDateReported}
        </div>

    </g:expansionSection>
</div>

<div id="stack-section" class="section">
    <g:expansionToggle section="stack-section" expand="false">
        Stacks
    </g:expansionToggle>

    <g:expansionSection section="stack-section" class="section-content" style="border: 1px solid #DFDFDF;padding: 5px;margin-top: 5px;">
        <g:if test="${strainInfos}">
            <%
                DateFormat stackDateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
            %>

            <div id="stack-id-div">
                <table>
                    <g:each in="${strainInfos}" var="strainInfo" status="lineno">
                        <tr>
                            <% def tdId = "stack-id-${lineno}" %>
                            <td>
                                <div id="${tdId}"
                                     class="stack-id ${lineno == 0 ? 'stack-id-sel' : ''}"
                                     onclick="selectStack('#${tdId}', '${strainInfo[0]}');">
                                    #${strainInfo[0]} <span style="color: #a9a9a9;">${stackDateFormat.format(strainInfo[1])}-${stackDateFormat.format(strainInfo[2])}</span>
                                </div>
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>

            <div>
                <div id="stack-copy-container" style="position:relative">
                    <span id="stack-copy-button">Copy</span>
                </div>

                <div id="stack-text">
                    <g:render template="/stack/stack" model="[stack: firstStack]"/>
                </div>
            </div>

            <div class="clear"></div>

        </g:if>
        <g:else>
            <div>No stack available</div>
        </g:else>
    </g:expansionSection>
</div>

<div id="hit-section" class="section">
    <g:expansionToggle section="hit-section" expand="false">
        Hits
        <span style="color:#999999;font-size: .6em;">
            (<g:link controller="hit" params="[id: bug.id]" style="color:#999999;">Browse...</g:link>)
        </span>
    </g:expansionToggle>

    <g:expansionSection section="hit-section" class="section-content" style="border: 1px solid #DFDFDF;padding: 5px;margin-top: 5px;">
        <div>Total: ${totalHits} hits</div>
        <table class="table-hover">
            <thead>
            <tr>
                <th class="sortable">ID</th>
                <th class="sortable">User</th>
                <th class="sortable">Host</th>
                <th class="sortable">Version</th>
                <th class="sortable">Date Built</th>
                <th class="sortable">Build Number</th>
                <th class="sortable">Dev.Build</th>
                <th class="sortable">Stack</th>
            </tr>
            </thead>
            <g:render template="/hit/hitsRows" model="[hits: hits]"/>
        </table>

    </g:expansionSection>
</div>

<script type="text/javascript">
    ZeroClipboard.setMoviePath('${resource(dir: 'js', file: 'ZeroClipboard.swf')}');
    var clip = new ZeroClipboard.Client();
    var txt = $("#stack-text-unf").html();
    clip.setText(txt);
    clip.setHandCursor(true);
    clip.glue('stack-copy-button', 'stack-copy-container');
</script>
</body>
</html>
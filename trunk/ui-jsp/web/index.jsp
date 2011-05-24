<%@ page import="org.dandoy.bug4j.server.gwt.client.data.Bug" %>
<%@ page import="org.dandoy.bug4j.server.store.Store" %>
<%@ page import="org.dandoy.bug4j.server.store.StoreFactory" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <title>Bug4J</title>
    <link rel="stylesheet" type="text/css" href="tb.css"/>
</head>
<body>
<div id="header"><h1>Bug4J</h1></div>
<div id="wrapper">
    <div id="content">
        <div id="incontent">
            <table class="bug-table">
                <thead>
                <tr>
                    <th id="bug-id-col">ID</th>
                    <th id="bug-subject-col">Subject</th>
                    <th id="bug-hit-col">Hit Count</th>
                </tr>
                </thead>
                <tbody>
                <%
                    final Store store = StoreFactory.getStore();
                    final List<Bug> bugs = store.getBugs("test", 0, 100, "h", false);
                    for (Bug bug : bugs) {
                %>
                <tr onmouseover="style.backgroundColor='lightgray';" onmouseout="style.backgroundColor='white'">
                    <td class="bug-table-id"><%=bug.getId()%>
                    </td>
                    <td class="bug-table-title">
                        <a href="detail.jsp?bugid=<%=bug.getId()%>">
                            <%=bug.getTitle()%>
                        </a>
                    </td>
                    <td class="bug-table-hits"><%=bug.getHitCount()%>
                    </td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div id="navigation">
    <ul>
        <li id="active">Top Bugs</li>
        <li>Menu 2</li>
        <li>Menu 3</li>
        <li>Menu 4</li>
    </ul>
</div>
<div id="footer">
    <%--<p>Powered by <a href="http://www.bug4j.org">Bug4J</a></p>--%>
</div>
</body>
</html>
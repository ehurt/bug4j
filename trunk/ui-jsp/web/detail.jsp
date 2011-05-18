<%@ page import="org.dandoy.bug4j.server.store.StoreFactory" %>
<%@ page import="org.dandoy.bug4j.server.store.Store" %>
<%@ page import="org.dandoy.bug4j.server.store.BugDetail" %>
<%@ page import="org.dandoy.bug4j.server.Util" %>
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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<%
    final String bugidValue = request.getParameter("bugid");
    final long bugid = Long.valueOf(bugidValue);
    final Store store = StoreFactory.getStore();
    final BugDetail bugDetail = store.getBug(bugid);
%>
<head>
    <title>Bug4J - <%=bugDetail.getId()%>-<%=Util.encode(bugDetail.getTitle())%>
    </title>
    <link rel="stylesheet" type="text/css" href="tb.css"/>
</head>
<body>
<div id="header"><h1>Bug4J</h1></div>
<div id="wrapper">
    <div id="content">
        <div id="incontent">
            <div id="crumbs"><a href="/">Top Bugs</a> > bug<%=bugDetail.getId()%></div>
            <H1>
                <%=bugDetail.getId()%>-<%=Util.encode(bugDetail.getTitle())%>
            </H1>
            <div id="bug-msg"><%=bugDetail.getMessage()%></div>
            <div id="bug-exmsg"><%=bugDetail.getExceptionMessage()%></div>
            <div id="bug-stack"><%=bugDetail.getStackText()%></div>
        </div>
    </div>
</div>
<div id="navigation">
    <ul>
        <li id="active"><a href="/">Top Bugs</a></li>
        <li >Menu 2</li>
        <li >Menu 3</li>
        <li >Menu 4</li>
    </ul>
</div>
<div id="footer"><p>Powered by <a href="http://www.bug4j.org">Bug4J</a></p></div>
</body>
</html>
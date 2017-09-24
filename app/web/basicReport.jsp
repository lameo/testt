<%@page import="java.util.Arrays"%>
<%@page import="model.User"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.time.Instant"%>

<%
    if (session.getAttribute("admin") != null && session.getAttribute("admin").equals("admin")) { //check if admin arrive page via link or through login
        response.sendRedirect("adminPage.jsp"); //send back to admin page
        return;
    } else if (session.getAttribute("user") == null) { //check if user arrive page via link or through login
        response.sendRedirect("index.jsp"); //send back to index page
        return;
    }
%>

<!DOCTYPE html>
<%@include file="clearCache.jsp"%> <%-- clear cache, don't allow user to backpage after logging out --%>
<html>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link href="css/bootstrap.css" rel="stylesheet"> <%-- twitter bootstrap for designing--%>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script> <%-- twitter bootstrap for designing--%>
    <script src='js/bootstrap.js'></script> <%-- twitter bootstrap for designing--%>

    <%  //user details, get using session
        User user = (User) session.getAttribute("user");
        String name = user.getName();
        String timestamp = (String) session.getAttribute("timestamp");

        //basic location report details, get using request
        String timedate = (String) request.getAttribute("timeDate");
        String topK = (String) request.getAttribute("topK");
    %>
    <head>
        <title>Reports Page</title>
    </head>
    <body>
        <nav class="navbar navbar-inverse"> <%-- navigation menu for user to click --%>
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="userPage.jsp">SLOCA</a>
                </div>
                <ul class="nav navbar-nav">
                    <li><a href="userPage.jsp">Home</a></li> <%-- send user to home page--%>
                    <li class="active"><a href="reportsPage.jsp">Basic Location Reports</a></li> <%-- set as active because user is in reports page. send user to reports page --%>
                    <li><a href="heatmapPage.jsp">Heat Map</a></li> <%-- send user to heatmap page --%>
                </ul>
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="userPage.jsp"><%="Welcome " + name + "!"%></a></li>
                    <li><a href="processLogout"><span class="glyphicon glyphicon-log-out"></span> Logout</a></li> <%-- send user to logout servlet and process logout --%>
                </ul>
            </div>
        </nav>
        <div class="container">
            <!-- first row -->
            <div class="row" style="margin-top:2.5%;text-align:center;">
                <div href="index.jsp" class="col-xs-6 col-lg-6">
                    <a href="basicReport.jsp">
                        <button  type="button"  class="btn"><b>Breakdown by Year & Gender</b></button>
                    </a>
                </div>
                <div class="col-xs-6 col-lg-6">
                    <a href="topKPop.jsp">
                        <button type="button" class="btn"><b>Top-K Popular Places</b></button>
                    </a>
                </div>
            </div>
            <!-- second row -->
            <div class="row" style="margin-top:5%;text-align:center;">
                <div class="col-xs-6 col-lg-6">
                    <button type="button" class="btn"><b>Top-K Next Places</b></button>
                </div>
                <div class="col-xs-6 col-lg-6">
                    <button type="button" class="btn"><b>Top-K Companions</b></button>
                </div>
            </div>
        </div>
        <%
            //debug
            out.print("<br>Top-K Companions & Top-K Next Places not working yet ");
            out.print("<br>Enter in this format: 2014-03-23 13:40:00");
        %>
    <center><%="<br>User session: " + timestamp%></center>
</body>
</html>

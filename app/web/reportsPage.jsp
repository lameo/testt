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
                <div class="col-xs-6 col-lg-6">
                    <button type="button" class="btn"><b>Breakdown by Year & Gender</b></button>
                </div>
                <div class="col-xs-6 col-lg-6">
                    <button type="button" class="btn"><b>Top-K Popular Places</b></button>
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

        <br><br>
        <form method=post action="report">
            <table>
                <!-- first row -->
                <tr>
                    <td><input type="radio" name="reportType" value="breakdownReport" checked></td>
                    <td>Breakdown by Year & Gender&ensp;&ensp;</td>
                    <td><input type="text" name="timeDate" size="25" placeholder="Enter date and time" required/></td>
                </tr>
                <!-- second row -->
                <tr>
                    <td><input type="radio" name="reportType" value="topKPopular"></td>
                    <td>Top-K Popular Places</td>
                    <td><select name = "topK">
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option selected value="3">3</option>
                            <option value="4">4</option>
                            <option value="5">5</option>
                            <option value="6">6</option>
                            <option value="7">7</option>
                            <option value="8">8</option>
                            <option value="9">9</option>
                            <option value="10">10</option>
                        </select></td>
                </tr>
                <!-- third row -->
                <tr>
                    <td><input type="radio" name="reportType" value="tnp"></td>
                    <td>Top-K Next Places</td>
                    <td><input type="submit" value ="Generate"/></td>
                </tr>
                <!-- forth row -->
                <tr>
                    <td><input type="radio" name="reportType" value="tcc"></td>
                    <td>Top-K Companions</td>
                </tr>
            </table>
        </form>
        <%
            //if basic report is generated
            float totalCount = 0;
            if (request.getAttribute("breakdownReport") != null) {
                String breakdownReport = (String) (request.getAttribute("breakdownReport"));
                String[] y = breakdownReport.split(",");
                totalCount = Integer.parseInt(y[0]) + Integer.parseInt(y[1]);
                out.print("<h3>Breakdown by Year & Gender at " + timedate + "</h3>");
                out.print("<table border=\"1\">");
                out.print("<tr><td></td><td>Male</td><td>Female</td><tr>");
                out.print("<tr><td>2010</td><td>" + Math.round((Integer.parseInt(y[2]) / totalCount * 100) * 10) / 10.0 + "%</td><td>" + Math.round((Integer.parseInt(y[3]) / totalCount * 100) * 10) / 10.0 + "%</td><tr>");
                out.print("<tr><td>2011</td><td>" + Math.round((Integer.parseInt(y[4]) / totalCount * 100) * 10) / 10.0 + "%</td><td>" + Math.round((Integer.parseInt(y[5]) / totalCount * 100) * 10) / 10.0 + "%</td><tr>");
                out.print("<tr><td>2012</td><td>" + Math.round((Integer.parseInt(y[6]) / totalCount * 100) * 10) / 10.0 + "%</td><td>" + Math.round((Integer.parseInt(y[7]) / totalCount * 100) * 10) / 10.0 + "%</td><tr>");
                out.print("<tr><td>2013</td><td>" + Math.round((Integer.parseInt(y[8]) / totalCount * 100) * 10) / 10.0 + "%</td><td>" + Math.round((Integer.parseInt(y[9]) / totalCount * 100) * 10) / 10.0 + "%</td><tr>");
                out.print("<tr><td>2014</td><td>" + Math.round((Integer.parseInt(y[10]) / totalCount * 100) * 10) / 10.0 + "%</td><td>" + Math.round((Integer.parseInt(y[11]) / totalCount * 100) * 10) / 10.0 + "%</td><tr>");
                out.print("<tr><td>Total:</td><td>" + Math.round((Integer.parseInt(y[0]) / totalCount * 100) * 10) / 10.0 + "%</td><td>" + Math.round((Integer.parseInt(y[1]) / totalCount * 100) * 10) / 10.0 + "%</td><tr>");
                out.print("</table>");
            }

            //If top K report is generated
            if (request.getAttribute("topKPopular") != null) {
                out.print("<h3>Top-" + topK + " Popular Places at " + timedate + "</h3>");
                out.print("<table border=\"1\">");
                String topKPopular = (String) (request.getAttribute("topKPopular"));
                String[] y = topKPopular.split(",");
                out.print("<table border=\"1\">");
                out.print("<tr><td>Rank</td><td>Semantic place</td><td>No pax</td></tr>");
                for (int j = 0; j < y.length; j += 2) {
                    out.print("<tr><td>" + (j / 2 + 1) + "</td><td>" + y[j] + "</td><td>" + y[j + 1] + "</td></tr>");
                }
                out.print("</table>");
            }

            //debug
            out.print("<br>Top-K Companions & Top-K Next Places not working yet ");
            out.print("<br>Enter in this format: 2014-03-23 13:40:00");
        %>
    <center><%="<br>User session: " + timestamp%></center>
</body>
</html>

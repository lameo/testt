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
    %>
    <head>
        <title>Automatic Group Detection</title>
    </head>
    <body>
        <nav class="navbar navbar-inverse"> <%-- navigation menu for user to click --%>
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="userPage.jsp">SLOCA</a>
                </div>
                <ul class="nav navbar-nav">
                    <li><a href="userPage.jsp">Home</a></li> <%-- send user to home page--%>
                    <li><a href="reportsPage.jsp">Basic Location Reports</a></li> <%-- set as active because user is in reports page. send user to reports page --%>
                    <li><a href="heatmapPage.jsp">Heat Map</a></li> <%-- send user to heatmap page --%>
                    <li class="active"><a href="automaticGroupDetection.jsp">Automatic Group Detection</a></li> <%-- send user to Automatic Group Detection page --%>
                </ul>
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="userPage.jsp"><%="Welcome " + name + "!"%></a></li>
                    <li><a href="processLogout"><span class="glyphicon glyphicon-log-out"></span> Logout</a></li> <%-- send user to logout servlet and process logout --%>
                </ul>
            </div>
        </nav>
    <center>

        <div class="container">
            <br><br>
            <!-- Form for user to input date&time and top K for top K popular places report -->
            <form method=post action="xyChangeHere">
                <!-- report type -->
                <input type="hidden" name="andHere" value="xyChangeHereTooooooo">
                <!-- form input for date & time  -->
                <div class="form-group">
                    <label class="form-control-label" for="timing">Enter date & time:</label>
                    <input type="text" class="form-control" id="timing" name="timeDate" placeholder="Example: 2014-03-23 13:40:00" required>
                </div>
                <!-- form input for mac-address  -->
                <div class="form-group">
                    <label class="form-control-label" for="locationGetter">Enter MAC Addresse:</label>
                    <input type="text" class="form-control" id="locationGetter" name="location" placeholder="Example: 009562b08360d78848a977dc26368b53cc0f1d44" required>
                </div>
                <button type="submit" class="btn btn-primary">Generate</button>
            </form>



        </div>
        <%
            //If top K report is generated
            if (request.getAttribute("topK") != null) {

                String timedate = request.getParameter("timeDate");
                String topK = (String) request.getAttribute("topK");
                out.print("<h3>Top-" + topK + " Popular Places at " + timedate + "</h3>");
                
                
                
                out.print("<div class=\"container\"><table class=\"table table-bordered\"><thead>");
                String topKPopular = (String) (request.getAttribute("topKPopular"));
                String[] y = topKPopular.split(",");
                out.print("<tr><th>Rank</th><th>Semantic place</th><th>No pax</th></tr></thead></tbody>");
                for (int j = 0; j < y.length; j += 2) {
                    out.print("<tr><td>" + (j / 2 + 1) + "</td><td>" + y[j] + "</td><td>" + y[j + 1] + "</td></tr>");
                }
                out.print("</tbody></table></div>");
            }
        %>

        <%="<br>Example: 2014-03-23 13:40:00"%>
        <%="<br>Mac address: 009562b08360d78848a977dc26368b53cc0f1d44"%>
        <%="<br>User session: " + timestamp%>
    </center>
</body>
</html>
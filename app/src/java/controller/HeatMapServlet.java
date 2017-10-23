package controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.HeatMap;
import model.HeatMapDAO;

public class HeatMapServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        String timeDate = request.getParameter("timeDate"); //retrieve time from user input
        timeDate = timeDate.replace("T", " ");
        SimpleDateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date timestamp = null;
        try {
            timestamp = (Date) readFormat.parse(timeDate);
            timeDate = writeFormat.format(timestamp);

            int floor = Integer.parseInt(request.getParameter("floor")); //retrieve floor from user input
            String floorName = "B1";

            if (floor > 0) {
                floorName = "L" + floor;
            }

            HashMap<String, HeatMap> heatmapList = HeatMapDAO.retrieveHeatMap(timeDate, floorName);

            session.setAttribute("floorName", floorName);
            session.setAttribute("timeDate", timeDate);
            session.setAttribute("heatmapList", heatmapList);

            response.sendRedirect("heatmapPage.jsp"); //send back to heatmapPage
            
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "This is a Heatmap Servlet to process heatmap";
    }// </editor-fold>

}

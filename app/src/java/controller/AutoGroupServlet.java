/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.AutoGroupDAO;
import static model.AutoGroupDAO.retrieveAutoGroups;
import model.Group;
import model.ReportDAO;

/**
 *
 * @author xuying
 */
public class AutoGroupServlet extends HttpServlet {

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
            throws ServletException, IOException, ParseException {

        try (PrintWriter out = response.getWriter()) {
            HttpSession session = request.getSession();

            String timeDate = request.getParameter("timeDate"); //retrieve time from user input
            timeDate = timeDate.replace("T", " ");
            SimpleDateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            SimpleDateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            Date timestamp = null;
            try {
                timestamp = (Date) readFormat.parse(timeDate);
                timeDate = writeFormat.format(timestamp);
                //System.out.println("Retrieved and formatted dateTime: " + timestamp.toString());
            } catch (ParseException e) {
                System.out.println("Date formatter failed to parse chosen sendTime.");
                e.printStackTrace();
            }
            //retreive all the users whom stay at SIS building in specified time window
            ArrayList<String> AutoUsers = AutoGroupDAO.retreiveAutoUserMacaddresses(timeDate);
            Map<String,ArrayList<String>> ValidAutoUsers = new HashMap<String,ArrayList<String>>();
            //retrieve group of users whom stay at SIS in processing window
            for (int i = 0; i < AutoUsers.size(); i += 1) {
                String AutoUserMac = AutoUsers.get(i);
                //retreive location traces of each user 
                ArrayList<String> AutoUserLocationTimestamps = ReportDAO.retrieveUserLocationTimestamps(AutoUserMac,timeDate);
                //check if user stays at SIS building for at least 12 minutes
                if(AutoGroupDAO.AutoUser12Mins(AutoUserLocationTimestamps)){
                    ValidAutoUsers.put(AutoUserMac, AutoUserLocationTimestamps);
                }
            }
            ArrayList<Group> AutoGroups = retrieveAutoGroups(timeDate);

            session.setAttribute("timeDate", timeDate);
            session.setAttribute("test", ValidAutoUsers);

            response.sendRedirect("automaticGroupDetection.jsp");
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
        try {
            processRequest(request, response);
        } catch (ParseException ex) {
            Logger.getLogger(AutoGroupServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            processRequest(request, response);
        } catch (ParseException ex) {
            Logger.getLogger(AutoGroupServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

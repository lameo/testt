package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HeatMap;
import model.HeatMapDAO;
import model.SharedSecretManager;

/**
 * A servlet that manages inputs from url and results from HeatmapDAO.
 * Contains processRequest, doPost, doGet, getServletInfo methods
 */
@WebServlet(urlPatterns = {"/json/heatmap"})
public class Heatmap extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        //creates a new gson object by instantiating a new factory object, set pretty printing, then calling the create method
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //creats a new json object for printing the desired json output
        JsonObject jsonOutput = new JsonObject();
        //create a json array to store errors
        JsonArray errMsg = new JsonArray();

        
        //get token from request
        String tokenEntered = request.getParameter("token");
        // check if token is null (dont have ?token=something)
        if (tokenEntered == null) {
            errMsg.add("missing token");
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("messages", errMsg);
            out.println(gson.toJson(jsonOutput));
            out.close(); //close PrintWriter
            return;
        }

        // check if token is empty (?token="")
        if (tokenEntered.isEmpty()) {// if token given is not valid
            errMsg.add("blank token");
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("messages", errMsg);
            out.println(gson.toJson(jsonOutput));
            out.close(); //close PrintWriter
            return;
        }

        // checking if the token submitted by the user is valid
        if (!SharedSecretManager.verifyUser(tokenEntered)) {
            // if token given is not valid
            errMsg.add("invalid token");
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("messages", errMsg);
            out.println(gson.toJson(jsonOutput));
            out.close(); //close PrintWriter
            return;
        }

        
        //get floor from request
        String stringFloor = request.getParameter("floor");
        // check if floor is null (dont have ?floor=something)
        if (stringFloor == null) {
            errMsg.add("missing floor");
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("messages", errMsg);
            out.println(gson.toJson(jsonOutput));
            out.close(); //close PrintWriter
            return;
        }

        // check if floor is empty (?floor="")
        if (stringFloor.isEmpty()) {
            errMsg.add("blank floor");
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("messages", errMsg);
            out.println(gson.toJson(jsonOutput));
            out.close(); //close PrintWriter
            return;
        }
        

        //get date from request
        String timeDate = request.getParameter("date");
        // check if date is null (dont have ?date=something)
        if (timeDate == null) {
            errMsg.add("missing date");
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("messages", errMsg);
            out.println(gson.toJson(jsonOutput));
            out.close(); //close PrintWriter
            return;
        }

        // check if date is empty (?date="")
        if (timeDate.isEmpty()) {
            errMsg.add("blank date");
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("messages", errMsg);
            out.println(gson.toJson(jsonOutput));
            out.close(); //close PrintWriter
            return;
        }
        
        // After this point, all variables required are not empty or null, so start checking whether they are valid format
        try {
            //check for valid date entered by user
            boolean dateValid = true;
            // Length check
            dateValid = dateValid && timeDate.length() == 19;
            // Year bigger than 2013 & smaller or equal to 2017
            dateValid = dateValid && (Integer.parseInt(timeDate.substring(0, 4)) > 2013) && (Integer.parseInt(timeDate.substring(0, 4)) <= 2017);
            // Check for dashes
            dateValid = dateValid && (timeDate.substring(4, 5).equals("-"));
            // Month bigger than 0 & smaller or equal to 12
            dateValid = dateValid && (Integer.parseInt(timeDate.substring(5, 7)) > 0) && (Integer.parseInt(timeDate.substring(5, 7)) <= 12);
            // Check for dashes
            dateValid = dateValid && (timeDate.substring(7, 8).equals("-"));
            // Day bigger than 0 & smaller or equal to 12
            dateValid = dateValid && (Integer.parseInt(timeDate.substring(8, 10)) > 0) && (Integer.parseInt(timeDate.substring(8, 10)) <= 31);
            // Check for T
            dateValid = dateValid && (timeDate.substring(10, 11).equals("T"));
            // Hour bigger or equal 0 & smaller or equal to 24
            dateValid = dateValid && (Integer.parseInt(timeDate.substring(11, 13)) >= 0) && (Integer.parseInt(timeDate.substring(11, 13)) <= 23);
            // Check for :
            dateValid = dateValid && (timeDate.substring(13, 14).equals(":"));
            // Min bigger or equal 0 & smaller or equal to 59
            dateValid = dateValid && (Integer.parseInt(timeDate.substring(14, 16)) >= 0) && (Integer.parseInt(timeDate.substring(14, 16)) <= 59);
            // Check for :
            dateValid = dateValid && (timeDate.substring(16, 17).equals(":"));
            // Second bigger or equal 0 & smaller or equal to 59
            dateValid = dateValid && (Integer.parseInt(timeDate.substring(17, 19)) >= 0) && (Integer.parseInt(timeDate.substring(17, 19)) <= 59);
            if (!dateValid) {
                errMsg.add("invalid date");
            }
        } catch (NumberFormatException e) {
            errMsg.add("invalid date");
        }

        // checking if floor number is out of bounds
        int floor = Integer.parseInt(stringFloor);
        if (floor < 0 || floor > 5) {
            errMsg.add("invalid floor");
        }

        //if all checks are valid, continue processing
        if (errMsg.size() == 0) {
            //proper date format -> (YYYY-MM-DDTHH:MM:SS)
            //replace "T" with "" to allow system to process correctly
            timeDate = timeDate.replaceAll("T", " ");
            
            // if floor = 0, floor is B1
            String floorName = "B1";

            // reassign floorName to corresponding level
            // Eg: if floor is 2, floorName equals to "L2"
            if (floor > 0) {
                floorName = "L" + floor;
            }

            // Retrieving all the rooms on that level with their qty and heatmap level 
            Map<String, HeatMap> heatmapMap = HeatMapDAO.retrieveHeatMap(timeDate, floorName);
            jsonOutput.addProperty("status", "success");
            JsonArray heatmaps = new JsonArray();

            //create a list of string of all semantic places based on the number of semantic places present in heatmapList
            List<String> keys = new ArrayList<>(heatmapMap.keySet());

            //sort the list before retrieval
            Collections.sort(keys);
            for (String key : keys) {

                //retrieve HeatMap object from list of sorted from heatmapMap
                HeatMap heatmap = heatmapMap.get(key);

                //create json object to add into json array for final output
                //every key will be stored into a new json object
                JsonObject heatmapObject = new JsonObject();

                //adding property and items for output
                heatmapObject.addProperty("semantic-place", heatmap.getPlace());
                heatmapObject.addProperty("num-people", heatmap.getQtyPax());
                heatmapObject.addProperty("crowd-density", heatmap.getHeatLevel());

                //add json object into json array for final output
                heatmaps.add(heatmapObject);
            }
            //add json array to final json object to output
            jsonOutput.add("heatmap", heatmaps);
            
            
        //if date or level is not valid, send error message
        } else {
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("messages", errMsg);
        }
        
        
        // Returning the json output we created in a pretty print format
        out.println(gson.toJson(jsonOutput));
        // close PrintWriter
        out.close(); 
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
        return "Short description";
    }// </editor-fold>

}

package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javazoom.upload.MultipartFormDataRequest;
import javazoom.upload.UploadException;
import model.SharedSecretManager;
import model.User;
import model.UserDAO;

@WebServlet(urlPatterns = {"/json/authenticate"})
public class Authenticate extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        //creates a new gson object by instantiating a new factory object, set pretty printing, then calling the create method
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //creates a new json object for printing the desired json output
        JsonObject jsonOutput = new JsonObject();

        JsonArray errMsg = new JsonArray();

        try {
            String username = null;
            String password = null;
            if (MultipartFormDataRequest.isMultipartFormData(request)) {
                //Uses MultipartFormDataRequest to parse the HTTP request.
                MultipartFormDataRequest multipartRequest = new MultipartFormDataRequest(request); //specialized version of request object to interpret the data

                username = multipartRequest.getParameter("username"); //get username from request
                password = multipartRequest.getParameter("password"); //get password from request   
            } else {
                username = request.getParameter("username"); //get username from request
                password = request.getParameter("password"); //get password from request   
            }
            
            if (username == null) { //check if username is null (i.e username field is not entered in url)
                errMsg.add("missing username");
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errMsg);
                out.println(gson.toJson(jsonOutput));
                out.close(); //close PrintWriter
                return;
            }

            if (username.isEmpty()) { //check if username is empty
                errMsg.add("blank username");
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errMsg);
                out.println(gson.toJson(jsonOutput));
                out.close(); //close PrintWriter
                return;
            }

            if (password == null) { //check if password is null (i.e password field is not entered in url)
                errMsg.add("missing password");
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errMsg);
                out.println(gson.toJson(jsonOutput));
                out.close(); //close PrintWriter
                return;
            }

            if (password.isEmpty()) { //check if password is empty
                errMsg.add("blank password");
                jsonOutput.addProperty("status", "error");
                jsonOutput.add("messages", errMsg);
                out.println(gson.toJson(jsonOutput));
                out.close(); //close PrintWriter
                return;
            }

            if (username.equals("admin") && password.equals("Password!SE888")) { //admin credentials
                String token = SharedSecretManager.authenticateAdmin();
                jsonOutput.addProperty("status", "success");
                jsonOutput.addProperty("token", token);
            } else if (UserDAO.validateUsername(username)) { //if username is valid e.g. john.doe.2016
                User user = UserDAO.retrieveUserByName(username, password);

                if (user instanceof User) { //if user in database
                    String token = SharedSecretManager.authenticateUser(user.getName());
                    jsonOutput.addProperty("status", "success");
                    jsonOutput.addProperty("token", token);
                } else {
                    errMsg.add("invalid username/password");
                }
            } else {
                errMsg.add("invalid username/password");
            }

        } catch (SQLException e) {
            errMsg.add("server is currently unavailable, please try again later. Thank you.");
        } catch (UploadException e) {
            out.println("error, Unable to upload. Please try again later");
        }

        if (errMsg.size() > 0) {
            jsonOutput.addProperty("status", "error");
            jsonOutput.add("messages", errMsg);
        }
        out.println(gson.toJson(jsonOutput));

        out.close(); // close PrintWriter
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
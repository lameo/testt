package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that contains all the logic to generate reports
 */
public class ReportDAO {

    /**
     * Returns a list of all the semantic places in the database
     *
     * @return ArrayList of all the semantic places
     *
     */
    public static ArrayList<String> getSemanticPlaces() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement
            preparedStatement = connection.prepareStatement("select distinct locationname from locationlookup order by locationname asc");

            //execute SQL query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
        }
        return list;
    }

    /**
     * Returns a list of all the macaddress in the database
     *
     * @return ArrayList of all the macaddress
     *
     */
    public static ArrayList<String> getAllMacaddress() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement
            //retrieve all the macaddresses found besides those from demographics.csv
            preparedStatement = connection.prepareStatement("select distinct macaddress from location");

            //execute SQL query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
        }
        return list;
    }

    /**
     * Returns a total quantity of users of a specific gender 15mins before the
     * specified time
     *
     * @param timeEnd String in dd/mm/yyyy hh:mm format for when the report is
     * generated
     * @param gender String either "M" or "F"
     * @return int of a specific gender 15mins before the specified time
     *
     */
    private static int retrieveByGender(String timeEnd, String gender) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String ans = "";
        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement
            preparedStatement = connection.prepareStatement("select count(DISTINCT l.macaddress) "
                    + "from location l, demographics d "
                    + "where  timestamp between DATE_SUB(?,INTERVAL 15 MINUTE) and DATE_SUB(?,INTERVAL 1 SECOND) "
                    + "and l.macaddress = d.macaddress and gender = ?");

            //set the parameters
            preparedStatement.setString(1, timeEnd);
            preparedStatement.setString(2, timeEnd);
            preparedStatement.setString(3, gender);

            //execute SQL query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ans = resultSet.getString(1);
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
        }
        return Integer.parseInt(ans);
    }

    /**
     * Returns a total quantity of unique macaddress of a specific school 15mins
     * before the specified time
     *
     * @param timeEnd String in dd/mm/yyyy hh:mm:ss format for when the report
     * is generated
     * @param school String of the school to retrieve qty
     * @return int of a specific school 15mins before the specified time
     *
     */
    private static int retrieveByEmail(String timeEnd, String school) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String ans = "";
        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement
            preparedStatement = connection.prepareStatement("select count(DISTINCT l.macaddress) "
                    + "from location l, demographics d "
                    + "where timestamp between DATE_SUB(?,INTERVAL 15 MINUTE) and DATE_SUB(?,INTERVAL 1 SECOND) "
                    + "and l.macaddress = d.macaddress and email like ?");

            //set the parameters
            preparedStatement.setString(1, timeEnd);
            preparedStatement.setString(2, timeEnd);
            preparedStatement.setString(3, "%" + school + "%");

            //execute SQL query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ans = resultSet.getString(1);
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
        }
        return Integer.parseInt(ans);
    }

    /**
     * Returns a total quantity of unique macaddress 15mins before the specified
     * time
     *
     * @param timeEnd String in dd/mm/yyyy hh:mm format for when the report is
     * generated
     * @return int of a specific school 15mins before the specified time
     *
     */
    private static int everyoneWithinTime(String timeEnd) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String ans = "";
        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement
            preparedStatement = connection.prepareStatement("select count(DISTINCT l.macaddress) "
                    + "from location l, demographics d "
                    + "where timestamp between DATE_SUB(?,INTERVAL 15 MINUTE) and DATE_SUB(?,INTERVAL 1 SECOND) "
                    + "and l.macaddress = d.macaddress");

            //set the parameters
            preparedStatement.setString(1, timeEnd);
            preparedStatement.setString(2, timeEnd);

            //execute SQL query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ans = resultSet.getString(1);
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
        }
        return Integer.parseInt(ans);
    }

    /**
     * Returns the top 10 populated semantic places with qty of unique
     * macaddress 15mins before the specified time
     *
     * @param time String in dd/mm/yyyy hh:mm format for when the report is
     * generated
     * @return Map with Key of places with the qty of unique macaddress 15mins
     * before the specified time and Value of semantic place name
     *
     */
    public static Map<Integer, String> retrieveTopKPopularPlaces(String time) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Map<Integer, String> map = new TreeMap<>(Collections.reverseOrder());
        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement
            //retrieve location name and corresponding number of people at the location. But only retrieve latest (max(timestamp)) location updates of user only
            preparedStatement = connection.prepareStatement("select n.locationname, count(n.locationname) "
                    + "from (SELECT max(TIMESTAMP) as TIMESTAMP, macaddress "
                    + "FROM location "
                    + "WHERE timestamp BETWEEN (SELECT DATE_SUB(?,INTERVAL 15 MINUTE)) AND (SELECT DATE_SUB(?,INTERVAL 1 SECOND)) "
                    + "group by macaddress) l, location m, locationlookup n "
                    + "where l.macaddress = m.macaddress and m.timestamp = l.timestamp and m.locationid = n.locationid "
                    + "group by n.locationname");

            //set the parameters
            preparedStatement.setString(1, time);
            preparedStatement.setString(2, time);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                if (map.containsKey(resultSet.getInt(2))) {
                    String previousResults = map.get(resultSet.getInt(2)); //get previous results
                    map.put(resultSet.getInt(2), previousResults + ", " + resultSet.getString(1)); //set into map
                } else {
                    map.put(resultSet.getInt(2), resultSet.getString(1)); //set new results into map
                }
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
        }
        return map;
    }

    /**
     * Returns the top 10 next popular places that users located at a specific
     * location are likely to visit in the next 15 minutes.
     *
     * @param inputTime String in dd/mm/yyyy hh:mm format for when the report is
     * generated
     * @param locationName String semantic name of the starting location
     * @return Map with value of ArrayList of semantic place sharing the same
     * qty of user and Integer key of qty of users that arrived from the
     * starting location
     *
     */
    public static Map<Integer, ArrayList<String>> retrieveTopKNextPlaces(String inputTime, String locationName) {
        ArrayList<String> usersList = retrieveUserBasedOnLocation(inputTime, locationName); //to retrieve all users who are in a specific place in given a specific time frame and location
        Map<String, Integer> nextPlacesMap = new HashMap<>();
        for (int i = 0; i < usersList.size(); i++) { // loop through all the users retrieved from retrieveUserBasedOnLocation() mtd
            String location = retrieveTimelineForUser(usersList.get(i), inputTime); // retrieve the latest location user spends at least 5 min
            if (location != null && location.length() > 0) { // check if there is a location
                if (nextPlacesMap.get(location) == null) { //nextPlacesMap is empty
                    nextPlacesMap.put(location, 1); // to initialise nextPlacesMap to set a default value as 1
                } else { //nextPlacesMap is not empty
                    int currentQuantity = nextPlacesMap.get(location);
                    int addOnQuantity = currentQuantity + 1;
                    nextPlacesMap.put(location, addOnQuantity); // increment the counter if location appears for every different user from usersList
                }
            }
        }

        //TreeMap is sorted by keys
        Map<Integer, ArrayList<String>> ranking = new TreeMap<>(Collections.reverseOrder()); //sort keys in descending order
        Set<String> locationKeys = nextPlacesMap.keySet(); // to retrieve all the keys(i.e location) from nextPlacesMap

        for (String location : locationKeys) {
            int totalNumOfUsers = nextPlacesMap.get(location); //for each key(i.e location) in keys, retrieve the total number of users in the location
            ArrayList<String> allLocationList = ranking.get(totalNumOfUsers); //list is to group all the different locations with the same quantity
            if (allLocationList == null || allLocationList.size() < 0) { // when the ranking map is empty
                ArrayList<String> sameLocations = new ArrayList<>();
                sameLocations.add(location);
                ranking.put(totalNumOfUsers, sameLocations);//to add all locations with the same quantity into map
            } else { // ranking map contains a list of locations
                allLocationList.add(location); // update the key(i.e location) into the list
                Collections.sort(allLocationList);
                ranking.put(totalNumOfUsers, allLocationList); // update map
            }
        }
        return ranking;
    }

    /**
     * Return users who are in a specific place 15mins before the specified time
     * at a specific location
     *
     * @param inputTime String in dd/mm/yyyy hh:mm format for when the report is
     * generated
     * @param locationName String semantic name of the starting location
     * @return ArrayList list of users who are in a specific place 15mins before
     * the specified time at a specific location
     *
     */
    public static ArrayList<String> retrieveUserBasedOnLocation(String inputTime, String locationName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ArrayList<String> usersInSpecificPlace = new ArrayList<String>();

        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement to retrieve users who are in a specific place in a given time frame in a specific location
            preparedStatement = connection.prepareStatement("SELECT distinct l.macaddress "
                    + "FROM (SELECT max(TIMESTAMP) as TIMESTAMP, macaddress "
                    + "FROM location "
                    + "WHERE timestamp BETWEEN (SELECT DATE_SUB(?,INTERVAL 15 MINUTE)) AND (SELECT DATE_SUB(?,INTERVAL 1 SECOND)) "
                    + "group by macaddress) as temp, locationlookup llu, location l "
                    + "WHERE temp.timestamp = l.timestamp "
                    + "AND l.locationid = llu.locationid "
                    + "AND temp.macaddress = l.macaddress "
                    + "AND llu.locationname = ?");

            //set the parameters
            preparedStatement.setString(1, inputTime);
            preparedStatement.setString(2, inputTime);
            preparedStatement.setString(3, locationName);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String result = resultSet.getString(1); // retrieves the user
                usersInSpecificPlace.add(result); // add into list to collate all users in the specific location
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
        }
        return usersInSpecificPlace;
    }

    /**
     * Returns the latest sematic place user spends at least 5 mins
     *
     * @param macaddress String of the macaddress of the particular user
     * @param dateTime String in dd/mm/yyyy hh:mm format for when the report is
     * generated
     * @return ArrayList list of users who are in a specific place 15mins before
     * the specified time at a specific location
     *
     */
    public static String retrieveTimelineForUser(String macaddress, String dateTime) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ArrayList<String> locationTimestampList = new ArrayList<>();
        String currentPlace = ""; //latest place the user spends at least 5 mins
        String spentMoreThan5Minutes = "";

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement to get location name and time given a specfic user and time
            preparedStatement = connection.prepareStatement("select llu.locationname, l.timestamp "
                    + "from locationlookup llu, location l "
                    + "where macaddress = ? and timestamp BETWEEN (SELECT DATE_ADD(? ,INTERVAL 0 MINUTE)) AND (SELECT DATE_ADD(DATE_ADD(? ,INTERVAL 14 MINUTE), INTERVAL 59 SECOND)) "
                    + "and llu.locationid = l.locationid");

            //set the parameters
            preparedStatement.setString(1, macaddress);
            preparedStatement.setString(2, dateTime);
            preparedStatement.setString(3, dateTime);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String locationName = resultSet.getString(1);
                String timestamp = resultSet.getString(2);
                locationTimestampList.add(locationName);
                locationTimestampList.add(timestamp);
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();

            //to get the total accumulated time for the given place
            double currentQuantity = 0.0;

            //arraylist locationTimestampList has locationname and timestamp in alternate order for 1 user only
            //Eg: location1, time1, location2, time2, location3, time3, .. etc
            for (int i = 0; i < locationTimestampList.size(); i += 2) { //loop every location name added
                if (currentPlace == null || currentPlace.length() <= 0) {
                    currentPlace = locationTimestampList.get(i); // to set currentPlace to first location from locationTimestampList at the start
                }
                //prevent arrayindexoutofbounds and to get all the locations before the last location in locationTimestampList
                if ((i + 2) < locationTimestampList.size()) {
                    String currentDate = locationTimestampList.get(i + 1); //to retrieve the corresponding date for currentPlace                    
                    String nextLocation = locationTimestampList.get(i + 2); //to retrieve the next immediate location after currentPlace
                    String nextDate = locationTimestampList.get(i + 3); //to retrieve the date for nextLocation

                    //to convert date and nextDate to Date objects
                    java.util.Date firstDateAdded = df.parse(currentDate);
                    java.util.Date nextDateAdded = df.parse(nextDate);

                    long diff = (nextDateAdded.getTime() - firstDateAdded.getTime()); // to get the time the user stayed at currentPlace in milliseconds
                    double timeDiff = diff / 1000.0; //to get the time the user stayed at currentPlace in seconds
                    currentQuantity += timeDiff; //update the latest time
                    if (!currentPlace.equals(nextLocation)) { //if different location check if time is more than 5 mins
                        if (currentQuantity >= 300) {
                            spentMoreThan5Minutes = currentPlace; //user spent more than 5 minutes at a location
                        }
                        currentPlace = nextLocation; //set the next place as current place
                        currentQuantity = 0; //reset time to re-count the time for nextLocation
                    }
                } else { //reach the end
                    String lastDate = locationTimestampList.get(i + 1); //to retrieve the corresponding date for last location in locationTimestampList

                    java.util.Date lastDateTimeAdded = df.parse(lastDate);
                    java.util.Date endDateTime = df.parse(dateTime);

                    //Calendar object to add 15min to dateTime given from user
                    //Eg: time is 11:00:00; after using Calendar object time is 11:15:00
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(endDateTime); //to use the dateTime given by input as the base
                    cal.add(Calendar.MINUTE, 15);
                    endDateTime = cal.getTime(); //assign the added 15min to endDateTime

                    long diff = (endDateTime.getTime() - lastDateTimeAdded.getTime()); //get the time difference between the last location to the max 15min window
                    double timeDiff = diff / 1000.0; //to get time in seconds

                    // based on wiki, will assume user spend the rest of his/her time there, update the latest time
                    currentQuantity += timeDiff;
                }
            }
            if (currentQuantity >= 300) { //if it is the same place all the way in the users time line
                spentMoreThan5Minutes = currentPlace;
            }
        } catch (SQLException e) {
        } catch (ParseException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return spentMoreThan5Minutes; //return sematic place with more than 5 minutes or if the user does not have any sematic place with more than 5 mins, return empty string
    }

    /**
     * Returns quantity of users of a specific year, school and gender 15mins
     * before the specified time
     *
     * @param timeEnd String in dd/mm/yyyy hh:mm format for when the report is
     * generated
     * @param year String of specific year to search
     * @param gender String of specific gender to search
     * @param school String of specific school to search
     * @return int quantity of users of that particular year, school and gender
     *
     */
    private static int retrieveThreeBreakdown(String timeEnd, String year, String gender, String school) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String ans = "";
        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();
            //prepare a statement
            preparedStatement = connection.prepareStatement("select count(DISTINCT l.macaddress) "
                    + "from location l, demographics d "
                    + "where timestamp between DATE_SUB(?,INTERVAL 15 MINUTE) and DATE_SUB(?,INTERVAL 1 SECOND) "
                    + "and l.macaddress = d.macaddress and gender = ? and email like ? and email like ?");

            //set the parameters
            preparedStatement.setString(1, timeEnd);
            preparedStatement.setString(2, timeEnd);
            preparedStatement.setString(3, gender);
            preparedStatement.setString(4, "%" + year + "%");
            preparedStatement.setString(5, "%" + school + "%");

            //execute SQL query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ans = resultSet.getString(1);
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
        }
        return Integer.parseInt(ans);
    }

    /**
     * Returns a css formated table of all users 15mins before the specified
     * time, broken down by specific category
     *
     * @param endTimeDate String in dd/mm/yyyy hh:mm format for when the report
     * is generated
     * @param text String[] of categories to split the user by
     * @return int quantity of users broken down by specific category
     */
    public static String notVeryBasicBreakdown(String[] text, String endTimeDate) {
        // initialize array
        String[] first, second, third;  //category have name in their first value to know what does first, second or third variable contains
        String[] year = {"year", "2013", "2014", "2015", "2016", "2017"};                              //5
        String[] gender = {"gender", "M", "F"};                                                        //2
        String[] school = {"school", "accountancy", "business", "economics", "law", "sis", "socsc"};   //6

        String userInput = "";

        switch (text[0]) { //get from basicReport.jsp, can be year/gender/school
            case "year":
                first = year; //add year array into first
                userInput += "year "; //user chose year
                break;

            case "gender":
                first = gender; //add gender array into first
                userInput += "gender "; //user chose gender
                break;

            case "school":
                first = school; //add school array into first
                userInput += "school "; //user chose school
                break;

            default:
                first = null;
        }

        switch (text[1]) { //get from basicReport.jsp, can be year/gender/school/optional
            case "year":
                second = year; //add year array into second
                if (userInput.contains("year")) {
                    return null;
                }
                userInput += "year "; //user chose year
                break;

            case "gender":
                second = gender; //add gender array into second
                if (userInput.contains("gender")) {
                    return null;
                }
                userInput += "gender "; //user chose gender
                break;

            case "school":
                second = school; //add school array into second
                if (userInput.contains("school")) {
                    return null;
                }
                userInput += "school "; //user chose school
                break;

            default:
                second = null; //user chose optional
        }

        switch (text[2]) { //get from basicReport.jsp, can be year/gender/school/optional
            case "year":
                third = year; //add year array into third
                if (userInput.contains("year")) {
                    return null;
                }
                userInput += "year "; //user chose year
                break;

            case "gender":
                third = gender; //add gender array into third
                if (userInput.contains("gender")) {
                    return null;
                }
                userInput += "gender "; //user chose gender
                break;

            case "school":
                third = school; //add school array into third
                if (userInput.contains("school")) {
                    return null;
                }
                userInput += "school "; //user chose school
                break;

            default:
                third = null; //user chose optional
        }

        String[] userInputArray = userInput.split(" "); //change from string into string array
        int totalOptions = userInputArray.length; //check how many options has the user selected, can be 1 2 or 3

        int secondL = 1;
        if (second != null) { //if user choose a second option
            secondL = (second.length - 1); //to make sure the array doesn't have ArrayOutOfBoundException
        }
        int thirdL = 1;
        if (third != null) { //if user choose a third option
            thirdL = (third.length - 1); //to make sure the array doesn't have ArrayOutOfBoundException
        }

        //string to return to ReportServlet.java
        String returnThis = "<div class=\"container\">      <table class=\"table table-bordered\">";

        //for the percentage calculation later to compare the number in each category with the total number of possible users
        int totalBetweenTime = everyoneWithinTime(endTimeDate);

        //Print table header
        returnThis += ("<thead><tr><th colspan = " + (totalOptions + 2) + ">Breakdown by: " + userInput + " <br>Total user found: " + totalBetweenTime + "</th></tr>");
        for (String header : userInputArray) { //can be year/gender/school
            returnThis += "<th>" + proper(header) + "</th>";
        }
        returnThis += "<th>Qty</th><th>Percentage</th></thead><tbody>";

        //Arraylist for first
        int r1Count = 0;
        ArrayList<Integer> firstVarSplit = null;
        if (second != null) {
            firstVarSplit = notVeryBasicBreakdownJson(first[0].split(" "), endTimeDate);
        }

        int r2Count = 0;
        ArrayList<Integer> secondVarSplit = null;
        if (second != null) {
            secondVarSplit = notVeryBasicBreakdownJson((first[0] + " " + second[0]).split(" "), endTimeDate);
        }

        int r3Count = 0;
        ArrayList<Integer> thirdVarSplit = notVeryBasicBreakdownJson(userInputArray, endTimeDate);

        for (int i = 1; i <= thirdVarSplit.size(); i++) {
            //Stating of first row
            String currentLine = "<tr>";

            //Text for first col
            if (i % (secondL * thirdL) == totalOptions / 2) {       //checks whether to print this row (happens when there is 3 var)
                //first var to split by
                //if one var trigger = 0
                //if two or three var trigger = 1
                currentLine += "<td rowspan =\""
                        + (secondL * thirdL) + "\">"
                        + proper(first[i / (secondL * thirdL) + totalOptions / 2]);
                if (second != null) {
                    currentLine += "<br>Qty: "
                            + firstVarSplit.get(r1Count) + "<br>"
                            + Math.round(firstVarSplit.get(r1Count++) * 100.0 / totalBetweenTime)
                            + "%</td>";
                }
                currentLine += "</td>";
            }

            int trigger = totalOptions - 2;
            //text for second col
            if (second != null && i % (thirdL) == trigger) {        //checks whether to print this row (happens when there is 2 var)
                //Second var to split by
                //if two var trigger = 0
                //if three var trigger = 1
                currentLine += "<td rowspan =\"" + (thirdL) + "\">"
                        + proper(second[(int) (i / (0.001 + thirdL) % (secondL)) + 1]);
                if (third != null) {
                    currentLine += "<br>Qty: "
                            + secondVarSplit.get(r2Count)
                            + "<br>"
                            + Math.round(secondVarSplit.get(r2Count++) * 100.0 / totalBetweenTime)
                            + "%</td>";
                }
                currentLine += "</td>";
            }

            //text for third col
            if (third != null) {
                //Third var to split by
                currentLine += "<td>"
                        + proper(third[(int) Math.ceil(i % (0.001 + thirdL))])
                        + "</td>";
            }

            //Third var qty
            currentLine += "<td>" + thirdVarSplit.get(r3Count) + "</td>";

            //Third var percentage
            currentLine += "<td>" + Math.round(thirdVarSplit.get(r3Count++) * 100.0 / totalBetweenTime) + "%</td>";

            //Ending
            currentLine += "</tr>";
            returnThis += currentLine;
        }
        returnThis += "</tbody></table></div>";
        return returnThis;
    }

    /**
     * Returns a list of all users 15mins before the specified time, broken down
     * by specific category sorted in eg( 2013 F, 2013 M, 2014 F, 2014 M, 2015
     * F, 2015 M)
     *
     * @param endTimeDate String in dd/mm/yyyy hh:mm format for when the report
     * is generated
     * @param text String[] of categories to split the user by
     * @return ArrayList of quantity of users broken down by specific category
     */
    public static ArrayList<Integer> notVeryBasicBreakdownJson(String[] text, String endTimeDate) {
        // initialize array
        String[] first = null;  //category have name in their first value to know what does first, second or third variable contains
        String[] second = null;  //category have name in their first value to know what does first, second or third variable contains
        String[] third = null;  //category have name in their first value to know what does first, second or third variable contains
        String[] year = {"year", "2013", "2014", "2015", "2016", "2017"};                              //5
        String[] gender = {"gender", "M", "F"};                                                        //2
        String[] school = {"school", "accountancy", "business", "economics", "law", "sis", "socsc"};   //6

        String userInput = "";

        switch (text[0]) { //get from basicReport.jsp, can be year/gender/school
            case "year":
                first = year; //add year array into first
                userInput += "year "; //user chose year
                break;

            case "gender":
                first = gender; //add gender array into first
                userInput += "gender "; //user chose gender
                break;

            case "school":
                first = school; //add school array into first
                userInput += "school "; //user chose school
                break;

            default:
                first = null;
        }

        try {
            switch (text[1]) { //get from basicReport.jsp, can be year/gender/school/optional
                case "year":
                    second = year; //add year array into second
                    userInput += "year "; //user chose year
                    break;

                case "gender":
                    second = gender; //add gender array into second
                    userInput += "gender "; //user chose gender
                    break;

                case "school":
                    second = school; //add school array into second
                    userInput += "school "; //user chose school
                    break;

                default:
                    second = null; //user chose optional
            }

            switch (text[2]) { //get from basicReport.jsp, can be year/gender/school/optional
                case "year":
                    third = year; //add year array into third
                    userInput += "year "; //user chose year
                    break;

                case "gender":
                    third = gender; //add gender array into third
                    userInput += "gender "; //user chose gender
                    break;

                case "school":
                    third = school; //add school array into third
                    userInput += "school "; //user chose school
                    break;

                default:
                    third = null; //user chose optional
            }

        } catch (ArrayIndexOutOfBoundsException e) {

        } catch (NullPointerException e1) {

        }

        String[] userInputArray = userInput.split(" "); //change from string into string array
        int totalOptions = userInputArray.length; //check how many options has the user selected, can be 1 2 or 3

        int firstL = (first.length - 1); //to make sure the array doesn't have ArrayOutOfBoundException
        int secondL = 1;
        int thirdL = 1;
        if (second != null) { //if user chose a second option
            secondL = (second.length - 1); //to make sure the array doesn't have ArrayOutOfBoundException
        }
        if (third != null) { //if user chose a third option
            thirdL = (third.length - 1); //to make sure the array doesn't have ArrayOutOfBoundException
        }

        //Number of rows to print
        int numberOfRows = firstL;
        if (second != null) {
            numberOfRows *= secondL;
        }
        if (third != null) {
            numberOfRows *= thirdL;
        }

        //string to return to ReportServlet.java
        ArrayList<Integer> ans = new ArrayList<>();

        for (int i = 1; i <= numberOfRows; i++) {
            String temp = "";

            //first var to split by
            //if 1 trigger = 0
            //if 2/3 trigger = 1
            if (i % (secondL * thirdL) == totalOptions / 2) {
                temp = first[0]
                        + " "
                        + first[i / (secondL * thirdL) + totalOptions / 2];
            }

            //if 2 trigger = 0
            //if 3 trigger = 1
            int trigger = totalOptions - 2;
            //Second var to split by
            if (second != null && i % (thirdL) == trigger) {
                temp += " "
                        + second[0]
                        + " "
                        + second[(int) (i / (0.001 + thirdL) % (secondL)) + 1];
            }

            if (third != null) {
                temp += " "
                        + third[0]
                        + " "
                        + third[(int) Math.ceil(i % (0.001 + thirdL))];
            }

            //Third var to split by
            //run all the time
            int value = -1;
            switch (totalOptions) {
                case 1: //user only choose 1 option
                    switch (userInput) { //check which option did the user choose
                        case "gender ":
                            value = retrieveByGender(endTimeDate, gender[i]);
                            break;
                        case "school ":
                            value = retrieveByEmail(endTimeDate, school[i]);
                            break;
                        case "year ":
                            value = retrieveByEmail(endTimeDate, year[i]);
                            break;
                        default:
                            value = -2;
                            break;
                    }
                    break;
                case 2: //user only choose 2 options
                    //Checking which variable is not selected
                    int totalSum = 0;
                    if (userInputArray[0].equals("year") && userInputArray[1].equals("gender")) {
                        for (int j = 1; j < school.length; j++) { //sum every school according to that year and gender, magic number is 2 (length of second input eg gender)
                            totalSum += retrieveThreeBreakdown(endTimeDate, year[(int) Math.ceil(i / 2.0)], gender[(i - 1) % 2 + 1], school[j]);
                        }
                    } else if (userInputArray[0].equals("year") && userInputArray[1].equals("school")) {
                        for (int j = 1; j < gender.length; j++) { //sum every gender according to that year and school, magic number is 6 (length of second input eg school)
                            totalSum += retrieveThreeBreakdown(endTimeDate, year[(int) Math.ceil((i / 6.0))], gender[j], school[(i - 1) % 6 + 1]);
                        }
                    } else if (userInputArray[0].equals("school") && userInputArray[1].equals("gender")) {
                        for (int j = 1; j < year.length; j++) { //sum every year according to that school and gender, magic number is 2 (length of second input eg gender)
                            totalSum += retrieveThreeBreakdown(endTimeDate, year[j], gender[(i - 1) % 2 + 1], school[(int) Math.ceil(i / 2.0)]);
                        }
                    } else if (userInputArray[0].equals("school") && userInputArray[1].equals("year")) {
                        for (int j = 1; j < gender.length; j++) { //sum every gender according to that school and year, magic number is 5 (length of second input eg year)
                            totalSum += retrieveThreeBreakdown(endTimeDate, year[(i - 1) % 5 + 1], gender[j], school[(int) Math.ceil(i / 5.0)]);
                        }
                    } else if (userInputArray[0].equals("gender") && userInputArray[1].equals("school")) {
                        for (int j = 1; j < year.length; j++) { //sum every year according to that gender and school, magic number is 6 (length of second input eg school)
                            totalSum += retrieveThreeBreakdown(endTimeDate, year[j], gender[(int) Math.ceil(i / 6.0)], school[(i - 1) % 6 + 1]);
                        }
                    } else if (userInputArray[0].equals("gender") && userInputArray[1].equals("year")) {
                        for (int j = 1; j < school.length; j++) { //sum every school according to that gender and year, magic number is 5 (length of second input eg year)
                            totalSum += retrieveThreeBreakdown(endTimeDate, year[(i - 1) % 5 + 1], gender[(int) Math.ceil(i / 5.0)], school[j]);
                        }
                    }
                    value = totalSum;
                    break;
                default: //user only choose 3 options
                    if (userInputArray[0].equals("year") && userInputArray[1].equals("gender") && userInputArray[2].equals("school")) { //same year 12 times, same gender 6 times, 6 different schools
                        value = retrieveThreeBreakdown(endTimeDate, year[(int) Math.ceil(i / 12.0)], gender[((int) Math.ceil((i - 1) / 6)) % 2 + 1], school[(i - 1) % 6 + 1]);
                    } else if (userInputArray[0].equals("year") && userInputArray[1].equals("school") && userInputArray[2].equals("gender")) { //same year 12 times, same school 2 times, 2 different gender
                        value = retrieveThreeBreakdown(endTimeDate, year[(int) Math.ceil(i / 12.0)], gender[(i - 1) % 2 + 1], school[((int) Math.ceil((i - 1) / 2)) % 6 + 1]);
                    } else if (userInputArray[0].equals("gender") && userInputArray[1].equals("year") && userInputArray[2].equals("school")) { //same gender 30 times, same year 6 times, 6 different school
                        value = retrieveThreeBreakdown(endTimeDate, year[((int) Math.ceil((i - 1) / 6)) % 5 + 1], gender[((int) Math.ceil(i / 30.0))], school[(i - 1) % 6 + 1]);
                    } else if (userInputArray[0].equals("gender") && userInputArray[1].equals("school") && userInputArray[2].equals("year")) { //same gender 30 times, same school 5 times, 5 different year
                        value = retrieveThreeBreakdown(endTimeDate, year[(i - 1) % 5 + 1], gender[(int) Math.ceil(i / 30.0)], school[((int) Math.ceil((i - 1) / 5)) % 6 + 1]);
                    } else if (userInputArray[0].equals("school") && userInputArray[1].equals("year") && userInputArray[2].equals("gender")) { //same school 10 times, same year 2 times, 2 different gender
                        value = retrieveThreeBreakdown(endTimeDate, year[((int) Math.ceil((i - 1) / 2)) % 5 + 1], gender[(i - 1) % 2 + 1], school[(int) Math.ceil(i / 10.0)]);
                    } else if (userInputArray[0].equals("school") && userInputArray[1].equals("gender") && userInputArray[2].equals("year")) { //same school 10 times, same gender 5 times, 5 different year
                        value = retrieveThreeBreakdown(endTimeDate, year[(i - 1) % 5 + 1], gender[((int) Math.ceil((i - 1) / 5)) % 2 + 1], school[(int) Math.ceil(i / 10.0)]);
                    }
                    break;
            }
            ans.add(value);
        }
        return ans;
    }

    /**
     * Returns a map with Key of total time spend with a particular user and
     * Value of users who spend the same amount of time with the particular user
     *
     * @param endTimeDate String in dd/mm/yyyy hh:mm format for when the report
     * is generated
     * @param macaddress of the particular user to search
     * @return Map with Double Key of total time spend with a particular user
     * and String Value of users who spend the same amount of time with the
     * particular user
     */
    public static Map<Double, ArrayList<String>> retrieveTopKCompanions(String endTimeDate, String macaddress) {

        //Retrieve user location timeline 
        ArrayList<String> userLocationsTimestampsList = retrieveUserLocationTimestamps(macaddress, endTimeDate);

        Map<String, Double> companionsMap = new HashMap<>();
        Map<Double, ArrayList<String>> sortedCompanionsMap = new TreeMap<>(Collections.reverseOrder());

        for (int i = 0; i < userLocationsTimestampsList.size(); i++) {
            String userEachLocationTimestamp = userLocationsTimestampsList.get(i); //to retrieve the String of location and timestamps from the list
            String[] userlocationTimestamp = userEachLocationTimestamp.split(",");

            //retrieve each location and time start and end from array
            String userLocationid = userlocationTimestamp[0];
            String userTimeStart = userlocationTimestamp[1];
            String userTimeEnd = userlocationTimestamp[2];

            //retrieve all users within the time query besides the specified user using the timestamp of the user
            //Eg: Specified user is in location A from 10:01 - 10:03 then 10:01 and 10:03 is used to find if there are any companions
            //5 mins before the userTimeStart to get a bigger range to make sure no other users are left out
            ArrayList<String> companionMacaddressList = retrieveCompanionMacaddresses(macaddress, userLocationid, userTimeStart, userTimeEnd);

            //retrieve all the companion's colocated timestamp with user
            //Eg: companionMacaddress, userLocationid, companionTimestamp, colocationTime
            ArrayList<String> companionsLocationTimestampsList = retrieveCompanionLocationTimestamps(companionMacaddressList, userLocationid, userTimeStart, userTimeEnd);

            //to put all the items from companionsLocationTimestampsList into map
            if (companionsLocationTimestampsList != null) {
                for (int j = 0; j < companionsLocationTimestampsList.size(); j++) {
                    String companionEachLocationTimestamp = companionsLocationTimestampsList.get(j);
                    String[] companionLocationTimestamp = companionEachLocationTimestamp.split(",");

                    String companionMacaddress = companionLocationTimestamp[0];
                    double colocationTime = Double.parseDouble(companionLocationTimestamp[3]);

                    if (colocationTime > 0) { //companions should have a colocationTime of more than 0
                        if (companionsMap.containsKey(companionMacaddress)) {
                            double previousTiming = companionsMap.get(companionMacaddress);
                            colocationTime += previousTiming;
                            companionsMap.put(companionMacaddress, colocationTime);
                        } else {
                            companionsMap.put(companionMacaddress, colocationTime);
                        }
                    }
                }
            }
        }

        //to sort map
        Set<String> companionMacaddressKeys = companionsMap.keySet();

        for (String eachCompanionMacaddress : companionMacaddressKeys) {
            double eachCompanionColocationTime = companionsMap.get(eachCompanionMacaddress);
            String eachCompanionEmail = retrieveEmailByMacaddress(eachCompanionMacaddress);

            if (eachCompanionEmail == null || eachCompanionEmail.length() <= 0) { //when no email, display no email found
                eachCompanionEmail = "No email found";
            }

            ArrayList<String> companionsList = sortedCompanionsMap.get(eachCompanionColocationTime); //null or something

            if (companionsList == null || companionsList.size() <= 0) {
                companionsList = new ArrayList<String>();
                companionsList.add(eachCompanionMacaddress + "," + eachCompanionEmail);
                sortedCompanionsMap.put(eachCompanionColocationTime, companionsList);
            } else {
                companionsList.add(eachCompanionMacaddress + "," + eachCompanionEmail);
                sortedCompanionsMap.put(eachCompanionColocationTime, companionsList);
            }
        }
        return sortedCompanionsMap;
    }

    /**
     * Returns all the timestamps generated by the particular user within 15mins
     * of the specified time
     *
     * @param endtimeDate String in dd/mm/yyyy hh:mm format for when the report
     * is generated
     * @param macaddress of the particular user to search
     * @return ArrayList of all the timestamp of the particular user within
     * 15mins of the specified time
     */
    public static ArrayList<String> retrieveUserLocationTimestamps(String macaddress, String endtimeDate) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String userConcatenationList = ""; //locationid, timestamp1, timestamp2
        double duration = 0;

        ArrayList<String> userLocationTimestamps = new ArrayList<String>();

        //to retrieve all locationid and timestamp in alternating order
        //Eg: locationid 1, timestamp 1, locationid 2, timestamp 2, ..
        ArrayList<String> userLocationsList = new ArrayList<String>();

        String timestring = null;
        int firstLocationTimeStartIndex = -1; //to always retrieve the first timestamp of a location
        java.util.Date timeQueriedByUser = null;

        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement
            //retrieve timeline of current user for the past 15min (endtimeDate)
            preparedStatement = connection.prepareStatement("select locationid, timestamp from location where macaddress = ? and timestamp between DATE_SUB(?, INTERVAL 15 MINUTE) and DATE_SUB(?, INTERVAL 0 MINUTE) order by timestamp");

            //set the parameters
            preparedStatement.setString(1, macaddress);
            preparedStatement.setString(2, endtimeDate);
            preparedStatement.setString(3, endtimeDate);

            //execute SQL query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String locationid = resultSet.getString(1);
                timestring = resultSet.getString(2);
                userLocationsList.add(locationid);
                userLocationsList.add(timestring);
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();

            for (int i = 0; i < userLocationsList.size(); i += 2) {
                String locationid = userLocationsList.get(i); //find first location id
                timestring = userLocationsList.get(i + 1); //find first time string

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date timestamp = dateFormat.parse(timestring);//convert time string to Date format - refers to current location timestamp
                java.util.Date timestampEnd = dateFormat.parse(endtimeDate); // refers to queried timestamp
                Calendar cal = Calendar.getInstance();

                timeQueriedByUser = timestampEnd; // assign timestampEnd to timedateEnd to use for calculation

                if (userLocationsList.size() <= i + 2) { //if last pair of location and time is reached
                    duration = (timeQueriedByUser.getTime() - timestamp.getTime()) / (1000.0); //to get the time diff between queried timestamp and last timestamp found

                    if (duration > 300.0) {
                        cal.setTime(timestamp);
                        cal.add(Calendar.MINUTE, 5); //corner case if time diff is more than 5mins
                        //timeDateEnd is 5 minutes after timeDateStart
                        timestampEnd = cal.getTime(); //add 5 mins to current timestamp
                    } else {
                        timestampEnd = timeQueriedByUser; //output the queried timestamp
                    }
                    if (firstLocationTimeStartIndex > -1) {
                        java.util.Date timestampStart = dateFormat.parse(userLocationsList.get(firstLocationTimeStartIndex));
                        timestamp = timestampStart;
                    }
                    userConcatenationList += locationid + "," + dateFormat.format(timestamp) + "," + dateFormat.format(timestampEnd) + ",";
                    userLocationTimestamps.add(userConcatenationList);
                    userConcatenationList = "";
                } else if (userLocationsList.size() > i + 2) { // to prevent the array out of bounds
                    String locationidNext = userLocationsList.get(i + 2); //to get the next corresponding location
                    String timestringNext = userLocationsList.get(i + 3); //to get the next corresponding timestamp
                    java.util.Date timestampNext = dateFormat.parse(timestringNext); //convert the timestamp into Date object

                    if (!locationid.equals(locationidNext)) {
                        //to compare the first timestamp of a different location found
                        if (firstLocationTimeStartIndex > -1) { //to get the first timing for the next location 
                            java.util.Date timestampStart = dateFormat.parse(userLocationsList.get(firstLocationTimeStartIndex));
                            timestamp = timestampStart;
                        }
                        duration += (timestampNext.getTime() - timestamp.getTime()) / (1000.0); //to get the collated time difference between the 2 timestamp updates from user
                        if (duration > 300.0) { //corner case when next time stamp is too far away 
                            cal.setTime(timestamp);
                            cal.add(Calendar.MINUTE, 5); //if time difference more than 5mins just add 5mins to timestamp

                            timestampEnd = cal.getTime();

                            //timestampEnd = timestamp + 5mins
                            userConcatenationList += locationid + "," + dateFormat.format(timestamp) + "," + dateFormat.format(timestampEnd) + ",";

                        } else { //if does not reach 5mins, also update the time difference
                            userConcatenationList += locationid + "," + dateFormat.format(timestamp) + "," + dateFormat.format(timestampNext) + ",";
                        }
                        userLocationTimestamps.add(userConcatenationList);

                        //reset
                        duration = 0;
                        userConcatenationList = "";
                        firstLocationTimeStartIndex = -1;

                    } else if (locationid.equals(locationidNext)) { //if the next update location is same as the previous one
                        if (firstLocationTimeStartIndex == -1) {
                            firstLocationTimeStartIndex = i + 1; //to get the index of the current first location timestamp 
                        }

                        //To get the time diff between current timestamp found and the corresponding next timestamp
                        duration = (timestampNext.getTime() - timestamp.getTime()) / 1000.0;

                        if (duration > 300.0) {
                            cal.setTime(timestamp);
                            cal.add(Calendar.MINUTE, 5); //timeDateEnd is 5 minutes after timeDateStart
                            timestampEnd = cal.getTime();
                            java.util.Date timestampStart = dateFormat.parse(userLocationsList.get(firstLocationTimeStartIndex)); //get the timestamp for a location the first time it is found

                            userConcatenationList += locationidNext + "," + dateFormat.format(timestampStart) + "," + dateFormat.format(timestampEnd) + ",";
                            userLocationTimestamps.add(userConcatenationList);

                            //reset
                            duration = 0;
                            userConcatenationList = "";
                            firstLocationTimeStartIndex = -1;

                        } else if (duration <= 300) {
                            duration += (timestampNext.getTime() - timestamp.getTime()) / 1000.0; //add the time difference between the first found timestamp and the subsequent timestamp after the first
                        }
                    }
                }
            }
        } catch (SQLException e) {
        } catch (ParseException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return userLocationTimestamps;
    }

    /**
     * Returns macaddress of all users (other than the macaddress inputted) who
     * are present at the specified locationid between the input time
     *
     * @param userMacaddress String Macaddress of the user we want to check for
     * companions
     * @param locationid String uniq identifier for the location to lookup for
     * @param timestringStart String in dd/mm/yyyy hh:mm format for where the
     * search begins (inclusive)
     * @param timestringEnd String in dd/mm/yyyy hh:mm format for where the
     * search ends (inclusive)
     * @return
     */
    public static ArrayList<String> retrieveCompanionMacaddresses(String userMacaddress, String locationid, String timestringStart, String timestringEnd) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ArrayList<String> companionsList = new ArrayList<String>();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date timeStart = dateFormat.parse(timestringStart);

            Calendar cal = Calendar.getInstance();
            cal.setTime(timeStart);
            cal.add(Calendar.MINUTE, -5); //get 5 minutes before (inclusive)
            String timestringBeforeStart = dateFormat.format(cal.getTime());

            //get a connection to database
            connection = ConnectionManager.getConnection();

            //prepare a statement
            preparedStatement = connection.prepareStatement("select distinct macaddress from location where macaddress <> ? and locationid= ? and timestamp between ? and ?");

            //set the parameters
            preparedStatement.setString(1, userMacaddress);
            preparedStatement.setString(2, locationid);
            preparedStatement.setString(3, timestringBeforeStart);
            preparedStatement.setString(4, timestringEnd);

            //execute SQL query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                companionsList.add(resultSet.getString(1));
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
        } catch (ParseException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return companionsList;
    }

    /**
     * Retrieve arraylist detailing the the companions macaddress, locationid,
     * timestamp and time spend together in a csv format
     *
     * @param companionsList ArrayList of all the macaddress to lookup time
     * spend together
     * @param userLocationid String uniq identifier for the location to lookup
     * for
     * @param userTimestringStart String in dd/mm/yyyy hh:mm format for where
     * the search begins (inclusive)
     * @param userTimestringEnd String in dd/mm/yyyy hh:mm format for where the
     * search ends (inclusive)
     * @return
     */
    public static ArrayList<String> retrieveCompanionLocationTimestamps(ArrayList<String> companionsList, String userLocationid, String userTimestringStart, String userTimestringEnd) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String companionConcatenationList = ""; //Eg: companionMacaddress, userLocationid, companionTimestamp, colocationTime 

        ArrayList<String> companionLocationTimestamps = new ArrayList<>();

        double colocationTime = 0;
        double companionTimeDiff = 0;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date userTimeStart = dateFormat.parse(userTimestringStart);
            java.util.Date userTimeEnd = dateFormat.parse(userTimestringEnd);

            Calendar cal = Calendar.getInstance();
            cal.setTime(userTimeStart);
            cal.add(Calendar.MINUTE, -5); //get 5 minutes before (inclusive)

            String timestringBeforeStart = dateFormat.format(cal.getTime());
            double userTimeDiff = (userTimeEnd.getTime() - userTimeStart.getTime()) / 1000.0;

            for (int j = 0; j < companionsList.size(); j++) {
                String companionMacaddress = companionsList.get(j);
                colocationTime = 0;
                companionConcatenationList = "";
                boolean correctTimestring = false; //to check if there is a previous start time for the smae location

                //get a connection to database
                connection = ConnectionManager.getConnection();

                //prepare a statement
                preparedStatement = connection.prepareStatement("select timestamp, locationid, TIMESTAMPDIFF(second,timestamp,?) as diff from location where macaddress = ? and timestamp between ? and ? order by timestamp,diff desc");

                //set the parameters
                preparedStatement.setString(1, userTimestringEnd);
                preparedStatement.setString(2, companionMacaddress);
                preparedStatement.setString(3, timestringBeforeStart);
                preparedStatement.setString(4, userTimestringEnd);

                //execute SQL query
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) { //to check if user timestamp is earlier or later than companion timestamp
                    String companionTimestring = resultSet.getString(1);
                    String companionLocationid = resultSet.getString(2);
                    int timeDiffBetweenUserTimeEndAndCompanionTime = resultSet.getInt(3); //the time difference between user's end timestamp and companion's timestamp

                    java.util.Date companionTimestamp = dateFormat.parse(companionTimestring);//convert time string to Date format
                    double timeGapBetweenUserAndCompanion = (timeDiffBetweenUserTimeEndAndCompanionTime - userTimeDiff);

                    //when the result retrieved is the last row of data
                    if (resultSet.isLast()) {
                        if (companionLocationid.equals(userLocationid)) { //if the last location of companion is the same as the location as user
                            if (companionTimestamp.before(userTimeStart)) { //check if the timestamp is earlier than user's timestamp
                                if (timeDiffBetweenUserTimeEndAndCompanionTime > 300) { //if the time difference is more than 5 mins
                                    colocationTime = (300 - timeGapBetweenUserAndCompanion); //Use 5 mins to check if user and companion ar colocated together assuming companion only has 1 update
                                } else {
                                    //find out the time both user and companion spent together
                                    colocationTime = timeDiffBetweenUserTimeEndAndCompanionTime - timeGapBetweenUserAndCompanion;
                                }
                            } else if (!companionTimestamp.before(userTimeStart)) { //companion time is either after or equal to user time
                                if (timeDiffBetweenUserTimeEndAndCompanionTime > 300) { //if colocated time is more than 5 mins
                                    colocationTime += 300; //add 5 mins to colocationTime
                                } else {
                                    colocationTime = timeDiffBetweenUserTimeEndAndCompanionTime; //accept the time difference as coloationTIme
                                }
                            }
                            companionConcatenationList = companionMacaddress + "," + userLocationid + "," + companionTimestamp + "," + colocationTime + ",";
                            companionLocationTimestamps.add(companionConcatenationList);

                            //reset
                            companionConcatenationList = "";
                            colocationTime = 0;
                            correctTimestring = false;
                        }
                    }

                    //more than 1 location update for companion
                    while (resultSet.next()) {
                        String companionNextTimestring = resultSet.getString(1);
                        String companionNextLocationid = resultSet.getString(2);
                        int nextTimeDiffBetweenUserTimeEndAndCompanionTime = resultSet.getInt(3);

                        java.util.Date companionNextTimestamp = dateFormat.parse(companionNextTimestring);//convert time string to Date format
                        timeGapBetweenUserAndCompanion = (nextTimeDiffBetweenUserTimeEndAndCompanionTime - userTimeDiff);

                        if (companionLocationid.equals(userLocationid) || correctTimestring) { //check if the previous location is correct or current location is correct
                            if (companionTimestamp.before(userTimeStart)) { //if companion timestamp is before user timestamp
                                if (companionNextTimestamp.after(userTimeStart) && companionLocationid.equals(userLocationid)) { //if timestamp is the last one before time start and location is correct
                                    if (companionLocationid.equals(companionNextLocationid)) { //if current and next location is the same
                                        companionTimeDiff = timeDiffBetweenUserTimeEndAndCompanionTime - nextTimeDiffBetweenUserTimeEndAndCompanionTime;
                                        if (companionTimeDiff > 300) {
                                            colocationTime = (300 - timeGapBetweenUserAndCompanion);
                                        } else {
                                            colocationTime = userTimeDiff - nextTimeDiffBetweenUserTimeEndAndCompanionTime;
                                        }
                                        //CompanionLocationTimestamps.add(macaddress + "same location time before start " + "," + colocationTime + "," + tmp + "," + duration + "," + gap);
                                        correctTimestring = true;
                                    } else if (!companionLocationid.equals(companionNextLocationid)) {
                                        companionTimeDiff = timeDiffBetweenUserTimeEndAndCompanionTime - nextTimeDiffBetweenUserTimeEndAndCompanionTime;
                                        if (companionTimeDiff > 300) {
                                            colocationTime = (300 - timeGapBetweenUserAndCompanion);
                                        } else {
                                            colocationTime = userTimeDiff - nextTimeDiffBetweenUserTimeEndAndCompanionTime;
                                        }
                                        //CompanionLocationTimestamps.add(macaddress + "diff location time before start " + colocationTime + "," + tmp);
                                        companionConcatenationList = companionMacaddress + "," + userLocationid + "," + companionNextTimestamp + "," + colocationTime + ",";
                                        companionLocationTimestamps.add(companionConcatenationList);

                                        //reset
                                        companionConcatenationList = "";
                                        colocationTime = 0;
                                        correctTimestring = false;
                                    }
                                }

                            } else if (!companionTimestamp.before(userTimeStart)) { //if companion timestamp is equal or after user timestamp
                                if (companionLocationid.equals(companionNextLocationid) && companionLocationid.equals(userLocationid)) { //if current and next location is the same
                                    companionTimeDiff = timeDiffBetweenUserTimeEndAndCompanionTime - nextTimeDiffBetweenUserTimeEndAndCompanionTime;
                                    if (companionTimeDiff > 300) {
                                        colocationTime += 300;
                                    } else {
                                        colocationTime += companionTimeDiff; //take whatever time the colocated time is
                                    }
                                    correctTimestring = true;
                                } else if (!companionLocationid.equals(companionNextLocationid) && companionLocationid.equals(userLocationid)) { //if current location is different from next one and is correct location
                                    companionTimeDiff = timeDiffBetweenUserTimeEndAndCompanionTime - nextTimeDiffBetweenUserTimeEndAndCompanionTime;
                                    if (companionTimeDiff > 300) {
                                        colocationTime += 300;
                                    } else {
                                        colocationTime += companionTimeDiff;
                                    }
                                    companionConcatenationList = companionMacaddress + "," + userLocationid + "," + companionNextTimestamp + "," + colocationTime + ",";
                                    companionLocationTimestamps.add(companionConcatenationList);

                                    //reset because a next location is found
                                    companionConcatenationList = "";
                                    colocationTime = 0;
                                    correctTimestring = false;
                                    //if previous location is correct and is not correct location
                                }
                            }
                        }
                        //check last location update
                        if (resultSet.isLast() && companionNextLocationid.equals(userLocationid)) {

                            //if last location same, include last timestamp
                            if (companionNextTimestamp.before(userTimeStart)) {
                                if (nextTimeDiffBetweenUserTimeEndAndCompanionTime > 300) {
                                    colocationTime += (300 - timeGapBetweenUserAndCompanion);
                                } else {
                                    colocationTime += nextTimeDiffBetweenUserTimeEndAndCompanionTime - timeGapBetweenUserAndCompanion;
                                }
                                //if last location is correct location and not before start timestamp  
                            } else {
                                if (nextTimeDiffBetweenUserTimeEndAndCompanionTime > 300) {
                                    colocationTime += 300;
                                } else {
                                    colocationTime += nextTimeDiffBetweenUserTimeEndAndCompanionTime;
                                }

                            }
                            companionConcatenationList = companionMacaddress + "," + userLocationid + "," + companionNextTimestamp + "," + colocationTime + ",";
                            companionLocationTimestamps.add(companionConcatenationList);

                            //reset
                            companionConcatenationList = "";
                            colocationTime = 0;
                            correctTimestring = false;
                        }
                        companionTimestring = companionNextTimestring;
                        companionLocationid = companionNextLocationid;
                        timeDiffBetweenUserTimeEndAndCompanionTime = nextTimeDiffBetweenUserTimeEndAndCompanionTime;
                        companionTimestamp = dateFormat.parse(companionTimestring);
                    }
                }
                //close connections
                resultSet.close();
                preparedStatement.close();
                connection.close();
            }
        } catch (SQLException e) {
        } catch (ParseException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return companionLocationTimestamps;
    }

    /**
     * Return particular user's email by macaddress
     *
     * @param macaddress of the particular user to search
     * @return String email address of the particular macaddress or null if not
     * found
     *
     */
    public static String retrieveEmailByMacaddress(String macaddress) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String ans = "";
        try {
            //get a connection to database
            connection = ConnectionManager.getConnection();
            //prepare a statement
            preparedStatement = connection.prepareStatement("SELECT email from demographics WHERE macaddress = ?");

            //set the parameters
            preparedStatement.setString(1, macaddress);

            //execute SQL query
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ans = resultSet.getString(1);
            }

            //close connections
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
        }
        return ans;
    }

    /**
     * Returns the proper capitalized string
     *
     * @param line String to properly capitalize
     * @return String of proper capitalized string
     */
    private static String proper(String line) {
        if (line == null) {
            return null;
        }
        if (line.length() == 1) {
            return line.toUpperCase();
        }
        return line.substring(0, 1).toUpperCase() + line.substring(1);
    }
}

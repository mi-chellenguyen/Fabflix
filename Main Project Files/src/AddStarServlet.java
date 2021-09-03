import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Servlet implementation class AddStarServlet
 */
@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String starName = request.getParameter("star-name");
		String birthYear = request.getParameter("birth-year");
		birthYear =  ((birthYear == null) ? "" : birthYear); 

		 // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        
        try {
        	 // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            
            // QUERY TO GET MAX STARS ID 
            String getMaxStarIdQuery = "SELECT max(stars.id)\r\n" + 
            						   "FROM stars;";

        	// QUERY TO INSERT INTO DB
            String insertStarWithBirthYearQuery = "INSERT INTO stars(id, name, birthYear)\r\n" +
            						 			  "VALUES(?, ?, ?)";
            
            String insertStarNoBirtYearQuery = "INSERT INTO stars(id, name)\r\n" +
					 				 		   "VALUES(?, ?)";
            
            
            
            // GET LARGEST ID & PARSE 
            PreparedStatement getMaxStarIdStatement = dbcon.prepareStatement(getMaxStarIdQuery);
            ResultSet maxIdResults = getMaxStarIdStatement.executeQuery();

            String maxStarId = "";
            if (maxIdResults.next())
            {
                maxStarId = maxIdResults.getString(1);
            }
            
            getMaxStarIdStatement.close();
            maxIdResults.close();
            

            Integer idNum = Integer.valueOf(maxStarId.substring(2));
            idNum++; 
            
			JsonArray jsonArray = new JsonArray();
            PreparedStatement insertStarStatement;
            if (birthYear != "") {	// Birth year is included
                insertStarStatement = dbcon.prepareStatement(insertStarWithBirthYearQuery);  
            	String uniqueStarId = "nm" + Integer.toString(idNum);
                insertStarStatement.setString(1, uniqueStarId);
                insertStarStatement.setString(2, starName);
                insertStarStatement.setInt(3, Integer.valueOf(birthYear));
                
    			jsonArray.add(uniqueStarId);
    			jsonArray.add(starName);
    			jsonArray.add(birthYear);

            }
            else {	// No birth year included
            	insertStarStatement = dbcon.prepareStatement(insertStarNoBirtYearQuery); 
            	String uniqueStarId = "nm" + Integer.toString(idNum);
                insertStarStatement.setString(1, uniqueStarId);
            	insertStarStatement.setString(2, starName);
            	
            	jsonArray.add(uniqueStarId);
    			jsonArray.add(starName);
            }
          	insertStarStatement.executeUpdate();
          				
            out.write(jsonArray.toString());
            
            response.setStatus(200);
          	insertStarStatement.close();
            dbcon.close();
        }
        
        catch (Exception e){
        	// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set response status to 500 (Internal Server Error)
			response.setStatus(500);
        }
        out.close();	
	}
}

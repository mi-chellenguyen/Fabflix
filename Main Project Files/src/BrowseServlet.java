import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
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
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Servlet implementation class BrowseServlet
 */
@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();

        try 
        {
            Connection dbcon = dataSource.getConnection();

        	 // Selects all genres
            String allGenresQuery = "SELECT G.name\r\n" + 
    			            		"FROM genres G;";
            
            Statement allGenresStatement = dbcon.createStatement();
            ResultSet allGenresResults = allGenresStatement.executeQuery(allGenresQuery);

            JsonArray jsonArray = new JsonArray();
            while (allGenresResults.next()) {
            	String genre_name = allGenresResults.getString("name");
            	jsonArray.add(genre_name);
            }
            
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            
            allGenresResults.close();
            allGenresStatement.close();
            dbcon.close();
        }
        catch (Exception e) {
        	
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

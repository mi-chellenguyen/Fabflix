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
import java.util.HashMap;
import java.util.Map.Entry;
import java.time.LocalDate;
import java.sql.Date;

/**
 * Servlet implementation class ConfirmationServlet
 */
@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	PrintWriter out = response.getWriter();
     	
     	try {
     		// Get a connection from dataSource
     		Connection dbcon = dataSource.getConnection();     		
     		
     		String getCustomerIdQuery = "SELECT C.id\r\n" + 
     								  	   "FROM customers C, sales S\r\n" + 
     								  	   "WHERE ? = C.email;";
     		
     		String getMovieInfoQuery = "SELECT M.title\r\n" + 
		    			   			   "FROM movies M\r\n" + 
		    			   			   "WHERE M.id = ?;";
     		
     		String updateSalesQuery = "INSERT INTO sales(customerId, movieId, salesDate)\n" + 
     								  "VALUES(?, ?, ?);";
		    
     		String getLastSalesIdQuery = "SELECT S.id\r\n" + 
					     				 "FROM sales S\r\n" + 
					     				 "ORDER BY S.id DESC\r\n" + 
					     				 "LIMIT 1;";
     		
     		PreparedStatement customerEmailStatement = dbcon.prepareStatement(getCustomerIdQuery); 
     		
			JsonArray jsonArray = new JsonArray();
     		if (request.getSession(false) != null) {
     			
     			// GET THE CUSTOMER'S ID 
     			String userEmail = ((User)request.getSession().getAttribute("user")).getEmail();
     			int customerId = 0;
				HashMap<String, Integer> cart = ((User)request.getSession().getAttribute("user")).getCart();
				 
				customerEmailStatement.setString(1, userEmail);
				ResultSet customerEmailResults = customerEmailStatement.executeQuery();

				if (customerEmailResults.next())
				{
					customerId = customerEmailResults.getInt("id");
				}
	     		customerEmailResults.close();

				// FOR EACH ITEM IN CART, GET MOVIE INFO, INSERT MOVIE INTO SALES DB, ADD TO JSON ARRAY FOR .HTML PROCESSING
				// Query to get movie info (title)
	     		
				// For each movie that was put into the cart, insert it into the DB sales table
				for (Entry<String, Integer> pair : cart.entrySet()) {
					
					// Get salesId(s) for the just added sale(s)
					Integer salesId = 0;
					
		            Statement getLastSalesIdStatement = dbcon.createStatement();
		            ResultSet lastSalesIdResult = getLastSalesIdStatement.executeQuery(getLastSalesIdQuery);
		     		if (lastSalesIdResult.next())
		     		{
		     			salesId = lastSalesIdResult.getInt("id");
		     		}
		     		getLastSalesIdStatement.close();
		     		lastSalesIdResult.close();
		     		
					// Get movie info (title) to show on .html
					PreparedStatement getMovieInfoStatement = dbcon.prepareStatement(getMovieInfoQuery);
					getMovieInfoStatement.setString(1, pair.getKey());
					
		            ResultSet movieInfoResults = getMovieInfoStatement.executeQuery();
					
		            String movieTitle = "";
		            String movieId = "";
	                if (movieInfoResults.next()) {
			            // Obtained title for movie
						movieTitle = movieInfoResults.getString("title");
						movieId = pair.getKey();
	                }
	                getMovieInfoStatement.close();
					movieInfoResults.close();
					
	                // Execute queries (based on quantity) to insert sale into DB
					Date sqlDate = Date.valueOf( LocalDate.now().toString() ); // (today's) date the sale is made
		     		PreparedStatement updateSalesStatement = dbcon.prepareStatement(updateSalesQuery);
					for (int count = 0; count < pair.getValue(); count++)
					{
						updateSalesStatement.setInt(1, customerId);
						updateSalesStatement.setString(2, movieId);
						updateSalesStatement.setDate(3, sqlDate);
						updateSalesStatement.executeUpdate();
						salesId++;
						
						JsonObject jsonObject = new JsonObject();
		                jsonObject.addProperty("salesId", salesId);
		                jsonObject.addProperty("movieId", movieId);
		                jsonObject.addProperty("title", movieTitle);
		                jsonObject.addProperty("quantity", 1);
		                jsonArray.add(jsonObject);
					}
					updateSalesStatement.close();
					
					// For processing in confirmation.html, add sales + movie info into JsonArray
					// Since multiple quantities/sales for the same movie, iterate through all salesIds
		            
	            	
				}
				
				// CLEAR THE CART 
				((User)request.getSession().getAttribute("user")).emptyCart();
     		}
     		// write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
     		
     		customerEmailStatement.close();
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
	}

}

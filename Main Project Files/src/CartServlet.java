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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String task = request.getParameter("task"); //Task can be either to update, remove, or view cart
        String movieId = request.getParameter("movie_id");
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        // Output stream to STDOUT
     	PrintWriter out = response.getWriter();
        try {
        	
        	// Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            
            String query = "SELECT M.title\r\n" + 
            				"FROM movies M\r\n" + 
            				"WHERE M.id = ?;";
            
            PreparedStatement statement = dbcon.prepareStatement(query); 
            
        	//String sessionId = ((HttpServletRequest) request).getSession().getId();
            //Long lastAccessTime = ((HttpServletRequest) request).getSession().getLastAccessedTime();
			 if (request.getSession(false) != null) { 
					 
					 if(task.equals("add")) {
					 // Add to cart
						 ((User)request.getSession().getAttribute("user")).addToCart(movieId, quantity);
					 }
					 else if(task.equals("update")) {
					 // Update cart
						 ((User)request.getSession().getAttribute("user")).updateCart(movieId, quantity);
					 }
						 
					 else if (task.equals("remove")) {
					 // Delete from cart
						 ((User)request.getSession().getAttribute("user")).removeFromCart(movieId);
					 }
					 
					 HashMap<String, Integer> cart = ((User)request.getSession().getAttribute("user")).getCart();
					 
					 JsonArray jsonArray = new JsonArray();
		      
					 for (Entry<String, Integer> pair : cart.entrySet()) {
		            	statement.setString(1, pair.getKey());
		                
		                ResultSet results = statement.executeQuery();
		                
		                if(results.next()) {
							 String movieTitle = results.getString("title");
		                
			                JsonObject jsonObject = new JsonObject();
			                jsonObject.addProperty("movieId", pair.getKey());
			                jsonObject.addProperty("title", movieTitle);
			                jsonObject.addProperty("quantity", pair.getValue());
			                jsonArray.add(jsonObject);
		                }
		            }
		            
		            response.getWriter().write(jsonArray.toString());  
		     } else {
	            JsonObject responseJsonObject = new JsonObject();
	            responseJsonObject.addProperty("status", "fail");
	            response.getWriter().write(responseJsonObject.toString());
	        }
			
            response.setStatus(200);
			
		} catch (Exception e) {
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

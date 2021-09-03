import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.omg.CORBA.Request;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;


/**
 * This class is declared as CheckoutServlet in web annotation, 
 * which is mapped to the URL pattern /api/checkout
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    //Create a data source
    @Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String cardNumber = request.getParameter("card-number");
        String firstName = request.getParameter("first-name");
        String lastName = request.getParameter("last-name");
        int expYear = Integer.parseInt(request.getParameter("exp-year"));
        int expMonth = Integer.parseInt(request.getParameter("exp-month"));
        int expDay = Integer.parseInt(request.getParameter("exp-day"));
        
        LocalDate expDate = LocalDate.of(expYear, expMonth, expDay);
        
        // Output stream to STDOUT
     	PrintWriter out = response.getWriter();
     		
        try {
			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();
			
			////////////////// Queries ///////////////////
			String verifyCreditCardQuery = "SELECT *\n" + 
									  "FROM creditcards\n" + 
					                  "WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?;";
            
            
			// Declare our statement
			PreparedStatement verifyCreditCardStatement = dbcon.prepareStatement(verifyCreditCardQuery);

			// Set the parameter represented by "?" in the query to the id we get from url
			verifyCreditCardStatement.setString(1, cardNumber);
			verifyCreditCardStatement.setString(2, firstName);
			verifyCreditCardStatement.setString(3, lastName);
			verifyCreditCardStatement.setString(4, expDate.toString());
			
			// Perform the query
			ResultSet verifyCreditCardResults = verifyCreditCardStatement.executeQuery();

			String resultCardNum= "";
			
			if(verifyCreditCardResults.next()) {
				resultCardNum = verifyCreditCardResults.getString("id");
			}
			
			 if (resultCardNum.equals(cardNumber)) {
		            // Credit card validation successful
		            JsonObject responseJsonObject = new JsonObject();
		            responseJsonObject.addProperty("status", "success");
		            responseJsonObject.addProperty("message", "success");

		            response.getWriter().write(responseJsonObject.toString());
		            
	        } else {
	            // Credit card validation fails
	            JsonObject responseJsonObject = new JsonObject();
	            responseJsonObject.addProperty("status", "fail");
	            responseJsonObject.addProperty("message", "Incorrect card information. Please try again!");
	            response.getWriter().write(responseJsonObject.toString());
	        }
			
            response.setStatus(200);
			verifyCreditCardResults.close();
			verifyCreditCardStatement.close();
			dbcon.close();
			
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

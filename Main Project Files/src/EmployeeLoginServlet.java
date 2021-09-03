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
import org.jasypt.util.password.StrongPasswordEncryptor;

/**
 * Servlet implementation class EmployeeLoginServlet
 */
@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/api/employee-login")
public class EmployeeLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	 //Create a data source
    @Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Output stream to STDOUT
				
     	PrintWriter out = response.getWriter();
     	
    	String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
    	System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
    	

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        	JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            if (gRecaptchaResponse.isEmpty()) {
            	responseJsonObject.addProperty("message", "Please check the reCAPCHTA box");
            }
            else {
            	responseJsonObject.addProperty("message", "An error occured with reCAPTCHA, please check eclipse console");
            }
			 response.getWriter().write(responseJsonObject.toString());
            out.close();
            return;
        }
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        response.setContentType("text/html"); // Response mime type
     		
        try {
			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();
			
			////////////////// Queries ///////////////////
			String verifyLoginQuery = "SELECT email, password\n" + 
									  "FROM employees\n" + 
					                  "WHERE email = ?;";
            
            
			// Declare our statement
			PreparedStatement verifyLoginStatement = dbcon.prepareStatement(verifyLoginQuery);

			// Set the parameter represented by "?" in the query to the id we get from url
			verifyLoginStatement.setString(1, email);
			
			// Perform the query
			ResultSet verifyLoginResults = verifyLoginStatement.executeQuery();
			
			String resultEmail = "";
			String resultPassword = "";
			boolean success = false;
			System.out.println("result set: " + verifyLoginResults);
			if(verifyLoginResults.next()) {
				resultEmail = verifyLoginResults.getString("email");
				resultPassword = verifyLoginResults.getString("password");
				success = new StrongPasswordEncryptor().checkPassword(password, resultPassword);
			}
			
			if (email.equals(resultEmail) && success) {
	            // Login succeeds
	            // Set this user into current session
	            String sessionId = ((HttpServletRequest) request).getSession().getId();
	            Long lastAccessTime = ((HttpServletRequest) request).getSession().getLastAccessedTime();
	            request.getSession().setAttribute("user", new User(email));
	            ((User)request.getSession().getAttribute("user")).setRole("employee");
	            
	            JsonObject responseJsonObject = new JsonObject();
	            responseJsonObject.addProperty("status", "success");
	            responseJsonObject.addProperty("message", "success");

	            response.getWriter().write(responseJsonObject.toString());
	            
	        } else {
	            // Login fails
	            JsonObject responseJsonObject = new JsonObject();
	            responseJsonObject.addProperty("status", "fail");
	            if (!email.equals(resultEmail)) {
	                responseJsonObject.addProperty("message", "Your email: " + email + " is not associated with a Fabflix employee account.");
	            } else {
	                responseJsonObject.addProperty("message", "Incorrect password.");
	            }
	            response.getWriter().write(responseJsonObject.toString());
	        }
			
            response.setStatus(200);
			verifyLoginResults.close();
			verifyLoginStatement.close();
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



import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class EmployeeLoginFilter
 */
// if a user tries to access any of the employee-only pages, check if an employee is logged in, otherwise, redirect to employee-login
@WebFilter(filterName = "/EmployeeLoginFilter", urlPatterns = {"/_dashboard", "/add-star", "/add-movie", "/db-metadata"})
public class EmployeeLoginFilter implements Filter {
	 /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if the URL is allowed to be accessed without log in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // If no one is logged in or if the user that is logged in is not an employee, redirect to employee-login
        if (httpRequest.getSession().getAttribute("user") == null 
        	|| !((User)httpRequest.getSession().getAttribute("user")).getRole().equals("employee")) 
        {
            	httpResponse.sendRedirect("employee-login.html");
        }
        else 
        {
        	// If the user exists in current session and is an employee, redirect the user to the corresponding URL
        	chain.doFilter(request, response);
        }
    }
    
    // Setup your own rules here to allow accessing some resources without logged in
    // Always allow your own login related requests (html, js, servlet, etc..)
    // You might also want to allow some CSS files, etc..
    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        requestURI = requestURI.toLowerCase();
        
        return requestURI.endsWith("employee-login.html")
        	 || requestURI.endsWith("employee-login.js")
        	 || requestURI.endsWith("api/employee-login")
			 || requestURI.endsWith("css/style.css")
			 || requestURI.endsWith("images/background.jpg")
			 || requestURI.endsWith("images/fabflix-logo.svg");
    }

    /**
     * This class implements the interface: Filter. In Java, a class that implements an interface
     * must implemented all the methods declared in the interface. Therefore, we include the methods
    * below.
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) {
    }

    public void destroy() {
    }
}

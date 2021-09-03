/**
 * Handle the data returned by EmployeeLoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to _dashboard.html
    if (resultDataJson["status"] === "success") {
    	console.log("login success");
        window.location.replace("_dashboard.html");
    } else {
    	grecaptcha.reset();
        // If login fails, the web page will display 
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.post(
        "api/employee-login",
        // Serialize the login form to the data sent by POST request
        $("#employee_login_form").serialize(),
        (resultDataString) => handleLoginResult(resultDataString)
    );
}

// Bind the submit action of the form to a handler function
$("#employee_login_form").submit((event) => submitLoginForm(event));

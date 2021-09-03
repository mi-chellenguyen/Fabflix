/**
 * Handle the data returned by CheckoutServlet
 * @param resultDataString jsonObject
 */
function handleCheckoutResult(resultDataString) {
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle checkout response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If checkout succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
    	console.log("checkout success");
        window.location.replace("confirmation.html");
    } else {
        // If checkout fails, the web page will display 
        // error messages on <div> with id "submit_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#submit_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitCheckoutForm(formSubmitEvent) {
    console.log("submit checkout form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.post(
        "api/checkout",
        // Serialize the login form to the data sent by POST request
        $("#checkout_form").serialize(),
        (resultDataString) => handleCheckoutResult(resultDataString)
    );
}

// Bind the submit action of the form to a handler function
$("#checkout_form").submit((event) => submitCheckoutForm(event));
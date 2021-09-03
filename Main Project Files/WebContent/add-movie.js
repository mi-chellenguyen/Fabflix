function handleAddMovieResult(resultData) {
	resultDataJson = JSON.parse(resultData);
	
    console.log("handle add movie response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
    	console.log("add movie success");
        window.location.replace("add-confirmation.html");
    } 
    else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#add_movie_error_message").text(resultDataJson["message"]);
    }  
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitAddMovieForm(formSubmitEvent) {
    console.log("submit add movie form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.post(
        "api/add-movie",
        // Serialize the add movie form to the data sent by POST request
        $("#add-movie-form").serialize(),
        (resultDataString) => handleAddMovieResult(resultDataString)
    );
}

// Bind the submit action of the form to a handler function
$("#add-movie-form").submit((event) => submitAddMovieForm(event));


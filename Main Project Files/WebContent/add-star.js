function handleStarResult(resultData) {
    console.log("handleStarResult");
    window.location.replace("add-confirmation.html");
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitAddStarForm(formSubmitEvent) {
    console.log("submit add star form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.post(
        "api/add-star",
        // Serialize the add star form to the data sent by POST request
        $("#add-star-form").serialize(),
        (resultDataString) => handleStarResult(resultDataString)
    );
}

// Bind the submit action of the form to a handler function
$("#add-star-form").submit((event) => submitAddStarForm(event));


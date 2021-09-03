function handleConfirmationResult(resultData) {
    console.log("handleConfirmationResult: populating confirmation table from resultData");

    // Populate the confirmation table
    // Find the empty table body by id "movie_table_body"
    let confirmationTableBodyElement = jQuery("#confirmation_table_body");

    for (let i = 0; i < resultData.length; i++) {
    	let itemHTML = "";
        itemHTML += '<tr>' +
	        			'<td>' + resultData[i]["salesId"] + '</td>' +
	                    '<td>' + resultData[i]["movieId"] + '</td>' +
	                    '<td>' + resultData[i]["title"] + '</td>' +
	            		'<td>' + resultData[i]["quantity"] +'</td>' + 
		            '</tr>';
	    confirmationTableBodyElement.append(itemHTML);
    }
}

//Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/confirmation", // Setting request url
    success: (resultData) => handleConfirmationResult(resultData)
});
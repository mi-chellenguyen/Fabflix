function handleDbResult(resultData) {
    console.log("handleDbResult: populating db table from resultData");

    console.log(resultData);
    // Populate the db table
    // Find the empty table body by id "db_table_body"
    let dbTableBodyElement = jQuery("#db_table_body");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {

    	let itemHTML = "";
        itemHTML += '<div class="card text-left text-dark border-danger mb-3" style="width: 18rem; display: inline-block; margin:15px;">' +
                        '<div class="card-header" style="display: flex;"><b>' + resultData[i]["table_name"] + '</b></div>' +
                        '<ul class="text-left list-group list-group-flush">' +
                            '<li class="list-group-item">' + resultData[i]["columns"][0];
        
						        for (let column = 1, l = resultData[i]["columns"].length; column < l; column++) {
						            itemHTML += '<br>' + resultData[i]["columns"][column]; 
						         }
	
		    itemHTML +=     '</li>' +
		                '</ul>' +
		            '</div>';
		dbTableBodyElement.append(itemHTML);
    }
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/db-metadata", 
    success: (resultData) => handleDbResult(resultData)
});
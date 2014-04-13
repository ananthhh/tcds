$(function(){
	loadSheets();
});

function format(item) { return item.name; }

function loadSheets(){
	reset(1);
	$("#recipient").hide();
	$.getJSON("sheets",function(data1){
		reset(2);
		$("#sheetDropdown").select2({
			placeholder: 'Select sheet',
			data:{ results: data1, text: 'name' },
		    formatSelection: format,
		    formatResult: format 
		});
	}).fail(function( ) {
		   resetNotification("alert-danger","Request Failed: Please try again later" );
	  });
}
function loadAttachments(){
	
	var data = $("#sheetDropdown").select2("data");
	if (data == null) {
		alert("Pleaae select sheet to proceed..");
		return;
	}
	reset(3);
	$.getJSON("attachments/"+data.id,function(data1){
		console.log(data1.size);
		if(data1 == undefined || data1 == null || data1.length == 0){
			alert("Sheet you have selected has no attachments in it. Please select other sheet.");
			reset(2);
			return;
		}
		reset(4);
		$("#attachmentDropdown").select2({
			placeholder: 'Select attachments',
			data:{ results: data1, text: 'name' },
		    formatSelection: format,
		    formatResult: format 
		});
		setSelection("panelSheet",data.name, data.id);
	}).fail(function(jqxhr, textStatus, error ) {
		var err = textStatus + ", " + error;
	    resetNotification("alert-danger","Request Failed: " + err);
	  });
}

function postAttachment(){
	var data = $("#attachmentDropdown").select2("data");
	var dataSheet = $("#sheetDropdown").select2("data");
	$.post( "attachments", { id: data.id, name: data.name, 
		parent_id: data.parentId, sheet_name: dataSheet.name} ).fail(function(jqxhr, textStatus, error ) {
			resetNotification("alert-danger","Request Failed: Please try again later" );
		  });
	setSelection("panelAttachment",data.name, data.id);
}
function postRecipients(){
	
	var names=$("#recipientName").val().split(",");
	var emails=$("#recipientEmail").val().split(",");
	var jsonObj=[];
	var item = {};
	var selection="";
	if(names[0].length == 0 && emails[0].length > 0){
		alert("No. of recipient email and recipient name should be equal. Please verify");
		return;
	}
	if(names.length != emails.length){
		alert("No. of recipient email and recipient name should be equal. Please verify");
		return;
	}
	console.log(emails[0].length);
	if(emails.length > 0){
		if(emails[0].length != 0 ){
			$.each(emails, function( index, value ) {
				var name = names[index];
		        var email = value;
		
		        item = {}
		        item ["name"] = name;
		        item ["email"] = email;
		        jsonObj.push(item);
		        selection+="<p>"+name+" , "+email+"</p>";
		        
			});
		
			$.ajax({
			    type :  "POST",
			    contentType: "application/json; charset=utf-8",
			    data: JSON.stringify(jsonObj),
			    url  :  "/recipients",
			        success: function(data){
			            console.log(data);
			            setSelection("panelReipients",selection,"");
			        }
			});
		}
	}
	

}
function loadSend(){
	reset(7);
	
//	$.get( "create" );
}

function loadRecipients(){
	reset(5);
	$('#recipientName').selectize({
	    delimiter: ',',
	    persist: false,
	    create: function(input) {
	        return {
	            value: input,
	            text: input
	        }
	    }
	});
	$.getJSON("recipients",function(data1){
		var REGEX_EMAIL = '[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})';
		reset(6);
		console.log(data1);
		$("#recipientEmail").selectize({
			maxItems: null,
		    valueField: 'email',
		    labelField: 'email',
		    searchField: 'email',
		    options: data1,
		    create: function(input) {
		        if ((new RegExp('^' + REGEX_EMAIL + '$', 'i')).test(input)) {
		            return {email: input};
		        }
		        var match = input.match(new RegExp('^([^<]*)\<' + REGEX_EMAIL + '\>$', 'i'));
		        if (match) {
		            return {
		                email : match[2],
		                name  : $.trim(match[1])
		            };
		        }
		        alert('Invalid email address.');
		        return false;
		    }
		});
	}).fail(function(jqxhr, textStatus, error ) {
		resetNotification("alert-danger","Request Failed: Please try again later" );
	  });
}

function reset(mode){
	switch(mode){
	case 1:
		$("#sheet").hide();
		$("#attachment").hide();
		$("#recipient").hide();
		$("#tagsend").hide();
		resetNotification("alert-info", "<strong>Loading sheets...</strong>");
		resetTab("tabSheet");
		break;
	case 2:
		$("#sheet").show();
		$("#attachment").hide();
		$("#recipient").hide();
		$("#tagsend").hide();
		resetNotification("alert-success", "<strong>Sheets loaded successfully!!. </strong>" +
				"Please select appropriate sheet and click next");

		resetTab("tabSheet");
		break;
	case 3:
		$("#sheet").hide();
		$("#attachment").hide();
		$("#recipient").hide();
		$("#tagsend").hide();
		resetNotification("alert-info", "<strong>Loading attachments...</strong>");

		resetTab("tabAttachment");
		break;
	case 4:
		$("#sheet").hide();
		$("#attachment").show();
		$("#recipient").hide();
		$("#tagsend").hide();
		resetNotification("alert-success", "<strong>Attachments loaded successfully!!. </strong>" +
		"Please select attachment and click next");

		resetTab("tabAttachment");
		break;
	case 5:
		$("#sheet").hide();
		$("#attachment").hide();
		$("#recipient").hide();
		$("#tagsend").hide();
		resetNotification("alert-info", "Loading ... ");

		resetTab("tabRecipient");
		break;
	case 6:
		$("#sheet").hide();
		$("#attachment").hide();
		$("#recipient").show();
		$("#tagsend").hide();
		resetNotification("alert-success", "Enter email address and name to proceed..");

		resetTab("tabRecipient");
		break;
	case 7:
		$("#sheet").hide();
		$("#attachment").hide();
		$("#recipient").hide();
		$("#tagsend").show();
		resetNotification("alert-success", "Please verify data in <Strong> Your Selection</Strong> and click submit. <br/>" +
				"You will be redirected to <strong>Tag and Send page</Strong>, where you can add tags and make changes to your envelope");

		resetTab("tabSend");
		break;
	}
}

function resetNotification(classToAdd, notification){
	$("#notification").removeClass("alert-info");
	$("#notification").removeClass("alert-success");
	$("#notification").removeClass("alert-danger");
	$("#notification").addClass(classToAdd);
	$("#notification").html(notification);
}

function resetTab(id){
	$("#tabSheet").removeClass("active");
	$("#tabAttachment").removeClass("active");
	$("#tabRecipient").removeClass("active");
	$("#tabSend").removeClass("active");
	$("#"+id).addClass("active");
	
}
function setSelection(id, text, value){
	$("#"+id+" > .text").html(text);
	$("#"+id+" > .value").html(value);
}

function displayError(errorText){
	
}
package controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import models.Document;
import models.Envelope;
import models.Recipient;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Attachment;
import com.smartsheet.api.models.Sheet;
import com.smartsheet.api.models.User;
import com.smartsheet.api.models.UserProfile;

import play.Logger;
import play.cache.Cache;
import play.libs.Json;
import play.libs.WS;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.WS.Response;
import play.libs.WS.WSRequestHolder;
import play.mvc.Controller;
import play.mvc.Security;
import play.mvc.Http.Request;
import play.mvc.Result;
import utils.DocuSignUtility;
import views.html.sendnew;


/**
 * @author ananthhh
 *
 * <p> Provides entry point to all transactions for sending new document 
 * for signing via docusign </p>
 */
@Security.Authenticated(Secured.class)
public class SendNew extends Controller{
	/**
	 * It retrieves all sheets for the user.
	 * It will call the template and fill initial page for sendNew module with list of sheets.
	 * 
	 * @return Resullt object
	 * @throws SmartsheetException 
	 */
	public static Result render() throws SmartsheetException {
		return ok(sendnew.render());
    }
	
	/**
	 * It retrieves all sheets for the user.
	 * It will call the template and fill initial page for sendNew module with list of sheets.
	 * 
	 * @return Resullt object
	 * @throws SmartsheetException 
	 */
	public static Result getSheets() throws SmartsheetException {
		Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken(session().get("accessToken")).build();
		List<Sheet> homeSheets = smartsheet.sheets().listSheets();
		JsonNode temp = Json.toJson(homeSheets);
		return ok(temp.toString());
    }
	
	
	/**
	 * Returns list of attachments for selected sheet.
	 *  
	 * @param id - Selected sheet id
	 * @return Result object with list of Attachment name in JSON format
	 * @throws SmartsheetException 
	 */
	public static Result getAttachments(Long id) throws SmartsheetException{

		Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken(session().get("accessToken")).build();
		List<Attachment> attachments = smartsheet.sheets().attachments().listAttachments(id);
		JsonNode temp = Json.toJson(attachments);
		 return ok(temp.toString());
	}
	
	/**
	 * Update envelope object with selected attachment.
	 *  
	 * @param id - Selected sheet id
	 * @return Result object with list of Attachment name in JSON format
	 * @throws SmartsheetException 
	 */
	public static Result setAttachment() throws SmartsheetException{
		final Map<String, String[]> values = request().body().asFormUrlEncoded();
		if(getEnvelope().getDocuments().size() == 0) getEnvelope().getDocuments().add(new Document());
		getEnvelope().getDocuments().get(0).setName(values.get("name")[0]);
		getEnvelope().getDocuments().get(0).setAttachmentId(Long.parseLong((String)values.get("id")[0]));
		getEnvelope().getDocuments().get(0).setParentId(Long.parseLong((String)values.get("parent_id")[0]));
		getEnvelope().getDocuments().get(0).setSheetName(values.get("sheet_name")[0]);
		
		Logger.debug("Updated selected document details to envelope object"+"--" +
				" sheet name: "+values.get("sheet_name")[0]+" Name:"+values.get("name")[0]);
		return ok();
	}
	
	/**
	 * Search email address
	 * 
	 * @return List of Email addresses in JSON format 
	 * @throws SmartsheetException 
	 */
	public static Result getRecipients() throws SmartsheetException{
		Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken(session().get("accessToken")).build();
		List<User> userList = smartsheet.users().listUsers();
		JsonNode json = Json.toJson(userList);

		return ok(json.toString());
	}
	
	/**
	 * Update envelope object with selected recipients.
	 *  
	 * @param id - Selected sheet id
	 * @return Result object with list of Attachment name in JSON format
	 * @throws SmartsheetException 
	 */
	public static Result setRecipients() throws SmartsheetException{

		JsonNode json = request().body().asJson();
		Envelope env=getEnvelope();
		
		env.getRecipients().getSigners().clear();
		if(null != json && json.size() > 0){
			for(int i=0; i<json.size(); i++){
				if(!json.get(i).get("email").asText().equals(session().get("email"))){
					Recipient r = new Recipient();
					r.setRecipientId(i+1);
					r.setEmail(json.get(i).get("email").asText());
					r.setName(json.get(i).get("name").asText());
					env.getRecipients().getSigners().add(r);
				}
			}
		}
		Logger.debug("No. of signers added are "+env.getRecipients().getSigners().size());
		return ok();
	}
	/**
	 * Submit the document to docusign.
	 * Then upload the status to database
	 * 
	 * @return status 
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 * @throws SmartsheetException 
	 */
	public static Result sendDocument() throws JsonProcessingException, IOException, SmartsheetException{
		Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken(session().get("accessToken")).build();
		Attachment attachment = smartsheet.attachments().getAttachment(getEnvelope().getDocuments().get(0).getAttachmentId());
		Envelope env = getEnvelope();
		models.User user = Ebean.find(models.User.class)  
			    .where()  
			      .eq("email", session().get("email")).findUnique(); 
		
		env.getRecipients().getEditors().clear();
		
		/*
		 * Block to add user as Editor
		 * He will be there in all envelopes he sends
		 */
		Recipient editor = new Recipient();
		editor.setRecipientId(1);
		editor.setEmail(user.getEmail());
		if(null != user.getName() )
			editor.setName(user.getName());
		else
			editor.setName(user.getFirstName()+" "+user.getLastName());
		env.getRecipients().getEditors().add(editor);
		
		//Set email subject. This can be modified in Tag and send page
		env.setEmailSubject("Sign Document :"+ env.getDocuments().get(0).getName()); 	
		
		String url = DocuSignUtility.submitDraft(getEnvelope(), attachment.getUrl()); 
		
		env.setUser(user);
		env.getRecipientList().addAll(env.getRecipients().getSigners());
		env.getRecipientList().addAll(env.getRecipients().getEditors());
		
		//Save data to database
		Ebean.save(env);
		
		Logger.debug("Draft created successfully and will be redirected to tag and send embedded view");
		
		return redirect(url);
	}
	
	
	private static Envelope getEnvelope(){
		Envelope envelope = (Envelope) Cache.get(session().get("email")+"envelope");
		if(null == envelope){
			envelope = new Envelope();
			Cache.set(session().get("email")+"envelope", envelope);
		}
		return envelope;
	}
	
	
	
}

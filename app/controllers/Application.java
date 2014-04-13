package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

import models.User;

import com.avaje.ebean.Ebean;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.UserProfile;
import com.smartsheet.api.oauth.AuthorizationResult;
import com.smartsheet.api.oauth.OAuthFlow;
import com.smartsheet.api.oauth.OAuthFlowBuilder;
import com.smartsheet.api.oauth.Token;

import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.dashboard;
import views.html.login;


public class Application extends Controller {
	
	public static Result login() {
	    return ok(login.render());
	}
	
	public static Result authenticate() throws SmartsheetException{
		Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken("6fey44jw6g2l9rbguhmorttuob").build();
		UserProfile me = smartsheet.users().getCurrentUser();
		User user = Ebean.find(User.class)  
			    .where()  
			      .eq("email", me.getEmail()).findUnique();  
		if(null == user) user =new User();
		user.setEmail(me.getEmail());
		user.setName(me.getName());
		user.setFirstName(me.getFirstName());
		user.setLastName(me.getLastName());
		user.setAccessToken("6fey44jw6g2l9rbguhmorttuob");
		Ebean.save(user);
		
		session().put("email", user.getEmail());
		session().put("userId", user.getId().toString());
		session().put("accessToken", user.getAccessToken());
		clearCache(user.getEmail());
		return redirect(routes.Dashboard.render());
	}
	
	public static Result logout() {
		clearCache(session().get("email"));
	    session().clear();
	    flash("success", "You've been logged out");
	    return redirect(
	        routes.Application.login()
	    );
	}
	
	private static void clearCache(String email){
		Cache.remove(session().get("email")+"envelope");
	}
	
	public static void OAuth() throws SmartsheetException, UnsupportedEncodingException, URISyntaxException, 
	NoSuchAlgorithmException {

	// Setup the information that is necessary to request an authorization code
	OAuthFlow oauth = new OAuthFlowBuilder().setClientId("YOUR_CLIENT_ID").setClientSecret("YOUR_CLIENT_SECRET").
		setRedirectURL("https://YOUR_DOMAIN.com/").build();

	// Create the URL that the user will go to grant authorization to the application
	String url = oauth.newAuthorizationURL(EnumSet.of(com.smartsheet.api.oauth.AccessScope.CREATE_SHEETS, 
			com.smartsheet.api.oauth.AccessScope.WRITE_SHEETS), "key=YOUR_VALUE");

	// Take the user to the following URL
	System.out.println(url);

	// After the user accepts or declines the authorization they are taken to the redirect URL. The URL of the page
	// the user is taken to can be used to generate an authorization Result object.
	String authorizationResponseURL = "https://yourDomain.com/?code=l4csislal82qi5h&expires_in=239550&state=key%3D12344";

	// On this page pass in the full URL of the page to create an authorizationResult object  
	AuthorizationResult authResult = oauth.extractAuthorizationResult(authorizationResponseURL);

	
	// Get the token from the authorization result
	Token token = oauth.obtainNewToken(authResult);

	// Save the token or use it.
	}
	
}

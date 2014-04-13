package controllers;

import java.util.List;

import models.User;
import com.avaje.ebean.Ebean;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.UserProfile;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.dashboard;
import play.mvc.Security;
import models.Envelope;

@Security.Authenticated(Secured.class)
public class Dashboard extends Controller {
	
	public static Result render() {
		List<Envelope> env = Ebean.find(Envelope.class).where()  
			      .eq("user_id", Long.valueOf(session().get("userId"))).findList();
		return ok(dashboard.render(env));
    }
	
}

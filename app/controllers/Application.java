package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {

    public static Result dashboard() {
        return ok(dashboard.render("Your new application is ready."));
    }

}

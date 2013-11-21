package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class NotificationController extends Controller {
	
	public static Result view(Long userId) {
		
		return null;
	}
	
	public static Result forward(Long notificationId) {
		
		return null;
	}
	
	
	
	
}

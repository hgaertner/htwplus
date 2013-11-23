package controllers;

import java.util.List;

import models.Account;
import models.Notification;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;

@Transactional
@Security.Authenticated(Secured.class)
@With(Component.class)
public class NotificationController extends Controller {
	
	
	public static Result viewForUser(Long userId) {
		Account account = Account.findById(userId);
		
		if(account == null) {
			return badRequest("Das klappt so nicht");
		}
		
		if(!Secured.viewNotification(account)){
			return badRequest("Das darfst du nicht");
		}
		
		List<Notification> list = Notification.findByUser(account);
		return ok(views.html.Notification.list.render(list));
	}
	
	
	public static Result forward(Long notificationId) {
		
		return null;
	}
	
	
	
	
}

package controllers;

import java.util.List;

import org.apache.commons.logging.Log;

import models.Account;
import models.Notification;
import models.Notification.NotificationType;
import play.Logger;
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
		Notification note = Notification.findById(notificationId);
		
		if(note == null) {
			return badRequest("Das gibts doch garnicht!");
		}
		
		if(note.noteType == NotificationType.GROUP_NEW_POST) {
			return redirect(routes.GroupController.view(note.objectId));
		}
		
		// Delete Note here
		
		return badRequest("Invalid Request");
	}
	
	public static Result forwardByString(Long notificationId, String url) {
		Notification note = Notification.findById(notificationId);
		Logger.info(url);

		if(note == null) {
			return badRequest("Das gibts doch garnicht!");
		}
		
		if(note.noteType == NotificationType.GROUP_NEW_POST) {
			return redirect(url);
			//return redirect(routes.GroupController.view(note.objectId));
		}
	
		// Delete Note here
		
		return badRequest("Invalid Request");
	}
	
	
	
	
}

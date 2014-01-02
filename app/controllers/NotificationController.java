package controllers;

import java.util.List;

import models.Account;
import models.Notification;
import play.Logger;
import play.api.templates.Html;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import scala.collection.mutable.StringBuilder;

@Transactional
@Security.Authenticated(Secured.class)
public class NotificationController extends BaseController{
	
	public static Html view() {
		Account account = Component.currentAccount();
		
		if(account == null) {
			return new Html(new StringBuilder("Das wird nichts"));
		}
		
		if(!Secured.viewNotification(account)){
			return new Html(new StringBuilder("Das darfst du nicht!"));
		}
		
		List<Notification> list = Notification.findByUser(account);
		return views.html.Notification.list.render(list);
	}
	
	public static Result forward(Long notificationId, String url) {
		Notification note = Notification.findById(notificationId);
		Logger.info(url);

		if(note == null) {
			return badRequest("Das gibts doch garnicht!");
		}
		
		note.delete();		
		return redirect(url);
	}
	
	public static Result deleteAll(String url) {
		Notification.deleteByUser(Component.currentAccount());
		flash("success", "Alle Neuigkeiten wurden gelöscht.");
		return redirect(url);
	}
	
	
	
}

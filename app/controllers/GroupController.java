package controllers;

import java.util.List;

import controllers.Navigation.Level;
import models.Account;
import models.Group;
import models.GroupAccount;
import models.Media;
import models.Notification;
import models.Notification.NotificationType;
import models.Post;
import models.enums.GroupType;
import models.enums.LinkType;
import play.Logger;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import views.html.Group.index;
import views.html.Group.media;
import views.html.Group.view;
import views.html.Group.create;
import views.html.Group.edit;
import views.html.Group.token;


@Transactional
@Security.Authenticated(Secured.class)
public class GroupController extends BaseController {

	static Form<Group> groupForm = Form.form(Group.class);
	static Form<Post> postForm = Form.form(Post.class);
	
	public static Result index() {
		Navigation.set(Level.GROUPS, "Übersicht");
		Account account = Component.currentAccount();
		List<GroupAccount> groupRequests = GroupAccount.findRequests(account);
		List<Group> groupAccounts = GroupAccount.findGroupsEstablished(account);
		List<Group> courseAccounts = GroupAccount.findCoursesEstablished(account);
		return ok(index.render(groupAccounts,courseAccounts,groupRequests,groupForm));
	}
		
	@Transactional(readOnly=true)
	public static Result view(Long id) {
		Logger.info("Show group with id: " +id);
		Group group = Group.findById(id);
		
		if(!Secured.viewGroup(group)){
			return redirect(routes.Application.index());
		}
		
		Account account = Component.currentAccount();
		if (group == null) {
			Logger.error("No group found with id: " +id);
			return redirect(routes.GroupController.index());
		} else {
			Navigation.set(Level.GROUPS, "Newsstream", group.title, routes.GroupController.view(group.id));
			Logger.info("Found group with id: " +id);
			List<Post> posts = Post.getPostForGroup(group);
			return ok(view.render(group, posts, postForm));
		}
	}
	
	@Transactional(readOnly=true)
	public static Result media(Long id) {
		Form<Media> mediaForm = Form.form(Media.class);
		Group group = Group.findById(id);
		
		if(!Secured.viewGroup(group)){
			return redirect(routes.Application.index());
		}
		
		Account account = Component.currentAccount();
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			Navigation.set(Level.GROUPS, "Media", group.title, routes.GroupController.view(group.id));
			List<Media> mediaSet = group.media; 
			return ok(media.render(group, mediaForm, mediaSet));
		}
	}
	
	public static Result create() {
		Navigation.set(Level.GROUPS, "Erstellen");
		return ok(create.render(groupForm));
	}

	public static Result add() {	
		Navigation.set(Level.GROUPS, "Erstellen");
		
		// Get data from request
		Form<Group> filledForm = groupForm.bindFromRequest();
		
		// Perform JPA Validation
		if (filledForm.hasErrors()) {
			return badRequest(create.render(filledForm));
		} else {
			Group group = filledForm.get();
			int groupType;
			try {
				groupType = Integer.parseInt(filledForm.data().get("type"));
			} catch (NumberFormatException ex){
				filledForm.reject("type", "Bitte eine Sichtbarkeit wählen!");
				return ok(create.render(filledForm));
			}
			
			String successMsg;
			switch(groupType){
			
				case 0: group.groupType = GroupType.open; 
						successMsg = "Öffentliche Gruppe"; 
						break;
						
				case 1: group.groupType = GroupType.close; 
						successMsg = "Geschlossene Gruppe";
						break;
						
				case 2: group.groupType = GroupType.course;
						successMsg = "Kurs";
						String token = filledForm.data().get("token");
						if(!Group.validateToken(token)){
							filledForm.reject("token","Bitte einen Token zwischen 4 und 45 Zeichen eingeben!");
							return ok(create.render(filledForm));
						}
						
						if(!Secured.createCourse()) {
							flash("error", "Du darfst leider keinen Kurs erstellen");
							return badRequest(create.render(filledForm));
						}
						break;
						
				default: 
					filledForm.reject("Nicht möglich!");
					return ok(create.render(filledForm));
			}
			
			group.createWithGroupAccount(Component.currentAccount());
			flash("success", successMsg+" erstellt!");
			return redirect(routes.GroupController.index());
		}
	}

	@Transactional
	public static Result edit(Long id) {
		Group group = Group.findById(id);
		
		
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			Navigation.set(Level.GROUPS, "Bearbeiten", group.title, routes.GroupController.view(group.id));
			Form<Group> groupForm = Form.form(Group.class).fill(group);
			groupForm.data().put("type", String.valueOf(group.groupType.ordinal()));
			return ok(edit.render(group, groupForm));
		}
	}
	
	@Transactional
	public static Result update(Long groupId) {
		Group group = Group.findById(groupId);
		Navigation.set(Level.GROUPS, "Bearbeiten", group.title, routes.GroupController.view(group.id));	
		
		// Check rights
		if(!Secured.editGroup(group)) {
			return redirect(routes.GroupController.index());
		}
		
		Form<Group> filledForm = groupForm.bindFromRequest();
		int groupType = Integer.parseInt(filledForm.data().get("type"));
		String description = filledForm.data().get("description");

		switch(groupType){
			case 0: group.groupType = GroupType.open; 
					group.token = null;
					break;
			case 1: group.groupType = GroupType.close; 
					group.token = null;
					break;
			case 2: group.groupType = GroupType.course; 
					String token = filledForm.data().get("token");
					if(!Group.validateToken(token)){
						filledForm.reject("token","Bitte einen Token zwischen 4 und 45 Zeichen eingeben!");
						return ok(edit.render(group, filledForm));
					}					
					if(!Secured.createCourse()) {
						flash("error", "Du darfst leider keinen Kurs erstellen");
						return badRequest(edit.render(group, filledForm));
					}	
					group.token = token;
					break;
			default:
				filledForm.reject("Nicht möglich!");
				return ok(edit.render(group, filledForm));
		}
		group.description = description;
		group.update();
		flash("info", "'" + group.title + "' erfolgreich bearbeitet!");
		return redirect(routes.GroupController.view(groupId));
		
	}
	
	public static Result delete(Long id) {
		Group group = Group.findById(id);
		Account account = Component.currentAccount();
		if(Secured.deleteGroup(group)){
			group.delete();
			flash("info", "'" + group.title + "' wurde erfolgreich gelöscht!");
		} else {
			flash("error", "Dazu hast du keine Berechtigung!");
		}
		return redirect(routes.GroupController.index());
	}
	
	public static List<Group> showAll() {
		return Group.all();
	}
	
	public static Result token(Long groupId) {
		Group group = Group.findById(groupId);
		Navigation.set(Level.GROUPS, "Token eingeben", group.title, routes.GroupController.view(group.id));	
		return ok(token.render(group, groupForm));
	}
	
	public static Result validateToken(Long groupId) {
		Group group = Group.findById(groupId);
		
		if(Secured.isMemberOfGroup(group, Component.currentAccount())){
			Logger.debug("User is already member of group or course");
			flash("error", "Du bist bereits Mitglied dieser Gruppe!");
			return redirect(routes.GroupController.view(groupId));
		}
		
		Navigation.set(Level.GROUPS, "Token eingeben", group.title, routes.GroupController.view(group.id));	
		Form<Group> filledForm = groupForm.bindFromRequest();
		String enteredToken = filledForm.data().get("token");
		
		if(enteredToken.equals(group.token)){
			Account account = Component.currentAccount();
			GroupAccount groupAccount = new GroupAccount(account, group, LinkType.establish);
			groupAccount.create();
			flash("success", "Kurs erfolgreich beigetreten!");
			return redirect(routes.GroupController.view(groupId));
		} else {
			flash("error", "Hast du dich vielleicht vertippt? Der Token ist leider falsch.");
			return badRequest(token.render(group, filledForm));
		}
	}
	
	
	public static Result join(long id){
		Account account = Component.currentAccount();
		Group group = Group.findById(id);
		GroupAccount groupAccount;
				
		if(Secured.isMemberOfGroup(group, account)){
			Logger.debug("User is already member of group or course");
			flash("error", "Du bist bereits Mitglied dieser Gruppe!");
			return redirect(routes.GroupController.view(id));
		}
		
		// is already requested?
		groupAccount = GroupAccount.find(account, group);
		if(groupAccount != null && groupAccount.linkType.equals(LinkType.request) ){
			flash("info", "Deine Beitrittsanfrage wurde bereits verschickt!");
			return redirect(routes.GroupController.index());
		}
		
		if(groupAccount != null && groupAccount.linkType.equals(LinkType.reject)){
			flash("error", "Deine Beitrittsanfrage wurde bereits abgelehnt!");
			return redirect(routes.GroupController.index());
		}
		
		else if(group.groupType.equals(GroupType.open)){
			groupAccount = new GroupAccount(account, group, LinkType.establish);
			groupAccount.create();
			flash("success", "'" + group.title + " erfolgreich beigetreten!");
			return redirect(routes.GroupController.view(id));
		}
		
		else if(group.groupType.equals(GroupType.close)){
			groupAccount = new GroupAccount(account, group, LinkType.request);
			groupAccount.create();
			Notification.newNotification(NotificationType.GROUP_NEW_REQUEST, group.id, group.owner);
			flash("success", "Deine Anfrage wurde erfolgreich übermittelt!");
			return redirect(routes.GroupController.index());
		}
		
		else if(group.groupType.equals(GroupType.course)){
			return redirect(routes.GroupController.token(id));
		}
						
		return redirect(routes.GroupController.index());
	}
		
	public static Result removeMember(long groupId, long accountId){
		Account account = Account.findById(accountId);
		Group group = Group.findById(groupId);
		GroupAccount groupAccount = GroupAccount.find(account, group);
		
		if(!Secured.removeGroupMember(group, account)) {
			return redirect(routes.GroupController.index());
		}
		
		if(groupAccount != null){
			groupAccount.delete();
			if(account.equals(Component.currentAccount())){
				flash("info", "Gruppe erfolgreich verlassen!");
			} else {
				flash("info", "Mitglied erfolgreich entfernt!");
			}
			if(groupAccount.linkType.equals(LinkType.request)){
				flash("info", "Anfrage zurückgezogen!");			
			}
			if(groupAccount.linkType.equals(LinkType.reject)){
				flash("info", "Anfrage gelöscht!");			
			}
		} else {
			flash("info", "Das geht leider nicht :(");
		}
		return redirect(routes.GroupController.index());
	}
	
	public static Result acceptRequest(long groupId, long accountId){
		Account account = Account.findById(accountId);
		Group group = Group.findById(groupId);
		if(account != null && group != null && Secured.isOwnerOfGroup(group, Component.currentAccount())){
			GroupAccount groupAccount = GroupAccount.find(account, group);
			if(groupAccount != null){
				groupAccount.linkType = LinkType.establish;
				groupAccount.update();
			}
		}
		Notification.newNotification(NotificationType.GROUP_REQUEST_SUCCESS, groupId, account);
		return redirect(routes.GroupController.index());
	}
	
	public static Result declineRequest(long groupId, long accountId){
		Account account = Account.findById(accountId);
		Group group = Group.findById(groupId);
		if(account != null && group != null && Secured.isOwnerOfGroup(group, Component.currentAccount())){
			GroupAccount groupAccount = GroupAccount.find(account, group);
			if(groupAccount != null){
				groupAccount.linkType = LinkType.reject;
			}
		}
		Notification.newNotification(NotificationType.GROUP_REQUEST_DECLINE, groupId, account);
		return redirect(routes.GroupController.index());
	}
}

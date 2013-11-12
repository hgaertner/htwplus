package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.node.ObjectNode;


import controllers.Navigation.Level;

import play.*;
import play.libs.Json;
import play.mvc.*;
import play.data.*;

import models.Account;
import models.Group;
import models.GroupAccount;
import models.Media;
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
import views.html.Group.snippets.addModal;
import views.html.Group.snippets.addModalSuccess;
import views.html.Group.snippets.editModal;
import views.html.Group.snippets.editModalSuccess;
import views.html.Group.snippets.searchModalResult;
import views.html.Group.snippets.tokenForm;
import views.html.Profile.snippets.editForm;
import views.html.Group.searchresult;

@Security.Authenticated(Secured.class)
@Transactional
public class GroupController extends BaseController {

	static Form<Group> groupForm = Form.form(Group.class);
	static Form<Post> postForm = Form.form(Post.class);
	
	public static Result index() {
		Navigation.set(Level.GROUPS);
		Account account = Component.currentAccount();
		List<Group> groupAccounts = GroupAccount.findEstablished(account);
		List<GroupAccount> groupRequests = GroupAccount.findRequests(account);
								
		return ok(index.render(groupAccounts,groupRequests,groupForm));
	}
		
	@Transactional(readOnly=true)
	public static Result view(Long id) {
		Logger.info("Show group with id: " +id);
		Group group = Group.findById(id);
		Account account = Component.currentAccount();
		if (group == null) {
			Logger.error("No group found with id: " +id);
			return redirect(routes.GroupController.index());
		} else {
			Navigation.set(Level.GROUPS, "Newsstream", group.title, routes.GroupController.view(group.id));
			Logger.info("Found group with id: " +id);
			List<Post> posts = Post.getPostForGroup(group);
			return ok(view.render(group, posts, postForm, Secured.isMemberOfGroup(group, account)));
		}
	}
	
	@Transactional(readOnly=true)
	public static Result media(Long id) {
		Form<Media> mediaForm = Form.form(Media.class);
		Group group = Group.findById(id);
		if(!Secured.isMemberOfGroup(group, Component.currentAccount())){
			flash("info","Bitte tritt der Gruppe erst bei.");
			return view(id);
		}
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			Navigation.set(Level.GROUPS, "Media", group.title, routes.GroupController.view(group.id));
			List<Media> mediaSet = group.media; 
			return ok(media.render(group, mediaForm, mediaSet));
		}
	}

	public static Result create() {
		Account account = Component.currentAccount();
		Form<Group> filledForm = groupForm.bindFromRequest();
		int groupType = Integer.parseInt(filledForm.data().get("optionsRadios"));
		if (filledForm.hasErrors()) {
			return ok(addModal.render(filledForm));
		} else {
			Group group = filledForm.get();
			if(filledForm.data().get("optionsRadios").equals("1")){
				group.isClosed = true;
			}
			switch(groupType){
				case 0: group.groupType = GroupType.open; break;
				case 1: group.groupType = GroupType.close; break;
				case 2: group.groupType = GroupType.course; break;
				default: 
					filledForm.reject("Nicht möglich!");
					return ok(addModal.render(filledForm));
			}
			
			if(groupType == 2){
				if(filledForm.data().get("token").equals("")){
					filledForm.reject("token","Bitte token eingeben!");
					return ok(addModal.render(filledForm));
				}
			}
			
			group.createWithGroupAccount(account);
			flash("success", "Neue Gruppe erstellt!");
			return ok(addModalSuccess.render());
		}
	}

	@Transactional
	public static Result edit(Long id) {
		Group group = Group.findById(id);
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			return ok(editModal.render(group, groupForm.fill(group)));
		}
	}
	
	@Transactional
	public static Result update(Long groupId) {
		Group group = Group.findById(groupId);
		Form<Group> filledForm = groupForm.bindFromRequest();
		int groupType = Integer.parseInt(filledForm.data().get("optionsRadios"));
		String description = filledForm.data().get("description");
		if(filledForm.data().get("optionsRadios").equals("1")){
			group.isClosed = true;
		} else {
			group.isClosed = false;
		}
		switch(groupType){
			case 0: group.groupType = GroupType.open; break;
			case 1: group.groupType = GroupType.close; break;
			case 2: group.groupType = GroupType.course; break;
			default:
				filledForm.reject("Nicht möglich!");
				return ok(addModal.render(filledForm));
		}
		group.description = description;
		group.update();
		return ok(editModalSuccess.render());
		
	}
	
	public static Result delete(Long id) {
		Group group = Group.findById(id);
		Account account = Component.currentAccount();
		if(Secured.isOwnerOfGroup(group, account)){
			group.delete();
			flash("info", "Gruppe " + group.title + " erfolgreich gelöscht!");
		} else {
			flash("error", "Dazu hast du keine Berechtigung!");
		}
		return redirect(routes.GroupController.index());
	}
	
	public static List<Group> showAll() {
		return Group.all();
	}
	
	
	public static Result searchForGroupByKeyword(){
		List<Group> result = null;
		final Set<Map.Entry<String, String[]>> entries = request()
				.queryString().entrySet();
		for (Map.Entry<String, String[]> entry : entries) {
			if (entry.getKey().equals("keyword")) {
				final String keyword = entry.getValue()[0];
				Logger.debug("Value of key" + keyword);
				result = Group.searchForGroupByKeyword(keyword);
				if (result != null && result.size() > 1) {
					Logger.debug("Found " + result.size()
							+ " groups with the keyword: " + keyword);
				}
			}

		}

		return ok(searchresult.render(result));
	}
	
	public static Result enterToken(long groupId) {
		Account account = Component.currentAccount();
		ObjectNode result = Json.newObject();
		Group group = Group.findById(groupId);
		Form<Group> filledForm = groupForm.bindFromRequest();
		String token = filledForm.data().get("token");
		if(token.equals(group.token)){
			GroupAccount groupAccount = new GroupAccount(account, group, LinkType.establish);
			groupAccount.create();
			result.put("status", "redirect");
			result.put("url", routes.GroupController.view(groupId).toString());
			flash("success", "Gruppe erfolgreich beigetreten!");
		} else {
			result.put("status", "response");
			filledForm.reject("token","Der Token ist falsch.");
			String form = tokenForm.render(group, filledForm).toString();
			
			result.put("payload", form);
		}
		
		return ok(result);
	}
	
	
	public static Result join(long id){
		Account account = Component.currentAccount();
		Group group = Group.findById(id);
		ObjectNode result = Json.newObject();
		GroupAccount groupAccount;
		
		if(Secured.isMemberOfGroup(group, account)){
			result.put("status", "redirect");
			result.put("url", routes.GroupController.view(id).toString());
			flash("info", "Du bist bereits Mitglied dieser Gruppe!");
		}
		
		if(group.groupType.equals(GroupType.open)){
			groupAccount = new GroupAccount(account, group, LinkType.establish);
			groupAccount.create();
			result.put("status", "redirect");
			result.put("url", routes.GroupController.view(id).toString());
			flash("success", "Gruppe erfolgreich beigetreten!");
		}
		
		if(group.groupType.equals(GroupType.close)){
			groupAccount = new GroupAccount(account, group, LinkType.request);
			groupAccount.create();
			result.put("status", "redirect");
			result.put("url", routes.GroupController.index().toString());
			flash("success", "Deine Anfrage wurde erfolgreich übermittelt!");
		}
		
		if(group.groupType.equals(GroupType.course)){
			
			result.put("status", "response");
			String form = tokenForm.render(group, groupForm).toString();
			result.put("payload", form);
		}
				
		return ok(result);
	}
		
	public static Result removeMember(long groupId, long accountId){
		Account account = Account.findById(accountId);
		Group group = Group.findById(groupId);
		GroupAccount groupAccount = GroupAccount.find(account, group);
		if(groupAccount != null && !Secured.isOwnerOfGroup(group, account)){
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
		return redirect(routes.GroupController.index());
	}
}

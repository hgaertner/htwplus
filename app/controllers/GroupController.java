package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import controllers.Navigation.Level;

import play.*;
import play.mvc.*;
import play.data.*;

import models.Account;
import models.Group;
import models.GroupAccount;
import models.Media;
import models.Post;

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

@Security.Authenticated(Secured.class)
@Transactional
public class GroupController extends BaseController {

	static Form<Group> groupForm = Form.form(Group.class);
	static Form<Post> postForm = Form.form(Post.class);
	
	public static Result index() {
		Navigation.set(Level.GROUPS);
		Account account = Component.currentAccount();
		List<GroupAccount> groupAccounts = GroupAccount.allByAccount(account);
		List<Group> approvedGroups = new ArrayList<Group>();
		List<GroupAccount> unapprovedGroups = new ArrayList<GroupAccount>();
		
		// split Groups (approved/unapproved)
		if(!groupAccounts.isEmpty()){
			for(GroupAccount groupAccount : groupAccounts){
				if(groupAccount.approved == null){
					unapprovedGroups.add(groupAccount);
				} else {
					if(groupAccount.approved == true && groupAccount.account.equals(account)){
						approvedGroups.add(groupAccount.group);
					} 

					if(groupAccount.approved == false && groupAccount.account.equals(account)){
						unapprovedGroups.add(groupAccount);
					}
				}
			}
		}
				
		return ok(index.render(approvedGroups,unapprovedGroups,groupForm));
	}
		
	@Transactional(readOnly=true)
	public static Result view(Long id) {
		Logger.info("Show group with id: " +id);
		Group group = Group.findById(id);
		Navigation.set(Level.GROUPS, "Newsstream", group.title, routes.GroupController.view(group.id));
		Account account = Component.currentAccount();
		if (group == null) {
			Logger.error("No group found with id: " +id);
			return redirect(routes.GroupController.index());
		} else {
			Logger.info("Found group with id: " +id);
			List<Post> posts = Post.getPostForGroup(group);
			return ok(view.render(group, posts, postForm, Secured.isMemberOfGroup(group, account)));
		}
	}
	
	@Transactional(readOnly=true)
	public static Result media(Long id) {
		Form<Media> mediaForm = Form.form(Media.class);
		Group group = Group.findById(id);
		Navigation.set(Level.GROUPS, "Media", group.title, routes.GroupController.view(group.id));
		if(!Secured.isMemberOfGroup(group, Component.currentAccount())){
			flash("info","Bitte tritt der Gruppe erst bei.");
			return view(id);
		}
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			List<Media> mediaSet = group.media; 
			return ok(media.render(group, mediaForm, mediaSet));
		}
	}

	public static Result create() {
		Account account = Component.currentAccount();
		Form<Group> filledForm = groupForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return ok(addModal.render(filledForm));
		} else {
			Group group = filledForm.get();
			group.create(account);
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
		String description = groupForm.bindFromRequest().data().get("description");
		if(groupForm.bindFromRequest().data().get("isClosed") == null){
			group.isClosed = false;
		} else {
			group.isClosed = true;
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
	
	
	public static Result searchForGroupByKeyword(final String keyword){
		Logger.info("Search for group with keyword: " +keyword);
		List<Group> result = Group.searchForGroupByKeyword(keyword);
		return ok(searchModalResult.render(result));
	}
	
	
	public static Result join(long id){
		Account account = Component.currentAccount();
		Group group = Group.findById(id);
		if(Secured.isMemberOfGroup(group, account)){
			flash("info", "Du bist bereits Mitglied dieser Gruppe!");
			return redirect(routes.GroupController.view(id));
		}
		if(GroupAccount.find(account, group) == null){
			GroupAccount groupAccount = new GroupAccount(account, group);
			if(!group.isClosed){
				groupAccount.approved = true;
				flash("success", "Gruppe erfolgreich beigetreten!");
			} else {
				flash("success", "Deine Anfrage wurde erfolgreich übermittelt!");
			}
			groupAccount.create();
			return redirect(routes.GroupController.view(id));
		}
		
		return redirect(routes.GroupController.index());
	}
		
	public static Result removeMember(long groupId, long accountId){
		Account account = Account.findById(accountId);
		Group group = Group.findById(groupId);
		GroupAccount groupAccount = GroupAccount.find(Account.findById(accountId), group);
		if(groupAccount != null && !Secured.isOwnerOfGroup(group, account)){
			groupAccount.delete();
			if(account.equals(Component.currentAccount())){
				flash("info", "Gruppe erfolgreich verlassen!");
			} else {
				flash("info", "Mitglied erfolgreich entfernt!");
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
				groupAccount.approved = true;
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
				groupAccount.approved = false;
			}
		}
		return redirect(routes.GroupController.index());
	}
}

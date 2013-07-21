package controllers;

import java.util.ArrayList;
import java.util.List;

import play.*;
import play.mvc.*;
import play.data.*;
import models.Account;
import models.Group;
import models.GroupAccount;
import models.Post;
import views.html.Group.*;
import views.html.Group.snippets.*;
import play.db.jpa.*;

@Security.Authenticated(Secured.class)
@Transactional
public class GroupController extends BaseController {

	static Form<Group> groupForm = Form.form(Group.class);
	static Form<Post> postForm = Form.form(Post.class);
	
	public static Result index() {
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
	
	@Transactional
	public static Result addPost(long groupId) {
		Group group = Group.findById(groupId);
		Account account = Component.currentAccount();
		if (Secured.isMemberOfGroup(group, account)) {
			Form<Post> filledForm = postForm.bindFromRequest();
			if (filledForm.hasErrors()) {
				flash("message", "Error in Form!");
			} else {
				Post p = filledForm.get();
				Post p2 = new Post();
				p2.content = p.content;
				p2.owner = Component.currentAccount();
				p2.group = Group.findById(groupId);
				p2.create();
			}
		} else {
			flash("message","Bitte tritt der Gruppe erst bei.");
		}
		return view(groupId);
	}
	
	@Transactional(readOnly=true)
	public static Result view(Long id) {
		Group group = Group.findById(id);
		Account account = Component.currentAccount();
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			List<Post> posts = Post.getPostForGroup(id);
			return ok(view.render(group, posts, postForm, Secured.isMemberOfGroup(group, account)));
		}
	}
	
	@Transactional(readOnly=true)
	public static Result media(Long id) {
		Group group = Group.findById(id);
		if(!Secured.isMemberOfGroup(group, Component.currentAccount())){
			flash("message","Bitte tritt der Gruppe erst bei.");
			return view(id);
		}
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			return ok(media.render(group));
		}
	}

	public static Result create() {
		Account account = Component.currentAccount();
		Form<Group> filledForm = groupForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return ok(addModal.render(filledForm));
		} else {
			Group g = filledForm.get();
			if(filledForm.bindFromRequest().data().get("isClosed") == null){
				g.isClosed = false;
			}
			g.create(account);
			flash("message", "Neue Gruppe erstellt!");
			return ok(addModalSuccess.render());
		}
	}

	@Transactional
	public static Result edit(Long id) {
		Group group = Group.findById(id);
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ok(editModal.render(group, groupForm.fill(group)));
		}
	}
	
	@Transactional
	public static Result update(Long id) {
		Group group = Group.findById(id);
		String description = groupForm.bindFromRequest().data().get("description");
		if(description.equals("") || description == null){
			groupForm.reject("description", "Bitte wähle eine Beschreibung");
			return ok(editModal.render(group, groupForm));
		} else {
			if(groupForm.bindFromRequest().data().get("isClosed") == null){
				group.isClosed = false;
			} else {
				group.isClosed = true;
			}
			group.description = description;
			group.update();
			return ok(addModalSuccess.render());
		}
	}
	
	public static Result delete(Long id) {
		Group group = Group.findById(id);
		Account account = Component.currentAccount();
		if(Secured.isOwnerOfGroup(group, account)){
			group.delete();
			flash("message", "Gruppe " + group.title + " erfolgreich gelöscht!");
		} else {
			flash("message", "Dazu hast du keine Berechtigung!");
		}
		return redirect(routes.GroupController.index());
	}
	
	public static List<Group> showAll() {
		return Group.all();
	}
	
	public static Result join(long id){
		Account account = Component.currentAccount();
		Group group = Group.findById(id);
		if(Secured.isMemberOfGroup(group, account)){
			flash("message", "Du bist bereits Mitglied dieser Gruppe!");
			return redirect(routes.GroupController.view(id));
		}
		if(GroupAccount.find(account, group) == null){
			GroupAccount groupAccount = new GroupAccount(account, group);
			if(!group.isClosed){
				groupAccount.approved = true;
				flash("message", "Gruppe erfolgreich beigetreten!");
			} else {
				flash("message", "Deine Anfrage wurde erfolgreich übermittelt!");
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
			groupAccount.remove();
			if(account.equals(Component.currentAccount())){
				flash("message", "Gruppe erfolgreich verlassen!");
			} else {
				flash("message", "Mitglied erfolgreich entfernt!");
			}
		} else {
			flash("message", "Das geht leider nicht :(");
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

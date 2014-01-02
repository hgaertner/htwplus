package controllers;


import models.Account;
import models.Friendship;
import models.Group;
import models.Media;
import models.Post;
import models.enums.AccountRole;
import models.enums.GroupType;
import play.Logger;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;
import views.html.index;
import views.html.login;

public class Secured extends Security.Authenticator {

	@Override
    public String getUsername(Context ctx) {
        return ctx.session().get("id");
    }
	
	@Override
    public Result onUnauthorized(Context ctx) {
		Logger.info("Unauthorized - Redirect to Login");
		return ok(login.render());
    }
		
	//Access rights
	
	public static boolean isMemberOfGroup(Group group, Account account){
		return Group.isMember(group, account);
	}
	
	public static boolean isOwnerOfGroup(Group group, Account account){
		if(group != null){
			return group.owner.equals(account);
		} else {
			return false;
		}
	}

	public static boolean isAllowedToDeletePost(final Post post, final Account account){
		if(post != null && post.account != null){
			if(isOwnerOfPost(post, account) || post.account.equals(account)){
				return true;
			}
		}else if(post != null && post.parent != null && post.parent.account != null){
			if (isOwnerOfPost(post, account) || post.parent.account.equals(account)){
				return true;
			}
		}else if(post != null && isOwnerOfPost(post, account)){
			return true;
		}
		return false;
	}
	
	public static boolean isOwnerOfPost(Post post, Account account){
		if(post != null){
			return post.owner.equals(account);
		}
		return false;
	}
		
	public static boolean isOwnerOfMedia(final Long mediaId){
		return Media.isOwner(mediaId, Component.currentAccount());
	}
		
	public static boolean isOwnerOfAccount(final Long accountId){
		return Account.isOwner(accountId,Component.currentAccount());
	}
	public static boolean isFriend(Account account){
		return Friendship.alreadyFriendly(Component.currentAccount(), account);
	}
	
	public static boolean editAccount(Account account) {
		if(Component.currentAccount().equals(account)){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean viewNotification(Account account) {
		if(Component.currentAccount().equals(account)){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean createCourse() {
		Account current = Component.currentAccount();
		if(current.role == AccountRole.TUTOR || current.role == AccountRole.ADMIN) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean viewGroup(Group group) {
		Account current = Component.currentAccount();
		
		if(group == null){
			return false;
		}
		
		if(Secured.isAdmin()){
			return true;
		}
		
		switch(group.groupType){
		
			case open:
				return true;
					
			case close: 
				if(Secured.isMemberOfGroup(group, current)) {
					return true;
				} 
			case course: 
				if(Secured.isMemberOfGroup(group, current)) {
					return true;
				} 
			default: 
					return false;
		}
		
	}
	
	public static boolean uploadMedia(Group group) {
		Account current = Component.currentAccount();
		
		if(group == null) {
			return false;
		}
		
		if(Secured.isAdmin()){
			return true;
		}
		
		switch(group.groupType){
			case open:
				if(Secured.isMemberOfGroup(group, current)) {
					return true;
				} 
					
			case close: 
				if(Secured.isMemberOfGroup(group, current)) {
					return true;
				} 
			case course: 
				if(Secured.isOwnerOfGroup(group, current)) {
					return true;
				} 
			default: 
					return false;
		}
	}
	
	public static boolean viewMedia(Media media) {
		return Secured.viewGroup(media.group);
	}
	
	public static boolean deleteMedia(Media media) {
		Account current = Component.currentAccount();
		Group group = media.group;
		if(Secured.isAdmin()){
			return true;
		}
		
		if(Secured.isOwnerOfGroup(group, current)){
			return true;
		}
		
		if(media.owner.equals(current)){
			return true;
		}
		
		return false;
	}
		
	public static boolean isAdmin(){
		Account current = Component.currentAccount();
		if(current.role == AccountRole.ADMIN){
			return true;
		} else {
			return false;
		}
	}
	
}

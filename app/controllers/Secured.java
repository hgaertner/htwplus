package controllers;


import play.*;
import play.mvc.*;
import play.mvc.Http.*;
import views.html.index;

import models.*;
import models.enums.*;

public class Secured extends Security.Authenticator {

	@Override
    public String getUsername(Context ctx) {
        return ctx.session().get("id");
    }
	
	@Override
    public Result onUnauthorized(Context ctx) {
		return ok(index.render());
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
		}else if(post != null && post.parent != null){
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
	
	public static boolean viewMedia(Media media) {
		if(media.group != null) {
			if(Group.isMember(media.group, Component.currentAccount())){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isOwnerOfMedia(final Long mediaId){
		return Media.isOwner(mediaId, Component.currentAccount());
	}
		
	public static boolean isOwnerOfAccount(final Long accountId, final Context ctx){
		return Account.isOwner(accountId, ctx.session().get("email"));
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
	
}

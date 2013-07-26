package controllers;


import play.*;
import play.mvc.*;
import play.mvc.Http.*;
import views.html.index;

import models.*;

public class Secured extends Security.Authenticator {

	@Override
    public String getUsername(Context ctx) {
        return ctx.session().get("email");
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
	
	public static boolean isOwnerOfCourse(final Long courseId){
		return Course.isOwner(courseId, Component.currentAccount());
	}
	
	public static boolean isMemberOfCourse(final Long courseId){
		return Course.isMember(courseId, Component.currentAccount());
	}
	
	public static boolean isOwnerOfAccount(final Long accountId, final Context ctx){
		return Account.isOwner(accountId, ctx.session().get("email"));
	}
}

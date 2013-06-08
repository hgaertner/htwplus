package controllers;


import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import models.*;

public class Secured extends Security.Authenticator {

	@Override
    public String getUsername(Context ctx) {
        return ctx.session().get("email");
    }
	
	@Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }
	
	
	//Access rights
	
	public static boolean isMemberOfGroup(final Long groupId, final Context ctx){
		return Group.isMember(groupId, ctx.session().get("email"));
	}
	
	
	public static boolean isOwnerOfPost(final Long postId, final Context ctx){
		return Post.isOwner(postId, ctx.session().get("email"));
	}
	
	public static boolean isOwnerOfMedia(final Long mediaId, final Context ctx){
		return Media.isOwner(mediaId, ctx.session().get("email"));
	}
	
	public static boolean isOwnerOfCourse(final Long courseId, final Context ctx){
		return Course.isOwner(courseId, ctx.session().get("email"));
	}
	
	public static boolean isMemberOfCourse(final Long courseId, final Context ctx){
		return Course.isMember(courseId, ctx.session().get("email"));
	}
	
	public static boolean isOwnerOfAccount(final Long accountId, final Context ctx){
		return Account.isOwner(accountId, ctx.session().get("email"));
	}
}

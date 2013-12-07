package controllers;

import java.util.List;

import models.Account;
import models.Group;
import models.Media;
import models.Post;
import play.Logger;
import play.Play;
import play.api.mvc.Call;
import play.api.templates.Html;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class PostController extends BaseController {
	
	static Form<Post> postForm = Form.form(Post.class);
	static final int PAGE = 1;
	
	/**
	 * @author Iven
	 * @param anyId - can be a accountId or groupId
	 * @param target - define target stream: profile-stream, group-stream
	 * @return
	 */
	public static Result addPost(Long anyId, String target) {
		Account account = Component.currentAccount();
		Form<Post> filledForm = postForm.bindFromRequest();
		
		if(target.equals(Post.GROUP)) {
			Group group = Group.findById(anyId);
			if (Secured.isMemberOfGroup(group, account)) {
				if (filledForm.hasErrors()) {
					flash("error", "Jo, fast. Probiere es noch einmal mit Inhalt ;-)");
				} else {
					Post p = filledForm.get();
					p.owner = Component.currentAccount();
					p.group = group;
					p.create();
				}
			} else {
				flash("info","Bitte tritt der Gruppe erst bei.");
			}
			return redirect(routes.GroupController.view(group.id));
		}
		
		if(target.equals(Post.PROFILE)) {
			Account profile = Account.findById(anyId);
			if(Secured.isFriend(profile) || profile.equals(account)){
				if (filledForm.hasErrors()) {
					flash("error", "Jo, fast. Probiere es noch einmal mit Inhalt ;-)");
				} else {
					Post p = filledForm.get();
					p.account = profile;
					p.owner = account;
					p.create();
				}
				return redirect(routes.ProfileController.stream(anyId, PAGE));
			}
			flash("info","Du kannst nur Freunden auf den Stream schreiben!");
			return redirect(routes.ProfileController.stream(anyId, PAGE));
		}
		
		if(target.equals(Post.STREAM)) {
			Account profile = Account.findById(anyId);
			if(profile.equals(account)){
				if (filledForm.hasErrors()) {
					flash("error", "Jo, fast. Probiere es noch einmal mit Inhalt ;-)");
				} else {
					Post p = filledForm.get();
					p.account = profile;
					p.owner = account;
					p.create();
				}
				return redirect(routes.Application.stream(PAGE));
			}
			flash("info","Du kannst nur dir oder Freunden auf den Stream schreiben!");
			return redirect(routes.Application.stream(PAGE));
		}
		return redirect(routes.Application.index());
	}
	
	@Transactional
	public static Result addComment(long postId) {
		Post parent = Post.findById(postId);
		Account account = Component.currentAccount();
		Form<Post> filledForm = postForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			System.out.println(filledForm.errors());
			flash("error", "Error in Form!");
		return badRequest();
		} else {
			Post post = filledForm.get();
			post.owner = account;
			post.parent = parent;
			post.create();
			return ok(views.html.snippets.postComment.render(post));
		}
	}
	

	
	@Transactional
	public static List<Post> getCommentsForPostInGroup(Long id) {
		int max = Integer.parseInt(Play.application().configuration().getString("htwplus.comments.init"));
		int count = Post.countCommentsForPost(id);
		if(count <= max){
			return Post.getCommentsForPost(id, 0, count);
		} else {
			return Post.getCommentsForPost(id, count-max, count);
		}
	}
	
	@Transactional
	public static Result getOlderComments(Long id, Integer current) {
		String result = "";
		//int max = Integer.parseInt(Play.application().configuration().getString("htwplus.comments.init"));
		int max = current;
		int count = Post.countCommentsForPost(id);
		List<Post> comments = null;
		if(count <= max){
			return ok(result);	
		} else {
			comments = Post.getCommentsForPost(id, 0, count-max);
			for (Post post : comments) {
				result = result.concat(views.html.snippets.postComment.render(post).toString());
			}
			return ok(result);	
		}
	}
	
	@Transactional
	public static Result deletePost(final Long postId) {
		final Post post = Post.findById(postId);
		// verify redirect after deletion
		Call routesTo = null;
		if(post.group != null){
			routesTo = routes.GroupController.view(post.group.id);
		}
		else if(post.account != null){
			routesTo = routes.Application.index();
		}
		else if(post.parent != null)
		{
			if(post.parent.group != null){
				routesTo = routes.GroupController.view(post.parent.group.id);
			}else if(post.account != null) {
				routesTo = routes.Application.index();
			}
		}
		final Account account = Component.currentAccount();
		if (Secured.isAllowedToDeletePost(post, account)) {
			post.delete();
			flash("success", "Gelöscht!");
		} else {
			flash("error", "Konnte nicht gelöscht werden!");
		}

		return redirect(routesTo);
	}
	
}

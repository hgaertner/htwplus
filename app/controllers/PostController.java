package controllers;

import java.util.List;

import models.Account;
import models.Course;
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
	
	public static Result addPost(Long anyId, String target) {
		Account account = Component.currentAccount();
		Form<Post> filledForm = postForm.bindFromRequest();
		
		if(target.equals(Post.GROUP)) {
			Group group = Group.findById(anyId);
			if (Secured.isMemberOfGroup(group, account)) {
				if (filledForm.hasErrors()) {
					flash("error", "Error in Form!");
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
		
		if(target.equals(Post.COURSE)) {
			Logger.info("Ich poste in den Course");		
		}
		if(target.equals(Post.STREAM)) {

			if (filledForm.hasErrors()) {
				flash("error", "Error in Form!");
			} else {
				Post p = filledForm.get();
				p.account = account;
				p.owner = account;
				p.create();
			}
			return redirect(routes.Application.index());
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
			post.owner = Component.currentAccount();
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
	public static Result deletePost(Long postId) {
		
		// verify redirect after deletion
		Call routesTo = routes.Application.index();
		
		Post post = Post.findById(postId);
		Account account = Component.currentAccount();
		if (Secured.isOwnerOfPost(post, account)) {
			post.delete();
			flash("info", "Post gelöscht");
		} else {
			flash("info", "Post konnte nicht gelöscht werden");
		}

		return redirect(routesTo);
	}
	
	
	
}

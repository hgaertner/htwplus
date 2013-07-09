package controllers;

import java.util.List;

import models.Account;
import models.Course;
import models.Group;
import models.Post;
import play.Logger;
import play.Play;
import play.api.mvc.Call;
import play.api.templates.Html;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Group.snippets.comment;

public class PostController extends BaseController {
	
	static Form<Post> postForm = Form.form(Post.class);
	
	@Transactional
	public static Result addComment(long postId) {
		Post parent = Post.findById(postId);
		Account account = Component.currentAccount();
		if (Secured.isMemberOfGroup(parent.group, account)) {
			Form<Post> filledForm = postForm.bindFromRequest();
			if (filledForm.hasErrors()) {
				System.out.println(filledForm.errors());
				flash("message", "Error in Form!");
				return badRequest();
			} else {
				Post p = filledForm.get();
				Post p2 = new Post();
				p2.content = p.content;
				p2.owner = Component.currentAccount();
				p2.group = parent.group;
				p2.parent = Post.findById(postId);
				p2.create();
				return ok(comment.render(p2));
			}
		} else {
			return forbidden();
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
	public static Result getOlderComments(Long id) {
		String result = "";
		int max = Integer.parseInt(Play.application().configuration().getString("htwplus.comments.init"));
		int count = Post.countCommentsForPost(id);
		List<Post> comments = null;
		if(count <= max){
			return ok(result);	
		} else {
			comments = Post.getCommentsForPost(id, 0, count-max);
			for (Post post : comments) {
				result = result.concat(views.html.Group.snippets.comment.render(post).toString());
			}
			return ok(result);	
		}
	}
	
	@Transactional
	public static Result deletePost(Long postId, Long anyId) {
		
		// verify redirect after deletion
		Call routesTo = routes.Application.index();
		if(Group.findById(anyId) != null){
			routesTo = routes.GroupController.view(anyId);
		}		
		if(Course.findById(anyId) != null){
			routesTo = routes.CourseController.view(anyId);
		}
		
		Post post = Post.findById(postId);
		Account account = Component.currentAccount();
		if (Secured.isOwnerOfPost(post, account)) {
			post.delete();
			flash("message", "Post gelöscht");
		} else {
			flash("message", "Post konnte nicht gelöscht werden");
		}

		return redirect(routesTo);
	}
	
	
	
}

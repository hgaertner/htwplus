package controllers;

import java.util.List;

import play.*;
import play.mvc.*;
import play.data.*;
import models.Account;
import models.Group;
import models.Post;
import views.html.Group.*;
import views.html.Group.snippets.*;
import play.db.jpa.*;

@Security.Authenticated(Secured.class)
@Transactional
public class GroupController extends BaseController {

	static Form<Group> groupForm = Form.form(Group.class);

	static Form<Post> postForm = Form.form(Post.class);
	
	public static Result showAll() {
		return ok(index.render(Group.all(),groupForm));
	}

	public static Result index() {
		Account account = Component.currentAccount();
		return ok(index.render(Group.allByAccount(account),groupForm));
	}
	
	@Transactional
	public static Result addPost(long groupId) {
		if (Secured.isMemberOfGroup(groupId)) {
			Form<Post> filledForm = postForm.bindFromRequest();
			if (filledForm.hasErrors()) {
				flash("message", "Error in Form!");
				// return badRequest(view.render(arg0, arg1));
			} else {
				Post p = filledForm.get();
				try {
					final long accountId = Long.parseLong(session().get("id"));
					Post p2 = new Post();
					p2.content = p.content;
					p2.owner = Account.findById(accountId);
					p2.group = Group.findById(groupId);
					p2.create();
					// } else {
					// TODO return error Message current user is not a member of
					// the course...
					// }
				} catch (NumberFormatException exp) {
					// TODO log exception and handle it...
				}
			}
		} else {
			return forbidden();
		}
		return view(groupId);
	}
	
	@Transactional
	public static Result addComment(long postId, long groupId) {
		if (Secured.isMemberOfGroup(groupId)) {
			Form<Post> filledForm = postForm.bindFromRequest();
			if (filledForm.hasErrors()) {
				System.out.println(filledForm.errors());
				flash("message", "Error in Form!");
				// return badRequest(view.render(arg0, arg1));
			} else {
				Post p = filledForm.get();
				try {
					final long accountId = Long.parseLong(session().get("id"));
					Post p2 = new Post();
					p2.content = p.content;
					p2.owner = Account.findById(accountId);
					p2.group = Group.findById(groupId);
					p2.parent = Post.findById(postId);
					p2.create();
					// } else {
					// TODO return error Message current user is not a member of
					// the course...
					// }
				} catch (NumberFormatException exp) {
					// TODO log exception and handle it...
				}
			}
		} else {
			return forbidden();
		}
		return view(groupId);
	}
	
	@Transactional(readOnly=true)
	public static Result view(Long id) {
		Group group = Group.findById(id);
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			Form<Post> formPost = Form.form(Post.class);
			List<Post> posts = Post.getPostForGroup(id);
			return ok(view.render(group, posts, postForm));
		}
	}
	
	@Transactional(readOnly=true)
	public static Result media(Long id) {
		Group group = Group.findById(id);
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
			g.create(account);
			flash("message", "Created new Group!");
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
			group.description = description;
			group.update();
			return ok(addModalSuccess.render());
		}
	}
	
	public static Result delete(Long id) {
		Group group = Group.findById(id);
		group.delete();
		flash("message", "Group " + group.title + " deleted!");
		return redirect(routes.GroupController.index());
	}

	public static Result deletePost(Long id) {
		try {
			if (Secured.isOwnerOfPost(id)) {
				Post p = Post.findById(id);
				long currentUserId = Long.parseLong(session().get("id"));
				p.delete(currentUserId);
				flash("message", "Post deleted");
			} else {
				return forbidden();
			}
		} catch (NumberFormatException exp) {
			// TODO Log exception
			flash("message", "Couldn't delete post");
		}

		return redirect(routes.GroupController.index());
	}
}

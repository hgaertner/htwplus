package controllers;

import java.util.List;

import play.*;
import play.mvc.*;
import play.data.*;
import models.Account;
import models.Course;
import models.Group;
import models.Post;
import views.html.Group.*;
import views.html.Group.snippets.*;
import play.db.jpa.*;

@Security.Authenticated(Secured.class)
@Transactional
@With(Common.class)
public class GroupController extends Controller {

	static Form<Group> groupForm = Form.form(Group.class);
	
	static Form<Post> postForm = Form.form(Post.class);

	public static Result index() {
		return ok(index.render(Group.all()));
	}
	
	public static Result indexByAccount(Long accountId) {
		Account account = Account.findById(accountId);
		return ok(index.render(Group.allByAccount(account)));
	}
	
	public static Result addPost(long groupId) {
		// try{
		Form<Post> filledForm = postForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			flash("message", "Error in Form!");
			// return badRequest(view.render(arg0, arg1));
		} else {
			Post p = filledForm.get();
			try{
				final long accountId = Long.parseLong(session().get("id"));
				Post p2 = new Post();
				p2.content = p.content;
				//if(currentCourse.members.contains(currentUser)){
					p2.owner = JPA.em().find(Account.class, accountId);
					p2.group = JPA.em().find(Group.class, groupId);
					p2.create();
				//} else {
					//TODO return error Message current user is not a member of the course...
				//}
			}catch(NumberFormatException exp){
				//TODO log exception and handle it...
			}
		}
		return view(groupId);
	}
	

	public static Result view(Long id) {
		Group group = Group.findById(id);
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			Form<Post> formPost = Form.form(Post.class);
			
			List<Post> posts = JPA.em()
			  .createQuery("SELECT p FROM Post p WHERE p.group.id = ?1")
			 .setParameter(1, id).getResultList();
			return ok(view.render(group,posts,postForm));
		}
	}
	
	public static Result create() {
		Account account = Account.findByEmail(session().get("email"));
		Form<Group> filledForm = groupForm.bindFromRequest();		
		if (filledForm.hasErrors()) {
			flash("message", "Error in Form!");
			return ok(addModal.render(filledForm));
		} else {
			Group g = filledForm.get();
			g.create(account);
			flash("message", "Created new Group!");
			return ok(addModalSuccess.render());
		}
	}

	public static Result edit(Long id) {
		Group group = Group.findById(id);
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			return ok(edit.render(group.id, groupForm.fill(group)));
		}
	}
	
	public static Result update(Long id) {
		Group group = Group.findById(id);
		Form<Group> filledForm = groupForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			flash("message", "Error in Form!");
			return badRequest(edit.render(id, filledForm));
		} else {
			
			Logger.info(filledForm.get().description);
			group.title = filledForm.get().title;
			group.description = filledForm.get().description;
			group.isClosed = filledForm.get().isClosed;
			group.update(id);
			flash("message", "Updated Group!");
			return redirect(routes.GroupController.index());
		}
	}

	public static Result add() {
		return ok(add.render(groupForm));
	}


	public static Result delete(Long id) {
		Group group = Group.findById(id);
		group.delete();
		flash("message", "Group " + group.title + " deleted!");
		return redirect(routes.GroupController.index());
	}
	
	public static Result deletePost(Long id){
		try{
			Post p = Post.findById(id);
			long currentUserId = Long.parseLong(session().get("id"));
			p.delete(currentUserId);
			flash("message", "Post deleted");
		}catch (NumberFormatException exp){
			//TODO Log exception
			flash("message", "Couldn't delete post");
		}
			
		return redirect(routes.GroupController.index());
	}
}

package controllers;

import java.util.List;

import models.Account;
import models.Course;
import models.Post;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.Course.add;
import views.html.Course.edit;
import views.html.Course.index;
import views.html.Course.view;

@Security.Authenticated(Secured.class)
@Transactional
public class CourseController extends Controller {

	static Form<Course> courseForm = Form.form(Course.class);

	static Form<Post> postForm = Form.form(Post.class);

	public static Result index() {
		return ok(index.render(Course.all()));
	}

	public static Result add() {
		return ok(add.render(courseForm));
	}

	public static Result addPost(long courseId) {
		// try{
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
				// if(currentCourse.members.contains(currentUser)){
				p2.owner = JPA.em().find(Account.class, accountId);
				p2.course = JPA.em().find(Course.class, courseId);
				p2.create();
				// } else {
				// TODO return error Message current user is not a member of the
				// course...
				// }
			} catch (NumberFormatException exp) {
				// TODO log exception and handle it...
			}
		}
		return view(courseId);
	}

	public static Result create() {
		Form<Course> filledForm = courseForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			flash("message", "Error in Form!");
			return badRequest(add.render(filledForm));
		} else {
			Course c = filledForm.get();
			c.createByUser(ctx().session().get("email"));
			flash("message", "Created new Course!");
			return redirect(routes.CourseController.index());
		}
	}

	@Transactional
	public static Result view(Long id) {
		Course course = Course.findById(id);
		if (course == null) {
			return redirect(routes.CourseController.index());
		} else {

			List<Post> posts = JPA.em()
					.createQuery("SELECT p FROM Post p WHERE p.course.id = ?1")
					.setParameter(1, id).getResultList();

			return ok(view.render(course, postForm, posts));
		}
	}

	public static Result edit(Long id) {
		Course course = Course.findById(id);
		if (course == null) {
			return redirect(routes.CourseController.index());
		} else {
			return ok(edit.render(course.id, courseForm.fill(course)));
		}
	}

	public static Result update(Long id) {
		if (Secured.isOwnerOfCourse(id, ctx())){
			Course course = Course.findById(id);
			Form<Course> filledForm = courseForm.bindFromRequest();
			if (filledForm.hasErrors()) {
				flash("message", "Error in Form!");
				return badRequest(edit.render(id, filledForm));
			} else {
				filledForm.get().update(id);
				flash("message", "Updated Course!");
				return redirect(routes.CourseController.index());
			}
		}else{
			return forbidden();
		}
	}

	public static Result delete(Long id) {
		if(Secured.isOwnerOfCourse(id, ctx())){
			Course course = Course.findById(id);
			course.delete();
			flash("message", "Course " + course.title + " deleted!");
			return redirect(routes.CourseController.index());
		}else {
			return forbidden();
		}
	}

	public static Result deletePost(Long id) {
		try {
			if(Secured.isOwnerOfPost(id, ctx()) || Secured.isOwnerOfCourse(id, ctx())){
				Post post = Post.findById(id);
				long currentUser = Long.parseLong(session().get("id"));
				post.delete(currentUser);
				flash("message", "Post deleted!");
			}else {
				return forbidden();
			}
		} catch (NumberFormatException exp) {
			// Log exception here
			flash("message", "Could not delete post");
		}
		return redirect(routes.CourseController.index());
	}
}

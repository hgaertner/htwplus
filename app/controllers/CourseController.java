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
public class CourseController extends BaseController {

	static Form<Course> courseForm = Form.form(Course.class);

	static Form<Post> postForm = Form.form(Post.class);

	public static Result index() {
		return ok(index.render(Course.all()));
	}

	public static Result add() {
		return ok(add.render(courseForm));
	}

	public static Result addPost(long courseId) {
		if (Secured.isMemberOfCourse(courseId)) {
			Form<Post> filledForm = postForm.bindFromRequest();
			if (filledForm.hasErrors()) {
				flash("error", "Error in Form!");
				// return badRequest(view.render(arg0, arg1));
			} else {
				Post p = filledForm.get();
				try {
					final long accountId = Long.parseLong(session().get("id"));
					Post p2 = new Post();
					p2.content = p.content;
					// if(currentCourse.members.contains(currentUser)){
					p2.owner = Account.findById(accountId);
					p2.course = Course.findById(courseId);
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
		return view(courseId);
	}

	public static Result create() {
		Form<Course> filledForm = courseForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			flash("error", "Error in Form!");
			return badRequest(add.render(filledForm));
		} else {
			Course c = filledForm.get();
			c.createByUser(Component.currentAccount());
			flash("success", "Created new Course!");
			return redirect(routes.CourseController.index());
		}
	}

	@Transactional
	public static Result view(Long id) {
		Course course = Course.findById(id);
		if (course == null) {
			return redirect(routes.CourseController.index());
		} else {
			List<Post> posts = Post.getPostForCourse(id);
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
		if (Secured.isOwnerOfCourse(id)) {
			Course course = Course.findById(id);
			Form<Course> filledForm = courseForm.bindFromRequest();
			if (filledForm.hasErrors()) {
				flash("error", "Error in Form!");
				return badRequest(edit.render(id, filledForm));
			} else {
				filledForm.get().update();
				flash("success", "Updated Course!");
				return redirect(routes.CourseController.index());
			}
		} else {
			return forbidden();
		}
	}

	@Transactional
	public static Result delete(Long id) {
		if (Secured.isOwnerOfCourse(id)) {
			Course course = Course.findById(id);
			course.delete();
			flash("info", "Course " + course.title + " deleted!");
			return redirect(routes.CourseController.index());
		} else {
			return forbidden();
		}
	}
}

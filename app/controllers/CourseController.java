package controllers;

import java.util.List;

import org.hibernate.metamodel.source.binder.JpaCallbackClass;

import play.*;
import play.mvc.*;
import play.cache.Cache;
import play.data.*;
import models.*;
import views.html.Course.*;
import play.db.jpa.*;
import play.mvc.Security;

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
			try{
				final long accountId = Long.parseLong(session().get("id"));
				Post p2 = new Post();
				p2.content = p.content;
				//if(currentCourse.members.contains(currentUser)){
					p2.owner = Account.findById(accountId);
					p2.course = Course.findById(courseId);
					p2.create();
				//} else {
					//TODO return error Message current user is not a member of the course...
				//}
			}catch(NumberFormatException exp){
				//TODO log exception and handle it...
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
			c.create();
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
	}

	@Transactional
	public static Result delete(Long id) {
		Course course = Course.findById(id);
		course.delete();
		flash("message", "Course " + course.title + " deleted!");
		return redirect(routes.CourseController.index());
	}
	
	@Transactional
	public static Result deletePost(Long id){
		try{
			Post post = Post.findById(id);
			long currentUser = Long.parseLong(session().get("id"));
			post.delete(currentUser);
			flash("message", "Post deleted!");
		}catch (NumberFormatException exp){
			//Log exception here
			flash("message", "Could not delete post");
		}
		return redirect(routes.CourseController.index());
	}
}

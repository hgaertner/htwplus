
package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import models.*;
import views.html.Course.*;
import play.db.jpa.*;
import scala.reflect.internal.Trees.This;

public class CourseController extends Controller {
  
	static Form<Course> courseForm = Form.form(Course.class);	
	
    @Transactional(readOnly=true)
	public static Result index() {
		return ok(index.render(Course.all()));
	}
    
    public static Result add() {
    	return ok(add.render(courseForm));
    }
    
    @Transactional
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
    
    @Transactional(readOnly=true)	
    public static Result view(Long id) {
    	Course course = Course.findById(id);
    	if(course == null) {
    		return redirect(routes.CourseController.index());
    	} else {
        	return ok(view.render(course));
    	}
    }
    
    @Transactional
    public static Result edit(Long id) {
    	Course course = Course.findById(id);
    	if(course == null) {
    		return redirect(routes.CourseController.index());
    	} else {
        	return ok(edit.render(course.id, courseForm.fill(course)));
    	}
    }
    
    @Transactional
    public static Result update(Long id) {
    	Course course = Course.findById(id);
    	Form<Course> filledForm = courseForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			flash("message", "Error in Form!");
			return badRequest(edit.render(id ,filledForm));
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
    
  
}


package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import models.*;
import views.html.Course.*;
import play.db.jpa.*;
import scala.reflect.internal.Trees.This;

public class AccountController extends Controller {
	
    @Transactional
    public static Result create(String name) {
    	Account u = new Account();
    	u.loginname = name;
    	u.create();
    	return redirect(routes.CourseController.index());
    }
	
	
}
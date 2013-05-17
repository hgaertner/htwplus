package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import models.Account;
import models.Group;
import views.html.Group.*;
import play.db.jpa.*;

@Security.Authenticated(Secured.class)
public class GroupController extends Controller {

	static Form<Group> groupForm = Form.form(Group.class);

	@Transactional(readOnly = true)
	public static Result index() {
		return ok(index.render(Group.all(),Account.findByEmail(request().username())));
	}

	@Transactional(readOnly = true)
	public static Result view(Long id) {
		Group group = Group.findById(id);
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			return ok(view.render(group));
		}
	}

	@Transactional
	public static Result create() {
		Form<Group> filledForm = groupForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			flash("message", "Error in Form!");
			return badRequest(add.render(filledForm));
		} else {
			Group g = filledForm.get();
			g.create();
			flash("message", "Created new Group!");
			return redirect(routes.CourseController.index());
		}
	}

	@Transactional
	public static Result edit(Long id) {
		Group group = Group.findById(id);
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			return ok(edit.render(group.id, groupForm.fill(group)));
		}
	}

	@Transactional
	public static Result update(Long id) {
		Group group = Group.findById(id);
		Form<Group> filledForm = groupForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			flash("message", "Error in Form!");
			return badRequest(edit.render(id, filledForm));
		} else {
			filledForm.get().update(id);
			flash("message", "Updated Group!");
			return redirect(routes.GroupController.index());
		}
	}

	public static Result add() {
		return ok(add.render(groupForm));
	}

	@Transactional
	public static Result delete(Long id) {
		Group group = Group.findById(id);
		group.delete();
		flash("message", "Group " + group.title + " deleted!");
		return redirect(routes.GroupController.index());
	}
}

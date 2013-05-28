package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import models.Account;
import models.Group;
import views.html.Group.*;
import play.db.jpa.*;

@Security.Authenticated(Secured.class)
@Transactional
@With(Common.class)
public class GroupController extends Controller {

	static Form<Group> groupForm = Form.form(Group.class);

	public static Result index() {
		return ok(index.render(Group.all()));
	}
	
	public static Result indexByAccount(Long accountId) {
		Account account = Account.findById(accountId);
		return ok(index.render(Group.allByAccount(account)));
	}
	
	public static Result view(Long id) {
		Group group = Group.findById(id);
		if (group == null) {
			return redirect(routes.GroupController.index());
		} else {
			return ok(view.render(group));
		}
	}

	public static Result create() {
		Account account = Account.findByEmail(session().get("email"));
		Form<Group> filledForm = groupForm.bindFromRequest();
		System.out.println(filledForm.errors());
		if (filledForm.hasErrors()) {
			flash("message", "Error in Form!");
			return badRequest(add.render(filledForm));
		} else {
			Group g = filledForm.get();
			g.create(account);
			flash("message", "Created new Group!");
			return redirect(routes.GroupController.index());
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
}

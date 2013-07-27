package controllers;

import models.Account;
import play.cache.Cache;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Profile.*;

@Transactional
public class ProfileController extends BaseController {

	static Form<Account> accountForm = Form.form(Account.class);
	
	public static Result me() {
		Account account = Component.currentAccount();
		if (account == null) {
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.Application.index());
		} else {
			return ok(index.render(account, accountForm.fill(account)));
			//return ok(index.render(account));
		}
	}

	public static Result view(Long id) {
		Account account = Account.findById(id);
		if (account == null) {
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.Application.index());
		} else {
			return ok(index.render(account, accountForm.fill(account)));
			//return ok(index.render(account));
		}
	}

	public static Result edit(Long id) {
		Account account = Account.findById(id);
		if (account == null) {
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.Application.index());
		} else {
			return ok(index.render(account, accountForm.fill(account)));
			//return ok(edit.render(account.id, accountForm.fill(account)));
		}
	}

	public static Result update(Long id) {
		if (Secured.isOwnerOfAccount(id, ctx())) {
			Account account = Account.findById(id);
			Form<Account> filledForm = accountForm.bindFromRequest();
			System.out.println(filledForm.errors());
			if (filledForm.hasErrors()) {
				flash("error", "Error in Form!");
				return badRequest(index.render(account, filledForm));
				//return badRequest(edit.render(id, filledForm));
			} else {
				filledForm.get().update();
				flash("info", "Profile updated!");
				return redirect(routes.Application.index());
			}
		} else {
			return forbidden();
		}
	}

}

package controllers;

import models.Account;
import play.cache.Cache;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Profile.*;

@Transactional
public class ProfileController extends Controller {
	
	static Form<Account> accountForm = Form.form(Account.class);
	
	public static Result view(Long id) {
    	Account account = Account.findById(id);
    	if(account == null) {
    		flash("message", "Dieses Profil gibt es nicht.");
    		return redirect(routes.Application.index());
    	} else {
        	return ok(index.render(account));
    	}
    }
	
	public static Result edit(Long id) {
    	Account account = Account.findById(id);
    	if(account == null) {
    		flash("message", "Dieses Profil gibt es nicht.");
    		return redirect(routes.Application.index());
    	} else {
        	return ok(edit.render(account.id, accountForm.fill(account)));
    	}
    }
	
	public static Result update(Long id) {
    	Account account = Account.findById(id);
    	Form<Account> filledForm = accountForm.bindFromRequest();
    	System.out.println(filledForm.errors());
		if (filledForm.hasErrors()) {
			flash("message", "Error in Form!");
			return badRequest(edit.render(id ,filledForm));
		} else {
			filledForm.get().update(id);
			flash("message", "Profile updated!");
			return redirect(routes.Application.index());
		}		
    }
	
}

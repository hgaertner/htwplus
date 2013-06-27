package controllers;

import static play.data.Form.form;
import models.Account;
import models.Login;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.*;
import views.html.index;
import views.html.snippets.*;

@Transactional
public class AccountController extends BaseController {
	
    /**
     * Defines a form wrapping the Account class.
     */ 
    final static Form<Account> signupForm = form(Account.class);
  
	
	public static Result authenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			flash("success", "Nutzer oder Passwort nicht korrekt");
			return badRequest(index.render());
		} else {
			session().clear();
			session("email", loginForm.get().email);
			session("id", Account.findByEmail(loginForm.get().email).id.toString());
			session("firstname", Account.findByEmail(loginForm.get().email).firstname);
			return redirect(routes.Application.index());
		}
	}

	/**
	 * Logout and clean the session.
	 */
	public static Result logout() {
		session().clear();
		flash("success", "You've been logged out");
		return redirect(routes.Application.index());
	}
	  
    /**
     * Handle the form submission.
     */
    @Transactional
    public static Result submit() {
        Form<Account> filledForm = signupForm.bindFromRequest();
        System.out.println(filledForm.errors());
        // Check Mail
        if(!(Account.findByEmail(filledForm.field("email").value()) == null)) {
            filledForm.reject("email", "Mail is already taken!");
        }
        // Check repeated password
        if(!filledForm.field("password").valueOr("").isEmpty()) {
            if(!filledForm.field("password").valueOr("").equals(filledForm.field("repeatPassword").value())) {
                filledForm.reject("repeatPassword", "Password don't match");
            }
        }
                
        if(filledForm.hasErrors()) {
        	
            return ok(signup.render(filledForm));
        } else {
            Account created = filledForm.get();
            created.create();
            return ok(signupSuccess.render());
        }
    }
	
}
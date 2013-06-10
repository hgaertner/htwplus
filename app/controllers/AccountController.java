package controllers;

import static play.data.Form.form;
import models.Account;
import models.Login;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.*;
import views.html.index;
import views.html.signup.form;
import views.html.signup.summary;

@Transactional
public class AccountController extends BaseController {
	
    /**
     * Defines a form wrapping the Account class.
     */ 
    final static Form<Account> signupForm = form(Account.class);
  
	
	public static Result authenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			flash("success", "Error in Form");
			return badRequest(index.render());
		} else {
			session().clear();
			session("email", loginForm.get().email);
			session("id", Account.findByEmail(loginForm.get().email).id.toString());
			session("firstname", Account.findByEmail(loginForm.get().email).firstname);
			return redirect(routes.Application.stream());
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
     * Display a blank form.
     */ 
    public static Result blank() {
        return ok(form.render(signupForm));
    }
  
  
    /**
     * Handle the form submission.
     */
    @Transactional
    public static Result submit() {
        Form<Account> filledForm = signupForm.bindFromRequest();
        System.out.println(filledForm.errors());
        // Check accept conditions
        if(!"true".equals(filledForm.field("accept").value())) {
            filledForm.reject("accept", "You must accept the terms and conditions");
        }
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
        	
            return badRequest(form.render(filledForm));
        } else {
            Account created = filledForm.get();
            created.create();
            return ok(summary.render(created));
        }
    }
	
}
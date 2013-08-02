package controllers;

import static play.data.Form.form;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import models.Account;
import models.Login;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;
import views.html.index;
import views.html.snippets.signup;
import views.html.snippets.signupSuccess;

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
            filledForm.reject("email", "Diese Mail wird bereits verwendet!");
        }
        // Check repeated password
        if(!filledForm.field("password").valueOr("").isEmpty()) {
            if(!filledForm.field("password").valueOr("").equals(filledForm.field("repeatPassword").value())) {
                filledForm.reject("repeatPassword", "Passwörter stimmen nicht überein");
            }
        }
        if(filledForm.field("password").value().length() < 6){
        	filledForm.reject("password", "Das Passwort muss mindestens 6 Zeichen haben.");
        }
                
        if(filledForm.hasErrors()) {
        	
            return ok(signup.render(filledForm));
        } else {
            Account created = filledForm.get();
            created.password = Component.md5(created.password);
            Random generator = new Random();
            created.avatar = "a" + generator.nextInt(10);
            created.create();
            return ok(signupSuccess.render());
        }
    }
    

	
}
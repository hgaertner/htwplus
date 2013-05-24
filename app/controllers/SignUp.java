package controllers;

import javax.persistence.NoResultException;

import org.hibernate.exception.ConstraintViolationException;

import views.html.*;

import play.*;
import play.mvc.*;
import play.data.*;
import play.db.jpa.Transactional;
import static play.data.Form.*;

import views.html.signup.*;

import models.*;

public class SignUp extends Controller {
    
    /**
     * Defines a form wrapping the Account class.
     */ 
    final static Form<Account> signupForm = form(Account.class);
  
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
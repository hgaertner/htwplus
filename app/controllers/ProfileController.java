package controllers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import models.Account;
import models.Friendship;
import models.Post;
import models.Studycourse;

import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.data.Form;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import views.html.Profile.edit;
import views.html.Profile.index;
import views.html.Profile.stream;
import views.html.Profile.editPassword;
import controllers.Navigation.Level;

@Transactional
@Security.Authenticated(Secured.class)
public class ProfileController extends BaseController {

	static Form<Account> accountForm = Form.form(Account.class);
	static Form<Post> postForm = Form.form(Post.class);

	public static Result me() {
		Navigation.set(Level.PROFILE);
		Account account = Component.currentAccount();
		if (account == null) {
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.Application.index());
		} else {
			return ok(index.render(account, postForm));
			// return ok(index.render(account));
		}
	}

	public static Result view(final Long id) {
		Account account = Account.findById(id);

		if (account == null) {
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.Application.index());
		} else {
			Navigation.set(Level.FRIENDS, "Profil", account.name,
					routes.ProfileController.view(account.id));
			return ok(index.render(account, postForm));
			// return ok(index.render(account));
		}
	}

	public static Result stream(Long accountId) {
		Account account = Account.findById(accountId);
		Account currentUser = Component.currentAccount();

		if (currentUser.equals(account)) {
			Navigation.set(Level.PROFILE, "Newsstream");
		} else {
			Navigation.set(Level.FRIENDS, "Newsstream", account.name,
					routes.ProfileController.view(account.id));
		}

		// case for friends and own profile
		if (Friendship.alreadyFriendly(Component.currentAccount(), account)
				|| Component.currentAccount().equals(account)) {
			return ok(stream.render(account, Post.getFriendStream(account),
					postForm));
		}
		// case for visitors
		flash("info", "Du kannst nur den Stream deiner Freunde betrachten!");
		return redirect(routes.ProfileController.view(accountId));
	}

	public static Result editPassword(Long id) {
		Account account = Account.findById(id);
		Navigation.set(Level.PROFILE, "Editieren");
		return ok(editPassword.render(account, accountForm.fill(account)));
	}

	public static Result updatePassword(Long id) {
		// Get regarding Object
		Account account = Account.findById(id);
		
		// Create error switch
		Boolean error = false;
		
		// Check Access
		if(!Secured.editAccount(account)) {
			return redirect(routes.Application.index());
		}
		
		// Get data from request
		Form<Account> filledForm = accountForm.bindFromRequest();
		
		
		// Remove all unnecessary fields
		filledForm.errors().remove("firstname");
		filledForm.errors().remove("lastname");
		filledForm.errors().remove("email");

		// Store old and new password for validation
		String oldPassword = filledForm.field("oldPassword").value();
		String password = filledForm.field("password").value();
		String repeatPassword = filledForm.field("repeatPassword").value();
		
		// Perform JPA Validation
		if(filledForm.hasErrors()) {
			error = true;
		}
		
		// Custom Validations
		if (!oldPassword.isEmpty()) {
			if (!account.password.equals(Component.md5(oldPassword))) {
				filledForm.reject("oldPassword", "Dein altes Passwort ist nicht korrekt.");
				error = true;
			}
		} else {
			filledForm.reject("oldPassword","Bitte gebe Dein altes Passwort ein.");
			error = true;
		}

		if (password.length() < 6) {
			filledForm.reject("password", "Das Passwort muss mindestens 6 Zeichen haben.");
			error = true;
		}

		if (!password.equals(repeatPassword)) {
			filledForm.reject("repeatPassword", "Die Passwörter stimmen nicht überein.");
			error = true;
		}

		if (error) {
			return badRequest(editPassword.render(account, filledForm));
		} else {
			account.password = Component.md5(password);
			account.update();
			flash("success", "Passwort erfolgreich geändert.");
		}
		return redirect(routes.ProfileController.me());
	}

	public static Result edit(Long id) {
		Account account = Account.findById(id);
		if (account == null) {
			flash("info", "Dieses Profil gibt es nicht.");
			return redirect(routes.Application.index());
		} else {
			Navigation.set(Level.PROFILE, "Editieren");
			return ok(edit.render(account, accountForm.fill(account)));
		}
	}

	public static Result update(Long id) {
		// Get regarding Object
		Account account = Account.findById(id);
		
		// Check Access
		if(!Secured.editAccount(account)) {
			return redirect(routes.Application.index());
		}
		
		// Get the data from the request
		Form<Account> filledForm = accountForm.bindFromRequest();
		
		Navigation.set(Level.PROFILE, "Editieren");
	
		// Remove expected errors
		filledForm.errors().remove("password");
		filledForm.errors().remove("studycourse");

		// Custom Validations
		Account exisitingAccount = Account.findByEmail(filledForm.field("email").value());
		if (exisitingAccount != null && !exisitingAccount.equals(account)) {
			filledForm.reject("email", "Diese Mail wird bereits verwendet!");
			return badRequest(edit.render(account, filledForm));
		}
		
		// Perform JPA Validation
		if(filledForm.hasErrors()) {
			return badRequest(edit.render(account, filledForm));
		} else {

			// Fill an and update the model manually 
			// because the its just a partial form
			account.firstname = filledForm.field("firstname").value();
			account.lastname = filledForm.field("lastname").value();
			account.avatar = filledForm.field("avatar").value();
			account.email = filledForm.field("email").value();

			if (filledForm.field("degree").value().equals("null")) {
				account.degree = null;
			} else {
				account.degree = filledForm.field("degree").value();
			}

			if (filledForm.field("semester").value().equals("0")) {
				account.semester = null;
			} else {
				account.semester = Integer.parseInt(filledForm.field("semester").value());
			}

			Long studycourseId = Long.parseLong(filledForm.field("studycourse").value());
			Studycourse studycourse;
			if (studycourseId != 0) {
				studycourse = Studycourse.findById(studycourseId);
			} else {
				studycourse = null;
			}
			account.studycourse = studycourse;
			account.update();
		
			flash("success", "Profil erfolgreich gespeichert.");
			return redirect(routes.ProfileController.me());
		}
	}

}

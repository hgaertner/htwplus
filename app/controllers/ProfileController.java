package controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;

import models.Account;
import models.Friendship;
import models.Post;
import models.Studycourse;

import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.EmailValidator;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import views.html.Profile.index;
import views.html.Profile.stream;
import views.html.Profile.snippets.editForm;
import views.html.Profile.snippets.passwordForm;
import views.html.Profile.edit;
import controllers.Navigation.Level;

@Transactional
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
		return ok(passwordForm.render(account, accountForm.fill(account)));
	}

	public static Result updatePassword(Long id) {
		Account account = Account.findById(id);
		Form<Account> filledForm = accountForm.bindFromRequest();
		ObjectNode result = Json.newObject();
		Boolean error = false;

		Set<String> checkErrorSet = new HashSet<String>();
		checkErrorSet.add("password");

		String oldPassword = filledForm.field("oldPassword").value();
		String password = filledForm.field("password").value();
		String repeatPassword = filledForm.field("repeatPassword").value();

		if (!oldPassword.isEmpty()) {
			if (!account.password.equals(Component.md5(oldPassword))) {
				filledForm.reject("oldPassword",
						"Dein altes Passwort ist nicht korrekt.");
				error = true;
			}
		} else {
			filledForm.reject("oldPassword",
					"Bitte gebe dein altes Passwort ein.");
			error = true;
		}

		if (password.length() < 6) {
			filledForm.reject("password",
					"Das Passwort muss mindestens 6 Zeichen haben.");
			error = true;
		}

		if (!password.equals(repeatPassword)) {
			filledForm.reject("repeatPassword",
					"Die Passwörter stimmen nicht überein.");
			error = true;
		}

		Set<String> errorSet = filledForm.errors().keySet();
		if (!Collections.disjoint(errorSet, checkErrorSet)) {
			error = true;
		}

		if (error) {
			result.put("status", "response");
			String form = passwordForm.render(account, filledForm).toString();
			result.put("payload", form);
		} else {
			account.password = Component.md5(password);
			account.update();
			result.put("status", "redirect");
			result.put("url", routes.ProfileController.me().toString());
			flash("success", "Passwort erfolgreich geändert.");
		}
		return ok(result);
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
		Account account = Account.findById(id);
		Form<Account> filledForm = accountForm.bindFromRequest();
	
		// Does Email exists already
		Account exisitingAccount = Account.findByEmail(filledForm
				.field("email").value());
		if (exisitingAccount != null && !exisitingAccount.equals(account)) {
			filledForm.reject("email", "Diese Mail wird bereits verwendet!");
			Navigation.set(Level.PROFILE, "Editieren");
			return badRequest(edit.render(account, filledForm.fill(account)));
		}

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
			account.semester = Integer.parseInt(filledForm
					.field("semester").value());
		}

		Long studycourseId = Long.parseLong(filledForm.field("studycourse")
				.value());
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

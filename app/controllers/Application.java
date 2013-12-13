package controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Account;
import models.Group;
import models.Post;
import play.Logger;
import play.Play;
import play.Routes;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import views.html.error;
import views.html.help;
import views.html.searchresult;
import views.html.stream;
import controllers.Navigation.Level;


@Transactional
public class Application extends BaseController {
	
	static Form<Post> postForm = Form.form(Post.class);
	static final int LIMIT = Integer.parseInt(Play.application().configuration().getString("htwplus.post.limit"));
	static final int PAGE = 1;
	
	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter("jsRoutes",
				controllers.routes.javascript.GroupController.create(),
				controllers.routes.javascript.GroupController.update(),
				controllers.routes.javascript.AccountController.submit()
				));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result index() {
		Navigation.set(Level.STREAM);
		Account currentAccount = Component.currentAccount();
		return ok(stream.render(currentAccount,Post.getStream(currentAccount, LIMIT, PAGE),postForm,Post.countStream(currentAccount), LIMIT, PAGE));
	}
	
	public static Result help() {
		Navigation.set(Level.HELP);
		return ok(help.render());
	}
	
	@Security.Authenticated(Secured.class)
	public static Result stream(int page) {
		Navigation.set(Level.STREAM);
		Account currentAccount = Component.currentAccount();
		return ok(stream.render(currentAccount,Post.getStream(currentAccount, LIMIT, page),postForm,Post.countStream(currentAccount), LIMIT, page));
	}
	
	public static Result search(){
		List<Group> groupResults = null;
		List<Group> courseResults = null;
		List<Account> accResults = null;
		String keyword = "";
		final Set<Map.Entry<String, String[]>> entries = request()
				.queryString().entrySet();
		for (Map.Entry<String, String[]> entry : entries) {
			if (entry.getKey().equals("keyword")) {
				keyword = entry.getValue()[0];
				Logger.info("Keyword: " +keyword.isEmpty());
				Navigation.set("Suchergebnisse");
				courseResults = Group.searchForCourseByKeyword(keyword, true);
				groupResults = Group.searchForGroupByKeyword(keyword, true);
				accResults = Account.searchForAccountByKeyword(keyword, true);
				Logger.info("Sizes: " + "Groups: " +groupResults.size() + " Courses: " + courseResults.size() + " Account: " +accResults.size());
			}

		}

		return ok(searchresult.render(groupResults, courseResults, accResults, keyword));
	}
	
	public static Result searchForAccounts(final String keyword){
		List<Account> accounts = null;
		accounts = Account.searchForAccountByKeyword(keyword, false);
		return ok(searchresult.render(null, null, accounts,null));
	}
	
	public static Result searchForGroups(final String keyword){
		Logger.info("Keyword: " +keyword);
		List<Group> groups = null;
		groups = Group.searchForGroupByKeyword(keyword, false);
		return ok(searchresult.render(groups, null, null,null));
	}
	
	public static Result searchForCourses(final String keyword){
		List<Group> courses = null;
		courses = Group.searchForCourseByKeyword(keyword, false);
		return ok(searchresult.render(null, courses, null,null));
	}
	
	public static Result error() {
		Navigation.set("404");
		return ok(error.render());
	}
		
	public static Result defaultRoute(String path) {
		Logger.info(path+" nicht gefunden");
		return redirect(routes.Application.index());
	}

}

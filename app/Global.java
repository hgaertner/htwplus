import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import controllers.Component;
import controllers.routes;
import models.Account;
import models.Group;
import models.enums.AccountRole;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.Play;
import play.db.jpa.JPA;
import play.mvc.Http.RequestHeader;


public class Global extends GlobalSettings {

	@Override
	public void beforeStart(Application app) {
		Logger.info(" Global beforeStart");
		super.beforeStart(app);
	}

	@Override
	public void onStart(Application app) {
		Logger.info("Global - onStart");
		super.onStart(app);
		
		//Doesn't work!!!
		InitialData.insert(app);
	}
	
	@Override
	public play.mvc.Result onError(RequestHeader rh, Throwable t) {
		Logger.info("onError "+ rh + " " + t);
		
		// prod mode? return 404 page
		if(Play.mode(play.api.Play.current()).toString().equals("Prod")){
			return play.mvc.Results.redirect(routes.Application.error());
		}

		return super.onError(rh, t);
	}
	

	static class InitialData {

		public static void insert(Application app) {
			//create account if none exists
			
			JPA.withTransaction(new play.libs.F.Callback0() {
				
				@Override
				public void invoke() throws Throwable {
					Account admin = Account.findByEmail("admin@htwplus.de");
					if(admin == null){
						admin = new Account();
						admin.email = "admin@htwplus.de";
						admin.firstname = "Admin";
						admin.lastname = "Plus";
						admin.role = AccountRole.ADMIN;
						admin.avatar = "a1";
						admin.password = Component.md5("123456");
						admin.create();
					}
					
					Account account = null;
					if (Account.all().size() <= 0) {
						account = new Account();
						account.email = "test@example.de";
						account.firstname = "test";
						account.lastname = "test";
						account.avatar = "a1";
						account.role = AccountRole.STUDENT;
						account.password = Component.md5("123456");
						account.create();	
					}else {
						account = Account.all().get(0);
					}
					//Generate indexes
					FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(JPA.em());
					try {
						fullTextEntityManager.createIndexer(Group.class, Account.class).startAndWait();
					
					} catch (InterruptedException e) {
						
						Logger.error(e.getMessage());
					}
					
				}
			});
			
		}

	}
}

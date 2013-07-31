import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import controllers.Component;
import models.Account;
import models.Group;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;


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

	static class InitialData {

		public static void insert(Application app) {
			//create account if none exists
			
			JPA.withTransaction(new play.libs.F.Callback0() {
				
				@Override
				public void invoke() throws Throwable {
					Account account = null;
					if (Account.all().size() <= 0) {
						account = new Account();
						account.email = "test@example.de";
						account.firstname = "test";
						account.lastname = "test";
						account.password = Component.md5("123456");
						account.create();
					}else {
						account = Account.all().get(0);
					}
					
					FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(JPA.em());
					try {
						fullTextEntityManager.createIndexer(Group.class).startAndWait();
					} catch (InterruptedException e) {
						
						Logger.error(e.getMessage());
					}
					
				}
			});
			
		}

	}
}

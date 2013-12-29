import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import controllers.Component;
import controllers.routes;
import models.Account;
import models.Group;
import models.Post;
import models.enums.AccountRole;
import models.enums.GroupType;
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
	
		InitialData.insert(app);
	}
	
	@Override
	public play.mvc.Result onError(final RequestHeader rh, final Throwable t) {
		Logger.error("onError "+ rh + " " + t);
				
		JPA.withTransaction(new play.libs.F.Callback0() {
			
			@Override
			public void invoke() throws Throwable {
				Group group = Group.findByTitle("HTWplus");
				if(group != null){
					Post p = new Post();
					p.content = "Request: "+rh+"\nError: "+t;
					p.owner = Account.findByEmail("admin@htwplus.de");
					p.group = group;
					p.create();
				}
			}
		});
		
		
		// prod mode? return 404 page
		if(Play.mode(play.api.Play.current()).toString().equals("Prod")){
			return play.mvc.Results.redirect(routes.Application.error());
		}

		return super.onError(rh, t);
	}
	

	static class InitialData {

		public static void insert(Application app) {
			
			// Do some inital db stuff
			JPA.withTransaction(new play.libs.F.Callback0() {
				
				@Override
				public void invoke() throws Throwable {
					
					//create Admin account if none exists
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
					
					// create Admin group if none exists
					Group group = Group.findByTitle("HTWplus");
					if(group == null && admin != null){
						group = new Group();
						group.title = "HTWplus";
						group.groupType = GroupType.close;
						group.description = "reserved ...";
						group.createWithGroupAccount(admin);
					}
					// Generate indexes
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

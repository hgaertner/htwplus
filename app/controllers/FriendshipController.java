package controllers;

import java.util.List;

import javax.transaction.RollbackException;

import models.Account;
import models.Friendship;
import models.Group;
import models.GroupAccount;
import models.enums.LinkType;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import views.html.Friends.*;

@Security.Authenticated(Secured.class)
@Transactional
public class FriendshipController extends BaseController {
	
	public static Result index() {
		List<Account> allAccounts = Account.findAll();
		List<Account> friends = Friendship.findFriends(Component.currentAccount());
		List<Friendship> requests = Friendship.findRequests(Component.currentAccount());
		return ok(index.render(friends,requests,allAccounts));
	}
	
	public static Result requestFriend(long friendId){
		Account me = Component.currentAccount();
		Account potentialFriend = Account.findById(friendId);
		
		Friendship friendship = new Friendship(me,potentialFriend,LinkType.request);
		
		if(Friendship.findRequest(me,potentialFriend) != null){
			flash("info","Deine Freundschaftsanfrage wurde bereits verschickt!");
			return redirect(routes.FriendshipController.index());
		}
		
		if(Friendship.alreadyFriendly(me,potentialFriend)){
			flash("info","Ihr seid bereits Freunde!");
			return redirect(routes.FriendshipController.index());
		}
		
		friendship.create();

		flash("success","Deine Einladung wurde verschickt!");
		
		return redirect(routes.FriendshipController.index());
	}
	
	public static Result acceptFriendRequest(long friendId){
		Account account = Component.currentAccount();
		Account potentialFriend = Account.findById(friendId);
		
		// establish connection based on three actions
		// first, check if currentAccount got an request
		Friendship requestLink = Friendship.findRequest(potentialFriend,account);
		
		if(requestLink == null){
			flash("info","Es gab keine Freundschaftsanfrage von diesem User");
			return redirect(routes.FriendshipController.index());
		} else{
			// if so: set LinkType from request to friend
			requestLink.setLinkType(LinkType.friend);
			requestLink.update();
			
			// and create new friend-connection between currentAccount and requester
			new Friendship(account,potentialFriend,LinkType.friend).create();
			
			flash("success","Freundschaft erfolgreich hergestellt!");
		}
		
		
		
		return redirect(routes.FriendshipController.index());
	}

}

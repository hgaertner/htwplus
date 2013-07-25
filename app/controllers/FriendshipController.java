package controllers;

import java.util.List;

import models.Account;
import models.Friendship;
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
		Account currentUser = Component.currentAccount();
		List<Account> friends = Friendship.findFriends(currentUser);
		
		// find requests and add rejects to simplify view output
		List<Friendship> requests = Friendship.findRequests(currentUser);
		requests.addAll(Friendship.findRejects(currentUser));
		
		return ok(index.render(friends,requests,allAccounts));
	}
	
	public static Result requestFriend(long friendId){
		Account currentUser = Component.currentAccount();
		Account potentialFriend = Account.findById(friendId);
		
		if(hasLogicalErrors(currentUser,potentialFriend)){
			return redirect(routes.FriendshipController.index());
		}
		
		Friendship friendship = new Friendship(currentUser,potentialFriend,LinkType.request);
		friendship.create();

		flash("success","Deine Einladung wurde verschickt!");
		
		return redirect(routes.FriendshipController.index());
	}
	
	public static Result deleteFriend(long friendId){
		Account currentUser = Component.currentAccount();
		Account friend = Account.findById(friendId);
		
		if(friend == null){
			flash("error","Diesen User gibt es nicht!");
			return redirect(routes.FriendshipController.index());
		}
		
		Friendship friendshipLink = Friendship.findFriendLink(currentUser, friend);
		Friendship reverseLink = Friendship.findFriendLink(friend, currentUser);
		
		if(friendshipLink == null || reverseLink == null){
			flash("error","Diese Freundschaft besteht nicht!");
		} else {
			friendshipLink.delete();
			reverseLink.delete();
		}
		
		return redirect(routes.FriendshipController.index());
	}
	
	public static Result acceptFriendRequest(long friendId){
		Account currentUser = Component.currentAccount();
		Account potentialFriend = Account.findById(friendId);
		
		// establish connection based on three actions
		// first, check if currentAccount got an request
		Friendship requestLink = Friendship.findRequest(potentialFriend,currentUser);
		
		if(requestLink == null){
			flash("info","Es gibt keine Freundschaftsanfrage von diesem User");
			return redirect(routes.FriendshipController.index());
		} else{
			// if so: set LinkType from request to friend
			requestLink.linkType = LinkType.friend;
			requestLink.update();
			
			// and create new friend-connection between currentAccount and requester
			new Friendship(currentUser,potentialFriend,LinkType.friend).create();
			
			flash("success","Freundschaft erfolgreich hergestellt!");
		}
		
		
		
		return redirect(routes.FriendshipController.index());
	}
	
	public static Result declineFriendRequest(long friendshipId){
		Friendship requestLink = Friendship.findById(friendshipId);
		if(requestLink != null){
			requestLink.linkType = LinkType.reject;
			requestLink.update();
		}

		return redirect(routes.FriendshipController.index());
	}
	
	public static Result cancelFriendRequest(long friendshipId){
		
		Friendship friendship = Friendship.findById(friendshipId);
		if(friendship != null && friendship.account.equals(Component.currentAccount())){
			friendship.delete();
		} else {
			flash("error","Diese Freundschaftsanfrage gibt es nicht!");
		}
		
		return redirect(routes.FriendshipController.index());
	}
	
	private static boolean hasLogicalErrors(Account currentUser, Account potentialFriend) {
		if(potentialFriend.equals(currentUser)){
			flash("info","Du kannst nicht mit dir befreundet sein!");
			return true;
		}
		
		if(Friendship.findRequest(currentUser,potentialFriend) != null){
			flash("info","Deine Freundschaftsanfrage wurde bereits verschickt!");
			return true;
		}
		
		if(Friendship.alreadyFriendly(currentUser,potentialFriend)){
			flash("info","Ihr seid bereits Freunde!");
			return true;
		}
		return false;
	}

}

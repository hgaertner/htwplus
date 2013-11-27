package models;

import java.util.*;

import play.Logger;
import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;

import models.base.BaseModel;

import org.hibernate.validator.constraints.Length;

import play.db.jpa.*;
import views.html.Group.view;

@Entity
public class Post extends BaseModel {
	
	public static String GROUP = "group";
	public static String PROFILE = "profile";
	public static String STREAM = "stream";

	@Required
	@Column(length=2000)
	public String content;

	@ManyToOne
	public Post parent;

	@ManyToOne
	public Group group;

	@ManyToOne
	public Account account;

	@ManyToOne
	public Account owner;
		
	public void create() {
		JPA.em().persist(this);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete() {
		// delete all comments first
		List<Post> comments = getCommentsForPost(this.id, 0, 0);
		
		for(Post comment : comments){
			comment.delete();
		}
		
		JPA.em().remove(this);
	}
	
	protected static Query limit(Query query, int limit, int offset) {
		if (limit > 0) {
			query.setMaxResults(limit);
		}
		if (offset >= 0) {
			query.setFirstResult(offset);
		}
		return query;
	}
		
	public static Post findById(Long id) {
		return JPA.em().find(Post.class, id);
	}

	@SuppressWarnings("unchecked")
	public static List<Post> getPostForGroup(Group group) {
		return (List<Post>) JPA.em()
				.createQuery("SELECT p FROM Post p WHERE p.group.id = ?1 ORDER BY p.createdAt DESC")
				.setParameter(1, group.id)
				.getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<Post> getCommentsForPost(Long id, int start, int max) {	
		return (List<Post>) JPA.em()
				.createQuery("SELECT p FROM Post p WHERE p.parent.id = ?1 ORDER BY p.createdAt ASC")
				.setParameter(1, id)
				.setFirstResult(start)
				.setMaxResults(max)
				.getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<Post> findStreamForAccount(Account account, List<Group> groupList, List<Account> friendList, boolean isVisitor, int limit, int offset){
		
		Query query = streamForAccount("SELECT p ", account, groupList, friendList, isVisitor, "ORDER BY p.id DESC");

		// set limit and offset
		query = limit(query, limit, offset);
		List<Post> posts = query.getResultList();
		return posts;
	}
	
	public static int countStreamForAccount(Account account, List<Group> groupList, List<Account> friendList, boolean isVisitor){
		
		Query query = streamForAccount("SELECT COUNT(p)", account, groupList, friendList, isVisitor,"");
		
		int count = ((Number) query.getSingleResult()).intValue();
		
		return count;
	}
	
	/**
	 * @author Iven
	 * @param account - Account (current user, profile or a friend)
	 * @param groupList - a list containing all groups of given account
	 * @param friendList - a list containing all friends of given account
	 * @return List of Posts
	 */
	@SuppressWarnings("unchecked")
	public static Query streamForAccount(String selectClause, Account account, List<Group> groupList, List<Account> friendList, boolean isVisitor, String orderByClause){
		// since JPA is unable to handle empty lists (eg. groupList, friendList) we need to assemble our query.
		String groupListClause = "";
		String friendListClause = "";
		String visitorClause = "";
		
		// add additional clauses if not null or empty
		if(groupList != null && !groupList.isEmpty()){
			groupListClause = " OR p.group IN :groupList ";
		}
		if(friendList != null && !friendList.isEmpty()){
			friendListClause = " OR p.account IN :friendList ";
		}
		if(isVisitor){
			visitorClause = " AND p.owner = :currentAccount ";
		}
		
		// create Query. 
		Query query = JPA.em().createQuery(selectClause+" FROM Post p WHERE p.account = :currentAccount "+visitorClause+groupListClause+friendListClause+orderByClause);
		query.setParameter("currentAccount", account);
		
		
		// add parameter as needed
		if(groupList != null && !groupList.isEmpty()){
			query.setParameter("groupList", groupList);
		}
		if(friendList != null && !friendList.isEmpty()){
			query.setParameter("friendList", friendList);
		}
		
		return query;
		
	}
	
	public static int countCommentsForPost(Long id) {
		return ((Number)JPA.em().createQuery("SELECT COUNT(p.id) FROM Post p WHERE p.parent.id = ?1").setParameter(1, id).getSingleResult()).intValue();
	}
	
	public int getCountComments() {
		return Post.countCommentsForPost(this.id);
	}
	
	public boolean belongsToGroup(){
		if(this.group != null) return true;
		return false;
	}
		
	public boolean belongsToAccount(){
		if(this.account != null) return true;
		return false;
	}

	/**
	 * @author Iven
	 * @param account - Account (usually current user)
	 * @return List of Posts
	 */
	public static List<Post> getStream(Account account, int limit, int page) {
		// find friends and groups of given account
		List<Account> friendList = Friendship.findFriends(account);
		List<Group> groupList = GroupAccount.findEstablished(account);
		
		int offset = (page * limit) - limit;
		return findStreamForAccount(account, groupList, friendList, false, limit, offset);
	}
	
	/**
	 * @author Iven
	 * @param account - Account (usually current user)
	 * @return 
	 * @return Number of Posts
	 */
	public static int countStream(Account account){
		// find friends and groups of given account
		List<Account> friendList = Friendship.findFriends(account);
		List<Group> groupList = GroupAccount.findEstablished(account);
			
		return countStreamForAccount(account, groupList, friendList, false);
	}
	
	/**
	 * @author Iven
	 * @param account - Account (usually a friends)
	 * @return List of Posts
	 */
	public static List<Post> getFriendStream(Account friend, int limit, int page) {
		// find friends and non closed-groups of given account
		List<Account> friendList = Friendship.findFriends(friend);
		List<Group> groupList = GroupAccount.findPublicEstablished(friend);
			
		int offset = (page * limit) - limit;
		return findStreamForAccount(friend, groupList, friendList, false, limit, offset);
	}
	
	/**
	 * @author Iven
	 * @param account - Account (usually a friend)
	 * @return 
	 * @return Number of Posts
	 */
	public static int countFriendStream(Account account){
		// find friends and groups of given account
		List<Group> groupList = GroupAccount.findPublicEstablished(account);
		List<Account> friendList = Friendship.findFriends(account);
		
		return countStreamForAccount(account, groupList, friendList, false);
	}
}
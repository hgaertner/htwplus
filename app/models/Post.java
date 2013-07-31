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
	public static String COURSE = "course";
	public static String STREAM = "stream";

	@Required
	@Column(length=2000)
	public String content;

	@ManyToOne
	public Post parent;

	@ManyToOne
	public Group group;
	@ManyToOne
	public Course course;
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
		
	public static Post findById(Long id) {
		return JPA.em().find(Post.class, id);
	}

	@SuppressWarnings("unchecked")
	public static List<Post> getPostForCourse(Long courseId) {
		return (List<Post>) JPA.em()
				.createQuery("SELECT p FROM Post p WHERE p.course.id = ?1 ORDER BY p.createdAt DESC")
				.setParameter(1, courseId).getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<Post> getPostForGroup(Group group) {
		return (List<Post>) JPA.em()
				.createQuery("SELECT p FROM Post p WHERE p.group.id = ?1 ORDER BY p.createdAt DESC")
				.setParameter(1, group.id)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Post> getPostForAccount(Account account) {
		return (List<Post>) JPA.em()
				.createQuery("SELECT p FROM Post p WHERE p.account.id = ?1 ORDER BY p.createdAt DESC")
				.setParameter(1, account.id)
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
	
	/**
	 * @author Iven
	 * @param account - Account (usually currentUser)
	 * @param groupList - a list containing all groups of currentAccount
	 * @param friendList - a list containing all friends of currentAccount
	 * @return List of Posts
	 */
	@SuppressWarnings("unchecked")
	public static List<Post> findStreamForAccount(Account account, List<Group> groupList, List<Account> friendList){
		// since JPA is unable to handle empty lists (eg. groupList, friendList) we need to assemble our query.
		String groupListClause = "";
		String friendListClause = "";
		
		// add additional clauses if not empty
		if(!groupList.isEmpty()){
			groupListClause = " OR p.group IN :groupList ";
		}
		if(!friendList.isEmpty()){
			friendListClause = " OR p.account IN :friendList ";
		}
		
		// create Query
		Query query = JPA.em().createQuery("SELECT p FROM Post p WHERE p.account = :currentAccount "+groupListClause+friendListClause+" ORDER BY p.createdAt DESC");
		query.setParameter("currentAccount", account);
		
		// add parameter as needed
		if(!groupList.isEmpty()){
			query.setParameter("groupList", groupList);
		}
		if(!friendList.isEmpty()){
			query.setParameter("friendList", friendList);
		}
		
		List<Post> posts = query.getResultList();
		return posts;
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
	
	public boolean belongsToCourse(){
		if(this.course != null) return true;
		return false;
	}
	
	public boolean belongsToAccount(){
		if(this.account != null) return true;
		return false;
	}

	/**
	 * @author Iven
	 * @param account - Account (usually currentUser)
	 * @return List of Posts
	 */
	public static List<Post> getStream(Account account) {
		// find friends, groups and course of given account
		List<Account> friendList = Friendship.findFriends(account);
		List<Group> groupList = GroupAccount.findEstablished(account);
			
		return findStreamForAccount(account, groupList, friendList);
	}
}
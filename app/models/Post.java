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

	public static List<Post> getStream(Account account) {
		// Create empty ArrayList
		List<Post> posts = new ArrayList<Post>();
		
		// Add posts for given Account
		posts.addAll(getPostForAccount(account));
		
		// Add posts from all friend of this account
		for(Account friend : Friendship.findFriends(account)){
			posts.addAll(getPostForAccount(friend));
		}
		
		// Add posts from all groups of this account
		for(Group group : GroupAccount.findEstablished(account)){
			posts.addAll(getPostForGroup(group));
		}
		
		return posts;
	}
}
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
@SequenceGenerator(name = "default_seq", sequenceName = "post_seq")
public class Post extends BaseModel {

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

	@Override
	public void create() {
		JPA.em().persist(this);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
	}

	public static Post findById(Long id) {
		return JPA.em().find(Post.class, id);
	}

	public void delete(long userId) {
		if (this.owner.id == userId) {
			JPA.em().remove(this);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Post> getPostForCourse(Long courseId) {
		return (List<Post>) JPA.em()
				.createQuery("SELECT p FROM Post p WHERE p.course.id = ?1")
				.setParameter(1, courseId).getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<Post> getPostForGroup(Long id) {
		return (List<Post>) JPA.em()
				.createQuery("SELECT p FROM Post p WHERE p.group.id = ?1 ORDER BY p.createdAt")
				.setParameter(1, id)
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
	
	public static boolean isOwner(Long postId, Account account) {
		Post p = JPA.em().find(Post.class, postId);
		if(p.owner.equals(account)){
			return true;
		} else {
			return false;
		}
	}

}
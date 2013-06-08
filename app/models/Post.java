package models;

import java.util.*;

import play.Logger;
import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;

import models.base.BaseModel;

import org.hibernate.validator.constraints.Length;

import play.db.jpa.*;

@Entity
@SequenceGenerator(name = "default_seq", sequenceName = "post_seq")
public class Post extends BaseModel {
	
	@Required
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
	public void update(Long id) {
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
		if(this.owner.id == userId){
			JPA.em().remove(this);
		}
	}

	public static boolean isOwner(Long postId, String email) {
		Post p = JPA.em().find(Post.class, postId);
		if(p.owner.email.equals(email)){
			return true;
		} else {
			return false;
		}
	}

}
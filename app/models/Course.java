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
public class Course extends BaseModel {

	@Required
	public String title;

	public String description;

	@ManyToOne
	public Account owner;

	@ManyToMany
	public Set<Account> members;

	@Required
	@Length(min = 4, max = 45)
	public String token;
	
	@OneToMany(mappedBy="course")
	@OrderBy("createdAt DESC")
	public List<Media> media;

	public static List<Course> all() {
		List<Course> courses = JPA.em().createQuery("SELECT t FROM Course t")
				.getResultList();
		return courses;
	}

	public static Course findById(Long id) {
		return JPA.em().find(Course.class, id);
	}

	
	public void createByUser(final Account account) {
		this.owner = account;
		JPA.em().persist(this);
	}

	@Override
	public void update() {
		// createdAt seems to be overwritten (null) - quickfix? (Iven)
		this.createdAt = findById(id).createdAt;
		JPA.em().merge(this);
	}

	@Override
	public void delete() {
		JPA.em().remove(this);
	}

	public static boolean isOwner(Long courseId, Account account) {
		Course course = JPA.em().find(Course.class, courseId);
		if (course.owner.equals(account)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isMember(Long courseId, Account account) {
		
		Course course = JPA.em().find(Course.class, courseId);
		if (course.members.contains(account)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void create() {
		// TODO Auto-generated method stub
		
	}
}
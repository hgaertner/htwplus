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
@SequenceGenerator(name = "default_seq", sequenceName = "course_seq")
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

	public static List<Course> all() {
		List<Course> courses = JPA.em().createQuery("SELECT t FROM Course t")
				.getResultList();
		return courses;
	}

	public static Course findById(Long id) {
		return JPA.em().find(Course.class, id);
	}

	
	public void createByUser(final String email) {
		Account a = (Account) JPA
				.em()
				.createQuery("SELECT a FROM Account a WHERE a.email = ?1")
				.setParameter(1, email).getSingleResult();
		this.owner = a;
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

	public static boolean isOwner(Long courseId, String email) {
		Course course = JPA.em().find(Course.class, courseId);
		if (course.owner.email.equals(email)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isMember(Long courseId, String email) {
		Account a = (Account) JPA.em()
				.createQuery("SELECT a FROM ACCOUNT a WHERE a.email = ?1")
				.setParameter(1, email).getSingleResult();
		Course course = JPA.em().find(Course.class, courseId);
		if (course.members.contains(a)) {
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
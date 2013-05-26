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
		List<Course> courses = JPA.em().createQuery("SELECT t FROM Course t").getResultList();
		return courses;
	}

	public static Course findById(Long id) {
		return JPA.em().find(Course.class, id);
	}

	@Override
	public void create() {
		JPA.em().persist(this);
	}
	
	@Override
	public void update(Long id) {
		this.id = id;
		// createdAt seems to be overwritten (null) - quickfix? (Iven)
		this.createdAt = findById(id).createdAt;
		JPA.em().merge(this);
	}
	
	@Override
	public void delete() {
		JPA.em().remove(this);
	}
	
 }
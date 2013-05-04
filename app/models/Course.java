package models;

import java.util.*;
import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;
import org.hibernate.validator.constraints.Length;
import play.db.jpa.*;

@Entity
@SequenceGenerator(name = "course_seq", sequenceName = "course_seq")
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_seq")
	public Long id;
	
	@Required
	public String title;
	
	public String description;
	
	@ManyToOne(cascade = CascadeType.MERGE)
	public Account owner;
	
	//public String ownerId;
	//public String member;

	public Date createdAt;
	
	public Date updatedAt;
	
	@Required
	@Length(min = 4, max = 45)
	public String token;
	
	
	@PrePersist
	void createdAt() {
		this.createdAt = this.updatedAt = new Date();
	}

	@PreUpdate
	void updatedAt() {
		this.updatedAt = new Date();
	}
	
	public static List<Course> all() {
		List<Course> courses = JPA.em().createQuery("SELECT t FROM Course t").getResultList();
		return courses;
	}
	
	public static Course findById(Long id) {
		return JPA.em().find(Course.class, id);
	}
	
	public void create() {
		JPA.em().persist(this);
	}
	
	public void update(Long id) {
		this.id = id;
		JPA.em().merge(this);
	}
	
	public void delete() {
		JPA.em().remove(this);
	}
	
}
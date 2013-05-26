package models.base;

import java.util.*;
import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import models.Course;

import org.hibernate.annotations.Columns;
import org.hibernate.validator.constraints.Length;
import play.db.jpa.*;

@MappedSuperclass
public abstract class BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_seq")
	public Long id;
	
	@Column(name = "created_at")
	public Date createdAt;
	@Column(name = "updated_at")
	public Date updatedAt;
		
	@PrePersist
	void createdAt() {
		this.createdAt = this.updatedAt = new Date();
	}

	@PreUpdate
	void updatedAt() {
		this.updatedAt = new Date();
	}
	
	public abstract void create();
		
	public abstract void update(Long id);
	
	public abstract void delete();
	
	
}
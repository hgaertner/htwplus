package models;

import java.util.*;
import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;

import models.base.BaseModel;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import play.db.jpa.*;

@Entity
@SequenceGenerator(name = "default_seq", sequenceName = "media_seq")
public class Media extends BaseModel {

	@Required
	public String title;
	
	public String description;
	
	@URL
	public String url;

	@Required
	public String mimetype;
	
	@Required
	public Long size;
	
	@ManyToOne
	public Group group;
	
	@ManyToOne
	public Course course;
	
	@Override
	public void create() {
		JPA.em().persist(this);
	}
	
	@Override
	public void update(Long id) {
		this.id = id;
		JPA.em().merge(this);
	}
	
	@Override
	public void delete() {
		JPA.em().remove(this);
	}
	
}
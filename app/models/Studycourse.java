package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OrderBy;

import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;

import models.base.BaseModel;

@Entity
public class Studycourse extends BaseModel {
	
	@Required
	@Column(length=2000)
	public String title;

	@Override
	public void create() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		
	}
	
	public static Studycourse findById(Long id) {
		return JPA.em().find(Studycourse.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Studycourse> getAll() {
		List<Studycourse> courses = JPA.em().createQuery("FROM Studycourse").getResultList();
		return courses;
	}
	
}
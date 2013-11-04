package models;

import java.util.*;

import play.Logger;
import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;

import models.base.BaseModel;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.validator.constraints.Length;
import play.db.jpa.*;


@Indexed
@Entity
public class Course extends BaseModel {

	@Required
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
	
	@SuppressWarnings("unchecked")
	public static List<Course> searchByKeyword(final String keyword){
		Logger.info("Course model searchByKeyword: " + keyword.toLowerCase());
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(JPA.em());
		//Create a querybuilder for the group entity 
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(Course.class).get();
		//Sets the field we want to search on and tries to match with the given keyword
		org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword().wildcard().onField("title").matching("*"+keyword.toLowerCase()+"*").createQuery();
		// wrap Lucene query in a javax.persistence.Query
		FullTextQuery fullTextQuery = fullTextEntityManager
				.createFullTextQuery(luceneQuery, Course.class);

		List<Course> result = fullTextQuery.getResultList(); //The result...
		Logger.info("Found " +result.size() +" courses with keyword: " +keyword);


		return result;
	}
}
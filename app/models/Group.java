package models;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import models.base.BaseModel;
import models.enums.GroupType;
import models.enums.LinkType;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;

@Indexed
@Entity
@Table(name = "Group_")
public class Group extends BaseModel {

	@Required
	@Column(unique = true)
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	public String title;

	public String description;

	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
	public Set<GroupAccount> groupAccounts;

	@ManyToOne
	public Account owner;

	@Enumerated(EnumType.STRING)
	public GroupType groupType;

	public String token;

	@OneToMany(mappedBy = "group")
	@OrderBy("createdAt DESC")
	public List<Media> media;

	// GETTER & SETTER

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Account getOwner() {
		return owner;
	}

	public void setOwner(Account owner) {
		this.owner = owner;
	}

	public List<Media> getMedia() {
		return media;
	}

	public void setMedia(List<Media> media) {
		this.media = media;
	}

	public String validate() {
		if (Group.findByTitle(this.title) != null) {
			return "Der Titel ist bereits vergeben";
		}
		return null;
	}

	public void createWithGroupAccount(Account account) {
		this.owner = account;
		this.create();
		GroupAccount groupAccount = new GroupAccount(account, this,
				LinkType.establish);
		groupAccount.create();

	}

	@Override
	public void create() {
		JPA.em().persist(this);
	}

	@Override
	public void update() {
		// this.id = id;
		// createdAt seems to be overwritten (null) - quickfix? (Iven)
		// this.createdAt = findById(id).createdAt;
		JPA.em().merge(this);
	}

	@Override
	public void delete() {
		// delete all Posts
		List<Post> posts = Post.getPostForGroup(this);
		for (Post post : posts) {
			post.delete();
		}

		// delete media
		for (Media media : this.media) {
			media.delete();
		}
		JPA.em().remove(this);
	}

	public static Group findById(Long id) {
		return JPA.em().find(Group.class, id);
	}

	public static Group findByTitle(String title) {
		@SuppressWarnings("unchecked")
		List<Group> groups = (List<Group>) JPA.em()
				.createQuery("FROM Group g WHERE g.title = ?1")
				.setParameter(1, title).getResultList();

		if (groups.isEmpty()) {
			return null;
		} else {
			return groups.get(0);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Group> all() {
		List<Group> groups = JPA.em().createQuery("FROM Group").getResultList();
		return groups;
	}

	public static boolean isMember(Group group, Account account) {
		@SuppressWarnings("unchecked")
		List<GroupAccount> groupAccounts = (List<GroupAccount>) JPA
				.em()
				.createQuery(
						"SELECT g FROM GroupAccount g WHERE g.account.id = ?1 and g.group.id = ?2 AND linkType = ?3")
				.setParameter(1, account.id).setParameter(2, group.id)
				.setParameter(3, LinkType.establish).getResultList();

		if (groupAccounts.isEmpty()) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * Search for a group with a given keyword.
	 * 
	 * @param keyword
	 * @return List of groups wich matches with the keyword
	 */
	@SuppressWarnings("unchecked")
	public static List<Group> searchForGroupByKeyword(String keyword, final boolean setMaxResult) {
		Logger.info("Group model searchForGroupByKeyword: "
				+ keyword.toLowerCase());
		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(JPA.em());

		// Create a querybuilder for the group entity
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(Group.class).get();
		org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword()
				.wildcard().onField("title")
				.matching("*" + keyword.toLowerCase() + "*").createQuery();
		//Create a criteria because we just want to search for groups
		Session session = fullTextEntityManager
				.unwrap(org.hibernate.Session.class);
		Criteria criteria = session.createCriteria(Group.class);
		criteria.add(Restrictions.or(
				Restrictions.eq("groupType", GroupType.open),
				Restrictions.eq("groupType", GroupType.close)));
		criteria.addOrder(Order.asc("title"));
		
		// wrap Lucene query in a javax.persistence.Query
		FullTextQuery fullTextQuery = fullTextEntityManager
				.createFullTextQuery(luceneQuery, Group.class);
		if(setMaxResult){
			criteria.setMaxResults(10); // Max result to 10
		}
		criteria.setReadOnly(true);
		fullTextQuery.setCriteriaQuery(criteria);
		fullTextQuery.setSort(new Sort(new SortField("title", SortField.STRING)));
		List<Group> result = fullTextQuery.getResultList(); // The result...
		session.clear();
		return result;
	}

	public static List<Group> searchForCourseByKeyword(String keyword, boolean setMaxResult) {
		Logger.info("Group model searchForCourseByKeyword: "
				+ keyword.toLowerCase());
		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(JPA.em());
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(Group.class).get();
		
		Session session = fullTextEntityManager
				.unwrap(org.hibernate.Session.class);
		
		Criteria courseCriteria = session.createCriteria(Group.class);
		courseCriteria.add(Restrictions.eq("groupType", GroupType.course));
		courseCriteria.addOrder(Order.asc("title"));
		org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword()
				.wildcard().onField("title")
				.matching("*" + keyword.toLowerCase() + "*").createQuery();
		// wrap Lucene query in a javax.persistence.Query
		FullTextQuery fullTextQuery = fullTextEntityManager
				.createFullTextQuery(luceneQuery, Group.class);
		if(setMaxResult) {
			courseCriteria.setMaxResults(10);
		}
		fullTextQuery.setCriteriaQuery(courseCriteria);
		List<Group> courses = fullTextQuery.getResultList();
		session.clear();
		return courses;
	}
}
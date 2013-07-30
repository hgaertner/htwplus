package models;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import models.GroupAccount;
import models.base.BaseModel;

import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
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

	public Boolean isClosed = false;

	@ManyToOne
	public Account owner;
	
	@OneToMany(mappedBy="group")
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
	
	public Boolean getIsClosed() {
		return isClosed;
	}

	public void setIsClosed(Boolean isClosed) {
		this.isClosed = isClosed;
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
	
	public String validate(){
		if(Group.findByTitle(this.title) != null){
			return "Der Titel ist bereits vergeben";
		}
		return null;
	}

	public void create(Account account) {
		this.owner = account;
		JPA.em().persist(this);
		GroupAccount groupAccount = new GroupAccount(account, this);
		groupAccount.approved = true;
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
		List<Post> posts = Post.getPostForGroup(this.id);
		for (Post post : posts) {
			post.delete();
		}
		
		// delete media
		for(Media media : this.media){
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
						"SELECT g FROM GroupAccount g WHERE g.account.id = ?1 and g.group.id = ?2 AND approved = TRUE")
				.setParameter(1, account.id).setParameter(2, group.id)
				.getResultList();

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
	public static List<Group> searchForGroupByKeyword(String keyword) {
		Logger.info("Group model searchForGroupByKeyWord: " + keyword);
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(JPA.em());
		/*try {
		 This part takes care to create indexes of persistent data, which is not inserted via hibernate/ JPA this block
		 is now in the onStart in Global.java
			fullTextEntityManager.createIndexer(Group.class).startAndWait();
		} catch (InterruptedException e) {
			
			Logger.error(e.getMessage());
		}*/
		//Create a querybuilder for the group entity 
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(Group.class).get();
		//Sets the field we want to search on and tries to match with the given keyword
		org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword()
				.onFields("title").matching(keyword).createQuery();
		// wrap Lucene query in a javax.persistence.Query
		FullTextQuery fullTextQuery = fullTextEntityManager
				.createFullTextQuery(luceneQuery, Group.class);

		List<Group> result = fullTextQuery.getResultList(); //The result...
		Logger.info("Found " +result.size() +" groups with keyword: " +keyword);
		

		return result;
	}
}
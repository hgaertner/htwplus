package models;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import models.base.BaseModel;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.impl.FullTextEntityManagerImpl;

import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;

@Indexed
@Entity
@SequenceGenerator(name = "default_seq", sequenceName = "group_seq")
@Table(name = "Group_")
public class Group extends BaseModel {

	@DocumentId // the equivalent of @Id for the Hibernate Search indexes
	@Required
	@Column(unique = true)
	public String title;

	public String description;

	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
	public Set<GroupAccount> groupAccounts;

	public Boolean isClosed;

	@ManyToOne
	public Account owner;

	@OneToMany(mappedBy = "group")
	public Set<Media> media;

	public String validate() {
		if (Group.findByTitle(this.title) != null) {
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

	public static boolean isMember(Long groupId, Account account) {
		@SuppressWarnings("unchecked")
		List<GroupAccount> groupAccounts = (List<GroupAccount>) JPA
				.em()
				.createQuery(
						"SELECT g FROM GroupAccount g WHERE g.account.id = ?1 and g.groupId= ?2 AND approved = TRUE")
				.setParameter(1, account.id).setParameter(2, groupId)
				.getResultList();

		if (groupAccounts.isEmpty()) {
			return false;
		} else {
			return true;
		}

	}

	@SuppressWarnings("unchecked")
	public static List<Group> searchForGroupByKeyword(String keyword) {
		//String selectString = "SELECT * FROM Group WHERE to_tsvector(group_.title) @@ to_tsquery('Closed')";
		//List<Group> result = JPA.em().createQuery(selectString).setParameter(1,keyword).getResultList();
		//return result;
		org.apache.lucene.search.Query luceneQuery = getLuceneQuery();
		FullTextEntityManager ftEm = new FullTextEntityManagerImpl(JPA.em());
		Query query = ftEm.createFullTextQuery(luceneQuery, Group.class);
	}

}
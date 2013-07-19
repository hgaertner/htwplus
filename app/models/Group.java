package models;

import java.util.*;

import play.Logger;
import play.api.data.validation.ValidationError;
import play.data.validation.Constraints.*;
import javax.persistence.*;
import models.base.BaseModel;
import play.db.jpa.*;

@Entity
@SequenceGenerator(name = "default_seq", sequenceName = "group_seq")
@Table(name = "Group_")
public class Group extends BaseModel {

	@Required
	@Column(unique=true)
	public String title;

	public String description;

	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
	public Set<GroupAccount> groupAccounts;

	public Boolean isClosed;

	@ManyToOne
	public Account owner;
	
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
		//this.id = id;
		// createdAt seems to be overwritten (null) - quickfix? (Iven)
		// this.createdAt = findById(id).createdAt;
		JPA.em().merge(this);
	}

	@Override
	public void delete() {
		// delete all Posts
		List<Post> posts = Post.getPostForGroup(this.id);
		for(Post post : posts){
			post.delete();
		}
		JPA.em().remove(this);
	}
		
	public static Group findById(Long id) {
		return JPA.em().find(Group.class, id);
	}
	
	public static Group findByTitle(String title) {
		@SuppressWarnings("unchecked")
		List<Group> groups =  (List<Group>) JPA.em().createQuery("FROM Group g WHERE g.title = ?1")
				.setParameter(1, title).getResultList();
		
		if(groups.isEmpty()) {
			return null;
		} else  {
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
		
		if(groupAccounts.isEmpty()) {
			return false;
		} else  {
			return true;
		}

	}
}
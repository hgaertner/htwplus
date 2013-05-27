package models;

import java.util.*;

import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;

import models.base.BaseModel;

import org.hibernate.validator.constraints.Length;
import play.db.jpa.*;

@Entity
@SequenceGenerator(name = "default_seq", sequenceName = "group_seq")
@Table(name = "Group_")
public class Group extends BaseModel {
	
	@Required
	public String title;
	
	public String description;
	
	@OneToMany(mappedBy = "group", cascade=CascadeType.ALL)
	public Set<GroupAccount> groupAccounts;
	
	public Boolean isClosed;
	
	public void create(Account account) {
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
	public void update(Long id) {
		this.id = id;
		// createdAt seems to be overwritten (null) - quickfix? (Iven)
		//this.createdAt = findById(id).createdAt;
		JPA.em().merge(this);
	}
	
	@Override
	public void delete() {
		JPA.em().remove(this);
	}

	public void addUserToGroup(Account user){
		//this.members.add(user);
		JPA.em().merge(this);
	}

	public static Group findById(Long id) {
		return JPA.em().find(Group.class, id);
	}

	public static List<Group> all() {
		List<Group> groups = JPA.em().createQuery("FROM Group").getResultList();
		return groups;
	}

	public static List<Group> allByAccount(Account account) {
		String query = "SELECT g.group FROM GroupAccount g WHERE g.account.id = " + account.id;
		List<Group> groups = JPA.em().createQuery(query).getResultList();
		return groups;
	}
	
}
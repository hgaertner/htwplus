package models;

import javax.persistence.*;
import play.db.jpa.*;

import models.ids.GroupAccountId;

@Entity
@IdClass(GroupAccountId.class)
@Table(name="group_account")
public class GroupAccount {
	
	@Id
	private Long groupId;
	@Id
	private Long accountId;
	
	@ManyToOne( optional = false )		
	@JoinColumn(name = "groupId", updatable = false, insertable = false)
	public Group group;
	
	@ManyToOne( optional = false )
	@JoinColumn(name = "accountId", updatable = false, insertable = false)
	public Account account;

	public Boolean approved;
	
	public GroupAccount() {

	}
	
	public GroupAccount(Account account, Group group) {
		this.account = account;
		this.accountId = account.id;
		this.group = group;
		this.groupId = group.id;
	}
	
	public static GroupAccount findById(Long id) {
		return JPA.em().find(GroupAccount.class, id);
	}
	
	public void create() {
		JPA.em().persist(this);
	}
	

}


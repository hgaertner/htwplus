package models;

import javax.persistence.*;

import play.db.jpa.*;
import java.util.List;

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
	
	/**
     * Retrieve Accounts from Group.
     */
    public static List<Account> findAccountsByGroup(Group group) {
    	@SuppressWarnings("unchecked")
    	List<Account> accounts = (List<Account>) JPA
				.em()
				.createQuery(
						"SELECT g.account FROM GroupAccount g WHERE g.group.id = ?1")
				.setParameter(1, group.id).getResultList();
		return accounts;
    }
	

}


package models;

import javax.persistence.*;

import play.db.jpa.*;

import java.util.Arrays;
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
	
	public void remove(){
		JPA.em().remove(this);
	}
	
	public void update() {
		JPA.em().merge(this);
	}
	
	/**
	 * Find all GroupAccounts where the given Account is Owner or Member
	 */
	public static List<GroupAccount> allByAccount(Account account) {
		@SuppressWarnings("unchecked")
		List<GroupAccount> groupAccounts = JPA
				.em()
				.createQuery(
						"SELECT g FROM GroupAccount g WHERE g.account.id = ?1 OR group.owner.id = ?1")
				.setParameter(1, account.id).getResultList();
		return groupAccounts;
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
    
    public static GroupAccount find(Account account, Group group) {
    	try {
    	return (GroupAccount) JPA
				.em()
				.createQuery(
						"SELECT g FROM GroupAccount g WHERE g.account.id = ?1 AND g.group.id = ?2")
				.setParameter(1, account.id).setParameter(2, group.id).getSingleResult();
    	}catch (NoResultException exp) {
	    	return null;
		}
    }
    
   
}


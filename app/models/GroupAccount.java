package models;

import javax.persistence.*;

import play.db.jpa.*;

import java.util.List;

import models.base.BaseModel;

@Entity
@Table(name="group_account", uniqueConstraints=
@UniqueConstraint(columnNames = {"account_id", "group_id"})) 
public class GroupAccount extends BaseModel{
	
	@ManyToOne( optional = false )		
	public Group group;
	
	@ManyToOne( optional = false )
	public Account account;

	public Boolean approved;
	
	public GroupAccount() {

	}
	
	public GroupAccount(Account account, Group group) {
		this.account = account;
		this.group = group;
	}
	
	public static GroupAccount findById(Long id) {
		return JPA.em().find(GroupAccount.class, id);
	}
	
	@Override
	public void create() {
		JPA.em().persist(this);
	}
	
	@Override
	public void update() {
		JPA.em().merge(this);
	}
	
	@Override
	public void delete() {
		JPA.em().remove(this);
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


package models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import play.db.jpa.*;

import java.util.List;

import models.base.BaseModel;
import models.enums.LinkType;

@Entity
@Table(name = "group_account", uniqueConstraints = @UniqueConstraint(columnNames = {
		"account_id", "group_id" }))
public class GroupAccount extends BaseModel {

	@ManyToOne(optional = false)
	public Group group;

	@ManyToOne(optional = false)
	public Account account;

	@Enumerated(EnumType.STRING)
	@NotNull
	public LinkType linkType;

	public GroupAccount() {

	}

	public GroupAccount(Account account, Group group, LinkType linkType) {
		this.account = account;
		this.group = group;
		this.linkType = linkType;
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
	 * Find all groups where given account is owner or member
	 */
	public static List<Group> findEstablished(Account account) {
		@SuppressWarnings("unchecked")
		List<Group> groupAccounts = JPA
				.em()
				.createQuery(
						"SELECT ga.group FROM GroupAccount ga WHERE ga.account.id = ?1 AND ga.linkType = ?2")
				.setParameter(1, account.id)
				.setParameter(2, LinkType.establish).getResultList();
		return groupAccounts;
	}
	
	/**
	 * Find all non private groups where given account is owner or member 
	 */
	public static List<Group> findPublicEstablished(Account account) {
		@SuppressWarnings("unchecked")
		List<Group> groupAccounts = JPA
				.em()
				.createQuery(
						"SELECT ga.group FROM GroupAccount ga WHERE ga.account.id = ?1 AND ga.linkType = ?2 AND ga.group.isClosed = FALSE")
				.setParameter(1, account.id)
				.setParameter(2, LinkType.establish).getResultList();
		return groupAccounts;
	}

	/**
	 * Find all requests and rejects for summarization under "Offene Anfragen"
	 * for given Account
	 * 
	 * @param account
	 * @return
	 */
	public static List<GroupAccount> findRequests(Account account) {
		@SuppressWarnings("unchecked")
		List<GroupAccount> groupAccounts = JPA
				.em()
				.createQuery(
						"SELECT ga FROM GroupAccount ga WHERE ((ga.group.owner.id = ?1 OR ga.account.id = ?1) AND ga.linkType = ?2) OR (ga.account.id = ?1 AND ga.linkType = ?3)")
				.setParameter(1, account.id).setParameter(2, LinkType.request)
				.setParameter(3, LinkType.reject).getResultList();
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
						"SELECT ga.account FROM GroupAccount ga WHERE ga.group.id = ?1 AND ga.linkType = ?2")
				.setParameter(1, group.id).setParameter(2, LinkType.establish)
				.getResultList();
		return accounts;
	}

	public static GroupAccount find(Account account, Group group) {
		try {
			return (GroupAccount) JPA
					.em()
					.createQuery(
							"SELECT ga FROM GroupAccount ga WHERE ga.account.id = ?1 AND ga.group.id = ?2")
					.setParameter(1, account.id).setParameter(2, group.id)
					.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

}

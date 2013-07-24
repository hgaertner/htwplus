package models;

import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import play.db.jpa.JPA;

import models.base.BaseModel;
import models.enums.LinkType;

@Entity
@Table(uniqueConstraints=
@UniqueConstraint(columnNames = {"account_id", "friend_id"})) 
public class Friendship extends BaseModel {
	
	@ManyToOne
	@NotNull
	private Account account;
	
	@ManyToOne
	@NotNull
	private Account friend;

	@Enumerated(EnumType.STRING)
	@NotNull
	private LinkType linkType;
	
	public Friendship(){
		
	}
	
	public Friendship(Account account, Account friend, LinkType type) {
		this.account = account;
		this.friend = friend;
		this.linkType = type;
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

	public static Friendship findRequest(Account me, Account potentialFriend) {
		try{
			return (Friendship) JPA.em().createQuery("SELECT fs FROM Friendship fs WHERE fs.account.id = ?1 and fs.friend.id = ?2 AND fs.linkType = ?3")
			.setParameter(1, me.id).setParameter(2, potentialFriend.id).setParameter(3, LinkType.request).getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}
	
	public static boolean alreadyFriendly(Account me, Account potentialFriend) {
		try {
			JPA.em().createQuery("SELECT fs FROM Friendship fs WHERE fs.account.id = ?1 and fs.friend.id = ?2 AND fs.linkType = ?3")
			.setParameter(1, me.id).setParameter(2, potentialFriend.id).setParameter(3, LinkType.friend).getSingleResult();
		} catch (NoResultException exp) {
	    	return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Account> findFriends(Account account){
		return (List<Account>) JPA.em().createQuery("SELECT fs.friend FROM Friendship fs WHERE fs.account.id = ?1 AND fs.linkType = ?2")
				.setParameter(1, account.id).setParameter(2, LinkType.friend).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Friendship> findRequests(Account account) {
		return (List<Friendship>) JPA.em().createQuery("SELECT fs FROM Friendship fs WHERE fs.linkType = ?2 AND (fs.friend.id = ?1 OR fs.account.id = ?1)")
				.setParameter(1, account.id).setParameter(2, LinkType.request).getResultList();
	}
	
	
	//
	// GETTER & SETTER
	//
	
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Account getFriend() {
		return friend;
	}

	public void setFriend(Account friend) {
		this.friend = friend;
	}

	public LinkType getLinkType() {
		return linkType;
	}

	public void setLinkType(LinkType linkType) {
		this.linkType = linkType;
	}



	
}
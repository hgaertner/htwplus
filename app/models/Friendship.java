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
	public Account account;
	
	@ManyToOne
	@NotNull
	public Account friend;

	@Enumerated(EnumType.STRING)
	@NotNull
	public LinkType linkType;
	
	public Friendship(){
		
	}
	
	public Friendship(Account account, Account friend, LinkType type) {
		this.account = account;
		this.friend = friend;
		this.linkType = type;
	}
	
	public static Friendship findById(Long id) {
		return JPA.em().find(Friendship.class, id);
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
	
	public static Friendship findFriendLink(Account account, Account target) {
		try{
			return (Friendship) JPA.em().createQuery("SELECT fs FROM Friendship fs WHERE fs.account.id = ?1 and fs.friend.id = ?2 AND fs.linkType = ?3")
			.setParameter(1, account.id).setParameter(2, target.id).setParameter(3, LinkType.establish).getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}
	
	public static boolean alreadyFriendly(Account me, Account potentialFriend) {
		try {
			JPA.em().createQuery("SELECT fs FROM Friendship fs WHERE fs.account.id = ?1 and fs.friend.id = ?2 AND fs.linkType = ?3")
			.setParameter(1, me.id).setParameter(2, potentialFriend.id).setParameter(3, LinkType.establish).getSingleResult();
		} catch (NoResultException exp) {
	    	return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Account> findFriends(Account account){
		return (List<Account>) JPA.em().createQuery("SELECT fs.friend FROM Friendship fs WHERE fs.account.id = ?1 AND fs.linkType = ?2")
				.setParameter(1, account.id).setParameter(2, LinkType.establish).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Friendship> findRequests(Account account) {
		return (List<Friendship>) JPA.em().createQuery("SELECT fs FROM Friendship fs WHERE (fs.friend.id = ?1 OR fs.account.id = ?1) AND fs.linkType = ?2")
				.setParameter(1, account.id).setParameter(2, LinkType.request).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Friendship> findRejects(Account account) {
		return (List<Friendship>) JPA.em().createQuery("SELECT fs FROM Friendship fs WHERE fs.account.id = ?1 AND fs.linkType = ?2")
				.setParameter(1, account.id).setParameter(2, LinkType.reject).getResultList();
	}
	
	
}
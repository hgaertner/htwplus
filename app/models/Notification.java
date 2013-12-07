package models;

import java.util.List;
import java.util.concurrent.Callable;
import java.lang.Exception;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NoResultException;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import play.libs.Akka;
import play.libs.F.Promise;
import models.base.BaseModel;

@Entity
@Table(uniqueConstraints=
@UniqueConstraint(columnNames = {
		"account_id", 
		"noteType", 
		"object_id"
		}))
public class Notification extends BaseModel {
	
	/**
	 * Each notification type is associated with a model,
	 * which ID is the objectId. Query the right entity by
	 * using the objectID for the corresponding model. 
	 */
	public enum NotificationType {
		GROUP_NEW_POST, // Group Model
		GROUP_NEW_MEDIA, // Group Model
		GROUP_NEW_REQUEST, // Group Model
		GROUP_REQUEST_SUCCESS, // Group Model
		GROUP_REQUEST_DECLINE, // Group Model
		POST_GROUP_NEW_COMMENT, // Post Model
		PROFILE_NEW_POST, // Account Model
		POST_PROFILE_NEW_COMMENT, // Post Model
		FRIEND_NEW_REQUEST, // Account Model
		FRIEND_REQUEST_SUCCESS, // Account Model
		FRIEND_REQUEST_DECLINE, // Account Model
	}
	
	@Required
	@OneToOne
	public Account account;
	
	@Required
	public NotificationType noteType;
	
	@Column(name = "object_id")
	public Long objectId;
	
	public static Notification findById(Long id) {
		return JPA.em().find(Notification.class, id);
	}
	
	//SINGLE NOTIFICATION
	public static void newNotification(NotificationType type, Long objectId, Account recipient) {
		if(Notification.findUnique(type, recipient, objectId) == null) {
			Notification notf = new Notification();
			notf.account = recipient;
			notf.noteType = type;
			notf.objectId = objectId;
			notf.create();
			Logger.info("Created new Notification for User: " + recipient.id.toString());
		}
	}
	
	// GROUP NOTIFICATIONS
	public static void newGroupNotification(NotificationType type, Group group, Account sender) {
		// Get all accounts for that group
    	List<Account> accounts =  GroupAccount.findAccountsByGroup(group);
	
//		Akka.future(
//		  new Callable<Void>() {
//		    public Void call() {
//		    	Logger.info("Async test");
//		    	Notification.newNotification(NotificationType.FRIEND_NEW_REQUEST, (long) 123, sender);
//		    	List<Account> accounts =  GroupAccount.findAccountsByGroup(group);
//		    	Logger.info("Size:" + String.valueOf(accounts.size()));
//				NotificationType type = noteType;
//
//		    	for (Account account : accounts) {
//					if(!account.equals(sender)){
//						if(Notification.findUnique(type, account, group.id) == null) {
//							Notification notf = new Notification();
//							notf.account = account;
//							notf.noteType = type;
//							notf.objectId = group.id;
//							notf.create();
//							Logger.info("Created new Notification for User: " + account.id.toString());
//						}
//					}
//				}
//				return null;
//		    }
//		  }
//		);
		
		for (Account account : accounts) {
			if(!account.equals(sender)){
				if(Notification.findUnique(type, account, group.id) == null) {
					Notification notf = new Notification();
					notf.account = account;
					notf.noteType = type;
					notf.objectId = group.id;
					notf.create();
					Logger.info("Created new Notification for User: " + account.id.toString());
				}
			}
		}
	}
	
	// GROUP POST NOTIFICATIONS
	public static void newPostNotification(NotificationType noteType, Post post, Account sender){
		Group group = post.group;
		List<Account> accounts =  GroupAccount.findAccountsByGroup(group);
		NotificationType type = noteType;
		
		for (Account account : accounts) {
			if(!account.equals(sender)){
				if(Notification.findUnique(type, account, post.id) == null) {
					Notification notf = new Notification();
					notf.account = account;
					notf.noteType = type;
					notf.objectId = post.id;
					notf.create();
					Logger.info("Created new Notification for User: " + account.id.toString());
				}
			}
		}

	}
	
	public static Notification findUnique(NotificationType type, Account account, Long objectId) {
    	try{
	    	return (Notification) JPA.em()
					.createQuery("from Notification n where n.noteType = :type AND n.account.id = :account AND n.objectId = :object")
					.setParameter("type", type)
					.setParameter("account", account.id)
					.setParameter("object", objectId)
					.getSingleResult();
	    } catch (NoResultException exp) {
	    	return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<Notification> findByUser(Account account) {
		return (List<Notification>) JPA.em()
				.createQuery("FROM Notification n WHERE n.account.id = :account")
				.setParameter("account", account.id)
				.getResultList();
	}
	
	public static void deleteByUser(Account account) {
		JPA.em().createQuery("DELETE FROM Notification n WHERE n.account.id = :account")
				.setParameter("account", account.id).executeUpdate();
	}
	
	public static int countForAccount(Account account){
		return ((Number)JPA.em()
				.createQuery("SELECT COUNT(n.id) FROM Notification n WHERE n.account = :account")
				.setParameter("account", account)
				.getSingleResult()).intValue();
	}
		
	@Override
	public void create() {
		JPA.em().persist(this);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete() {
		JPA.em().remove(this);
	}
	
}

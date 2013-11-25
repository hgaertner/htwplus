package models;

import java.util.List;
import java.lang.Exception;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NoResultException;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import models.base.BaseModel;

@Entity
@Table(uniqueConstraints=
@UniqueConstraint(columnNames = {
		"account_id", 
		"noteType", 
		"object_id"
		}))
public class Notification extends BaseModel {
	
	public enum Scope {
		GROUP,
		POST,
		ACCOUNT
	}
	
	
	public enum NotificationType {
		GROUP_NEW_POST(Scope.GROUP),
		GROUP_NEW_MEDIA(Scope.GROUP),
		GROUP_NEW_REQUEST(Scope.GROUP),
		GROUP_REQUEST_SUCCESS(Scope.GROUP),
		GROUP_REQUEST_FAIL(Scope.GROUP),
		POST_NEW_COMMENT(Scope.POST);
		
		private Scope scope;
		
		public Scope getScope() {
			return this.scope;
		}
		
		private NotificationType(Scope scope) {
			this.scope = scope;
		}
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

	public static void groupActivity() {
		
	}

	// GROUP NOTIFICATIONS
	public static void newGroupNotification(NotificationType noteType, Group group, Account sender) {
		// Get all accounts for that group
		if(noteType.getScope() != Scope.GROUP) {
			throw new RuntimeException("Invalid Notification Type for this method");
		}
		
		List<Account> accounts =  GroupAccount.findAccountsByGroup(group);
		NotificationType type = noteType;
		
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

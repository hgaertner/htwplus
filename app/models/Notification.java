package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.NoResultException;
import javax.persistence.OneToOne;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import models.base.BaseModel;

@Entity
public class Notification extends BaseModel {
	
	public static enum Type {
		NEW_GROUP_POST,
		GROUP_ACTIVITY,
		GROUP_MEDIA_ACTIVITY,
		COURSE_ACTIVITY,
		COURSE_MEDIA_ACTIVITY,
		NEWSSTREAM
	}

	@Required
	@OneToOne
	public Account account;
	
	@Required
	public Type type;
	
	public String hash;
	
	@Required
	public Boolean read = false;
	
	public static void groupActivity() {
		
	}

	// GROUP NOTIFICATIONS
	public static void newGroupPost(Group group, Account sender) {
		// Get all accounts for that group
		List<Account> accounts =  GroupAccount.findAccountsByGroup(group);
		Type type = Type.NEW_GROUP_POST;
		
		for (Account account : accounts) {
			if(!account.equals(sender)){
				String hash = Notification.buildHash(account, type, group.id); 
				if(Notification.findByHash(hash) == null) {
					Notification notf = new Notification();
					notf.account = account;
					notf.type = type;
					notf.hash = hash;
					notf.create();
					Logger.info("Created new Notification for User: " + account.id.toString());
				}
			}
		}
	}
	
	public static Notification findByHash(String hash) {
    	try{
	    	return (Notification) JPA.em()
					.createQuery("from Notification n where n.hash = :hash")
					.setParameter("hash", hash).getSingleResult();
	    } catch (NoResultException exp) {
	    	return null;
		}
	}
	
	private static String buildHash(Account account, Type type, Long referenceId) {
		return account.id.toString() + 
				"_" + String.valueOf(type.ordinal()) + 
				"_" + referenceId.toString();
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
		// TODO Auto-generated method stub
		
	}
	
}

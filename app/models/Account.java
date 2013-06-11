package models;

import java.util.*;
import play.data.validation.Constraints.*;
import javax.persistence.*;
import models.base.BaseModel;
import play.db.jpa.*;

import java.util.Set;

@Entity
@SequenceGenerator(name = "default_seq", sequenceName = "account_seq")
public class Account extends BaseModel {

	public String loginname;

	@Required
	public String firstname;

	@Required
	public String lastname;

	@Required
	@Email
	public String email;

	@Required
	public String password;

	@OneToMany(mappedBy = "account")
	public Set<Friendship> friends;
	
	@OneToMany(mappedBy="account")
	public Set<GroupAccount> groupMemberships;

	public Date lastLogin;

	public String studentId;

	public int role;

	public Boolean approved;

	public static Account findById(Long id) {
		return JPA.em().find(Account.class, id);
	}

	@Override
	public void create() {
		JPA.em().persist(this);
	}

	@Override
	public void update(Long id) {
		this.id = id;
		JPA.em().merge(this);
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
	}
	
	/**
     * Retrieve a User from email.
     */
    public static Account findByEmail(String email) {
    	try{
	    	return (Account) JPA.em()
					.createQuery("from Account a where a.email = :email")
					.setParameter("email", email).getSingleResult();
	    } catch (NoResultException exp) {
	    	return null;
		}
    }

	public static Account authenticate(String email, String password) {
		Account currentAcc = null;
		try{
			final Account result = (Account) JPA.em()
				.createQuery("from Account a where a.email = :email")
				.setParameter("email", email).getSingleResult();
			if (result != null && password.equals(result.password)) {
				currentAcc = result;
			}
			return currentAcc;
		}catch (NoResultException exp) {
			return currentAcc;
		}
	}
	
	//
	// GETTER & SETTER
	//
	
	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public String getLoginname() {
		return loginname;
	}

	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public static boolean isOwner(Long accountId, String email) {
		Account a = JPA.em().find(Account.class, accountId);
		if(a.email.equals(email)){
			return true;
		} else { 
			return false;
		}
	}

}
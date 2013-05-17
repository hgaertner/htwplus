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

	@Required
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
		// TODO Auto-generated method stub

	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
	}
	
	/**
     * Retrieve a User from email.
     */
    public static Account findByEmail(String email) {
    	return (Account) JPA.em()
				.createQuery("from Account a where a.email = :email")
				.setParameter("email", email).getSingleResult();
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

}
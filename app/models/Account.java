package models;

import java.util.*;

import play.Logger;
import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;

import models.base.BaseModel;

import org.hibernate.validator.constraints.Length;
import play.db.jpa.*;
//import scala.collection.mutable.HashSet;

import java.util.HashSet;
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

}
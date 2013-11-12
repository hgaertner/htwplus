package models;

import java.util.*;

import play.Logger;
import play.data.validation.Constraints.*;

import javax.jws.Oneway;
import javax.persistence.*;
import javax.validation.ConstraintViolationException;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.validator.constraints.Length;

import controllers.Component;
import controllers.routes;
import models.base.BaseModel;
import play.db.jpa.*;

import java.util.Set;

@Entity
@Indexed
public class Account extends BaseModel {

	public String loginname;
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	public String name;

	@Required
	public String firstname;

	@Required
	public String lastname;

	@Required
	@Email
	@Column(unique=true)
	public String email;

	@Required
	public String password;
	
	public String avatar;

	@OneToMany(mappedBy = "account")
	public Set<Friendship> friends;
	
	@OneToMany(mappedBy="account")
	public Set<GroupAccount> groupMemberships;

	public Date lastLogin;

	public String studentId;

	@OneToOne
	public Studycourse studycourse;
	public String degree;
	public Integer semester;

	public int role;

	public Boolean approved;

	public static Account findById(Long id) {
		return JPA.em().find(Account.class, id);
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<Account> findAll(){
		return JPA.em().createQuery("SELECT a FROM Account a ORDER BY a.name").getResultList();
	}

	@Override
	public void create() {
		this.name = firstname+" "+lastname;
		JPA.em().persist(this);
	}

	@Override
	public void update() throws PersistenceException {
		this.name = this.firstname+" "+this.lastname;
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

    /**
     * Authenticates a user by email and password.
     * @param email of the user who wants to be authenticate
     * @param password of the user should match to the email ;) 
     * @return Returns the current account or Null
     */
	public static Account authenticate(String email, String password) {
		Account currentAcc = null;
		try{
			final Account result = (Account) JPA.em()
				.createQuery("from Account a where a.email = :email")
				.setParameter("email", email).getSingleResult();
			if (result != null && Component.md5(password).equals(result.password)) {
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
	
	public String getAvatarUrl() {
		String url = routes.Assets.at("images/avatars/" + this.avatar + ".png").toString();
		return url;
	}
	
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
	
	
	/**
	 * Try to get all accounts...
	 * @return List of accounts.
	 */
	@SuppressWarnings("unchecked")
	public static List<Account> all() {
		List<Account> accounts = JPA.em().createQuery("FROM Account").getResultList();
		return accounts;
	}
	
	/**
	 * Search for a account with a given keyword.
	 * 
	 * @param keyword
	 * @return List of accounts which matches with the keyword
	 */
	@SuppressWarnings("unchecked")
	public static List<Account> searchForAccountByKeyword(String keyword) {
		Logger.info("Account model searchForAccountByKeyWord: " + keyword.toLowerCase());
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(JPA.em());
		/*try {
		 This part takes care to create indexes of persistent data, which is not inserted via hibernate/ JPA this block
		 is now in the onStart in Global.java
			fullTextEntityManager.createIndexer(Group.class).startAndWait();
		} catch (InterruptedException e) {
			
			Logger.error(e.getMessage());
		}*/
		//Create a querybuilder for the group entity 
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(Account.class).get();
		//Sets the field we want to search on and tries to match with the given keyword
		org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword().wildcard()
				.onField("name").matching("*"+keyword.toLowerCase()+"*").createQuery();
		// wrap Lucene query in a javax.persistence.Query
		FullTextQuery fullTextQuery = fullTextEntityManager
				.createFullTextQuery(luceneQuery, Account.class);

		List<Account> result = fullTextQuery.getResultList(); //The result...
		Logger.info("Found " +result.size() +" accounts with keyword: " +keyword);


		return result;
	}
}
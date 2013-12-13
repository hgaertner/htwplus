package models;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceException;

import models.base.BaseModel;
import models.enums.AccountRole;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import controllers.Component;
import controllers.routes;

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

	public AccountRole role;

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
    	if(email.isEmpty()) {
    		return null;
    	}
    	try{
	    	return (Account) JPA.em()
					.createQuery("from Account a where a.email = :email")
					.setParameter("email", email).getSingleResult();
	    } catch (NoResultException exp) {
	    	return null;
		}
    }
    
	/**
     * Retrieve a User by loginname
     */
    public static Account findByLoginName(String loginName) {
    	try{
	    	return (Account) JPA.em()
					.createQuery("from Account a where a.loginname = :loginname")
					.setParameter("loginname", loginName).getSingleResult();
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
	
	public static boolean isOwner(Long accountId, Account currentUser) {
		Account a = JPA.em().find(Account.class, accountId);
		if(a.equals(currentUser)){
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
	public static List<Account> searchForAccountByKeyword(String keyword, final boolean setMaxResult) {
		final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(JPA.em());
		//Create a querybuilder for the group entity 
		final QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(Account.class).get();
		//Create a criteria because we just want to search for accounts
		final Session session = fullTextEntityManager
				.unwrap(org.hibernate.Session.class);
		final Criteria criteria = session.createCriteria(Account.class);
		criteria.addOrder(Order.asc("name"));
		//Sets the field we want to search on and tries to match with the given keyword
		final org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword().wildcard()
				.onField("name").matching("*"+keyword.toLowerCase()+"*").createQuery();
		// wrap Lucene query in a javax.persistence.Query
		final FullTextQuery fullTextQuery = fullTextEntityManager
				.createFullTextQuery(luceneQuery, Account.class);
		if(setMaxResult) {
			criteria.setMaxResults(10);
		}
		fullTextQuery.setCriteriaQuery(criteria);
		List<Account> result = fullTextQuery.getResultList(); //The result...
		session.clear();
		return result;
	}
}
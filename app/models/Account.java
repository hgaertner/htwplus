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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.StandardFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.apache.solr.analysis.StopFilterFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import play.Logger;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import controllers.Component;
import controllers.routes;

@Entity
@Indexed
@AnalyzerDef(name = "searchtokenanalyzerAcc", tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class), filters = {
		@TokenFilterDef(factory = StandardFilterFactory.class),
		@TokenFilterDef(factory = LowerCaseFilterFactory.class),
		@TokenFilterDef(factory = StopFilterFactory.class, params = { @Parameter(name = "ignoreCase", value = "true") }) })
@org.hibernate.search.annotations.Analyzer(definition = "searchtokenanalyzerAcc")
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
	public static FullTextQuery searchForAccountByKeyword(String keyword, int limit, int offset) {
		
		final BooleanQuery bQuery = new BooleanQuery();
		final FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(JPA.em());

		Analyzer analyzer = fullTextEntityManager.getSearchFactory()
				.getAnalyzer("searchtokenanalyzerAcc");
		QueryParser parser = new QueryParser(Version.LUCENE_35, "name",
				analyzer);
		String[] tokenized = null;
		if(!keyword.isEmpty()) {
			try {
				Query query = parser.parse(keyword);
				String cleanedText = query.toString("name");
				Logger.info("[CLEANING] " + cleanedText);
				tokenized = cleanedText.split("\\s");

			} catch (ParseException e) {
				Logger.error(e.getMessage());
			}
		}
		// Create a querybuilder for the account entity
		final QueryBuilder qBuilder = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(Account.class).get();
		if (tokenized != null && tokenized.length > 1) {
			for (int i = 0; i < tokenized.length; i++) {
				if (i == (tokenized.length - 1)) {
					org.apache.lucene.search.Query query = qBuilder.keyword()
							.wildcard().onField("name")
							.matching(tokenized[i] + "*").createQuery();
					bQuery.add(query, BooleanClause.Occur.MUST);
				} else {
					Term exactTerm = new Term("name", tokenized[i]);
					bQuery.add(new TermQuery(exactTerm),
							BooleanClause.Occur.MUST);
				}

			}
		} else {
			final org.apache.lucene.search.Query luceneQuery = qBuilder.keyword().wildcard()
					.onField("name").matching("*"+keyword.toLowerCase()+"*").createQuery();
			bQuery.add(luceneQuery, BooleanClause.Occur.MUST);
		}

		
		
		
		
		
		//Create a criteria because we just want to search for accounts
		final Session session = fullTextEntityManager
				.unwrap(org.hibernate.Session.class);
		Criteria criteria = session.createCriteria(Account.class);
		criteria.addOrder(Order.asc("name"));
		criteria = limit(criteria,limit,offset);
		
		//Sets the field we want to search on and tries to match with the given keyword
		
		// wrap Lucene query in a javax.persistence.Query
		final FullTextQuery fullTextQuery = fullTextEntityManager
				.createFullTextQuery(bQuery, Account.class);

		fullTextQuery.setCriteriaQuery(criteria);
		session.clear();
		return fullTextQuery;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Account> accountSearch(String keyword, int limit, int page) {
		
		// create offset
		int offset = (page * limit) - limit;
				
		FullTextQuery fullTextQuery = searchForAccountByKeyword(keyword, limit, offset);
		List<Account> accounts = fullTextQuery.getResultList(); // The result...
		return accounts;
	}
	
	public static int countAccountSearch(String keyword) {
		
		FullTextQuery fullTextQuery = searchForAccountByKeyword(keyword, 0, 0);
		
		// SearchException: HSEARCH000105: Cannot safely compute getResultSize() when a Criteria with restriction is used.
		int count = fullTextQuery.getResultList().size();
		return count;
	}
	
	protected static Criteria limit(Criteria criteria, int limit, int offset) {
		if (limit > 0) {
			criteria.setMaxResults(limit);
		}
		if (offset >= 0) {
			criteria.setFirstResult(offset);
		}
		return criteria;
	}
}
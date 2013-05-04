package models;

import java.util.*;

import play.Logger;
import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;
import org.hibernate.validator.constraints.Length;
import play.db.jpa.*;

@Entity
@SequenceGenerator(name = "user_seq", sequenceName = "user_seq")
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	public Long id;
	
	@Required
	public String name;

	public Date createdAt;
	public Date updatedAt;

	@PrePersist
	void createdAt() {
		this.createdAt = this.updatedAt = new Date();
	}

	@PreUpdate
	void updatedAt() {
		this.updatedAt = new Date();
	}
	
	public static Account findById(Long id) {
		return JPA.em().find(Account.class, id);
	}
	
	public void create() {
		JPA.em().persist(this);
	}

	
}
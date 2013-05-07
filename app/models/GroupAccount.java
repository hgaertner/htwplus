package models;

import javax.persistence.*;
import play.db.jpa.*;

import models.ids.GroupAccountId;

@Entity
@IdClass(GroupAccountId.class)
@Table(name="group_account")
public class GroupAccount {
	
	@Id
	@ManyToOne
	@JoinColumn(name = "group_")		
	public Group group;
	
	@Id
	@ManyToOne
	@JoinColumn(name = "account")
	public Account account;

	public Boolean approved;
	
	public static GroupAccount findById(Long id) {
		return JPA.em().find(GroupAccount.class, id);
	}
	

}


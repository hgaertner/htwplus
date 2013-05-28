package models;

import javax.persistence.*;

import models.ids.FriendshipId;

@Entity
@IdClass(FriendshipId.class)
public class Friendship {
	
	@Id
	private Long accountId;
	@Id
	private Long friendId;
	
	@ManyToOne
	@JoinColumn(name = "accountId", updatable = false, insertable = false)
	public Account account;
	
	@ManyToOne
	@JoinColumn(name = "friendId", updatable = false, insertable = false)
	public Account friend;

	public Boolean approved;

}
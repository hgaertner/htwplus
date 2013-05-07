package models;

import javax.persistence.*;
import models.ids.FriendshipId;

@Entity
@IdClass(FriendshipId.class)
public class Friendship {
	
	@Id
	@ManyToOne
	@JoinColumn(name="account")
	public Account account;
	
	@Id
	@ManyToOne
	@JoinColumn(name="friend")
	public Account friend;

	public Boolean approved;

}
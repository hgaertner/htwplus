package models.ids;

import java.io.Serializable;
import javax.persistence.*;

public class FriendshipId implements Serializable {

	private Long account;
	private Long friend;
	
	public FriendshipId() {
		super();
	}

	@Override
	public int hashCode() {	
		return (int)(account + friend);
	}
	
	@Override
	public boolean equals(Object object) {
	    if (object instanceof FriendshipId) {
	    	FriendshipId otherId = (FriendshipId) object;
	        return (otherId.account == this.account) && (otherId.friend == this.friend);
	      }
	      return false;
	}
}
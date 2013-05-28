package models.ids;

import java.io.Serializable;
import javax.persistence.*;

public class FriendshipId implements Serializable {

	private Long accountId;
	private Long friendId;
	
	public FriendshipId() {
		super();
	}

	@Override
	public int hashCode() {	
		return (int)(accountId + friendId);
	}
	
	@Override
	public boolean equals(Object object) {
	    if (object instanceof FriendshipId) {
	    	FriendshipId otherId = (FriendshipId) object;
	        return (otherId.accountId == this.accountId) && (otherId.friendId == this.friendId);
	      }
	      return false;
	}
}
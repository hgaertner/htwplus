package models.ids;

import java.io.Serializable;
import javax.persistence.*;


public class GroupAccountId implements Serializable {

	//@Column(name="group_")
	private Long groupId;
	private Long accountId;
	
	public GroupAccountId() {
		super();
	}

	@Override
	public int hashCode() {	
		return (int)(groupId + accountId);
	}
	
	@Override
	public boolean equals(Object object) {
	    if (object instanceof GroupAccountId) {
	    	GroupAccountId otherId = (GroupAccountId) object;
	        return (otherId.groupId == this.groupId) && (otherId.accountId == this.accountId);
	      }
	      return false;
	}
}
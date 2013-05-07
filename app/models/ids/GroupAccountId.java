package models.ids;

import java.io.Serializable;
import javax.persistence.*;


public class GroupAccountId implements Serializable {

	@Column(name="group_")
	private Long group;
	private Long account;
	
	public GroupAccountId() {
		super();
	}

	@Override
	public int hashCode() {	
		return (int)(group + account);
	}
	
	@Override
	public boolean equals(Object object) {
	    if (object instanceof GroupAccountId) {
	    	GroupAccountId otherId = (GroupAccountId) object;
	        return (otherId.group == this.group) && (otherId.account == this.account);
	      }
	      return false;
	}
}
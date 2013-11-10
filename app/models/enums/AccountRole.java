package models.enums;

public enum AccountRole {
	
	STUDENT("Student"),
	TUTOR("Dozent"),
	ADMIN("Administrator");

	private String displayName;
	
	private AccountRole(String displayName)  {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
}

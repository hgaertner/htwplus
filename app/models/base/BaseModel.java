package models.base;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@MappedSuperclass
public abstract class BaseModel{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_seq")
	public Long id;
	
	@Column(name = "created_at")
	public Date createdAt;
	@Column(name = "updated_at")
	public Date updatedAt;
		
	@PrePersist
	void createdAt() {
		this.createdAt = this.updatedAt = new Date();
	}

	@PreUpdate
	void updatedAt() {
		this.updatedAt = new Date();
	}
	
	public abstract void create();
		
	public abstract void update();
	
	public abstract void delete();
		
	public boolean equals(Object obj){
		BaseModel baseModel = (BaseModel) obj;
		if(baseModel.id == null){
			return false;
		}
		if(baseModel.id.equals(this.id)) {
			return true; 
		} else {
			return false;
		}
	}
	
}
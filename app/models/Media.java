package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;

import play.Logger;
import play.Play;
import play.data.validation.Constraints.*;
import play.data.format.Formats.*;
import javax.persistence.*;

import models.base.BaseModel;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import controllers.routes;

import play.db.jpa.*;
import play.mvc.Result;
import scala.reflect.io.FileOperationException;
import views.html.Course.view;

import java.util.UUID;

@Entity
@SequenceGenerator(name = "default_seq", sequenceName = "media_seq")
public class Media extends BaseModel {
	
	@Required
	public String title;
	
	@Required
	public String fileName;
	
	public String description;
	
	@Required
	public String url;

	@Required
	public String mimetype;
	
	@Required
	public Long size;
	
	@ManyToOne
	public Group group;
	
	@ManyToOne
	public Course course;
	
	@ManyToOne
	public Account owner;
	
	@Transient
	public File file;
	
	public static Media findById(Long id) {
		Media media = JPA.em().find(Media.class, id);
		// TODO check access
	    String path = Play.application().path().toString();
	    String relPath = Play.application().configuration().getString("media.relativePath");
		media.file = new File(path + "/" + relPath + "/" + media.url);
		if(media.file.exists()) {
			return media;
		} else {
			return null;
		}
	}
	
	@Override
	public void create() {
		
	}
	
	public void create(String user) {
		this.size = file.length();
		this.url = this.createRelativeURL() + "/" + this.getUniqueFileName(this.fileName);
		try {
			this.createFile();
			JPA.em().persist(this);
		} catch (FileOperationException e) {
			throw e;
		}
	}
	
	@Override
	public void update() {
		JPA.em().merge(this);
	}
	
	@Override
	public void delete() {
		JPA.em().remove(this);
	}
	
	private String getUniqueFileName(String fileName) {
		return UUID.randomUUID().toString() + '_' + fileName;
	}
		
	private void createFile() throws FileOperationException {
	    String path = Play.application().path().toString();
	    String relPath = Play.application().configuration().getString("media.relativePath");
	    File newFile = new File(path + "/" + relPath + "/" + this.url);
	    if(newFile.exists()){
	    	throw new FileOperationException("File exists already");
	    }
	    newFile.getParentFile().mkdirs();
	    this.file.renameTo(newFile);
	    if(!newFile.exists()) {
	    	throw new FileOperationException("Could not upload file");
	    }
	}
	
	private String createRelativeURL() {
		Date now = new Date();
		String format = new SimpleDateFormat("yyyy/MM/dd").format(now);
		return format;
	}

	public static boolean isOwner(Long mediaId, Account account) {
		Media m = JPA.em().find(Media.class, mediaId);
		if(m.owner.equals(account)){
			return true;
		}else {
			return false;
		}
	}
	
}
package models;

import java.io.File;
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
import views.html.Course.view;

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
	
	@Transient
	public File file;
	
	public static Media findById(Long id) {
		Media media = JPA.em().find(Media.class, id);
		// TODO check access
	    String path = Play.application().path().toString();
	    String relPath = Play.application().configuration().getString("media.relativePath");
		media.file = new File(path + "/" + relPath + "/" + media.url);
		return media;
	}
	
	@Override
	public void create() {
		this.size = file.length();
		this.url = this.createRelativeURL() + "/" + this.fileName;
		this.createFile();
		JPA.em().persist(this);
	}
	
	@Override
	public void update(Long id) {
		this.id = id;
		JPA.em().merge(this);
	}
	
	@Override
	public void delete() {
		JPA.em().remove(this);
	}
		
	private void createFile() {
	    String path = Play.application().path().toString();
	    String relPath = Play.application().configuration().getString("media.relativePath");
	    File newFile = new File(path + "/" + relPath + "/" + this.url);
	    newFile.getParentFile().mkdirs();
	    this.file.renameTo(newFile);
	}
	
	private String createRelativeURL() {
		Date now = new Date();
		String format = new SimpleDateFormat("yyyy/MM/dd").format(now);
		return format;
	}
	
}
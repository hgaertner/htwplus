package controllers;

import java.io.File;
import java.util.List;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.data.*;
import models.*;
import views.html.Course.view;
import views.html.Media.*;
import play.db.jpa.*;
import scala.reflect.internal.Trees.This;

@Security.Authenticated(Secured.class)
public class MediaController extends BaseController {
	
	static Form<Media> mediaForm = Form.form(Media.class);
	
    @Transactional(readOnly=true)	
    public static Result view(Long id) {
    	Media media = Media.findById(id);
    	if(media == null) {
    		  return redirect(routes.Application.index());
    	} else {
    		response().setHeader("Content-disposition","attachment; filename=" + media.fileName);
    		return ok(media.file);
    	}
    }
        
	@Transactional
    public static Result upload(String target, Long id) {				
		MultipartFormData body = request().body().asMultipartFormData();
		List<Http.MultipartFormData.FilePart> uploads = body.getFiles();
		Call ret = routes.Application.index();
		Group group = null;
		Course course = null;
		
		if(target.equals(Media.GROUP)) {
			group = Group.findById(id);
			ret = routes.GroupController.media(id);
		} else if (target.equals(Media.COURSE)) {
			course = Course.findById(id);
			ret = routes.CourseController.index();
		} else {
			return redirect(ret);
		}
		
		if(!uploads.isEmpty()) {
			for (FilePart upload : uploads) {
				Media med = new Media();
				
				if(target.equals(Media.GROUP)) {
					med.group = group;
				} else if (target.equals(Media.COURSE)) {
					med.course = course;
				}
				
				med.title = upload.getFilename();
				med.mimetype = upload.getContentType();
				med.fileName = upload.getFilename();
				med.file = upload.getFile();
				med.owner = Component.currentAccount();
				try {
					med.create(request().username());
				} catch (Exception e) {
					return internalServerError(e.getMessage());
				}
			}
			flash("message", "File uploaded!");
		    return redirect(ret);
		} else {
			flash("error", "Missing file");
		    return redirect(ret);  
		}
		
    }
	
}
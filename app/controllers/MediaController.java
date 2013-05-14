package controllers;

import java.io.File;

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

public class MediaController extends Controller {
	
	static Form<Media> mediaForm = Form.form(Media.class);	
	
    @Transactional(readOnly=true)	
    public static Result view(Long id) {
    	Media media = Media.findById(id);
    	if(media == null) {
    		return redirect(routes.MediaController.add());
    	} else {
    		return ok(media.file);
    	}
    }
    
    public static Result add() {
    	return ok(upload.render(mediaForm));
    }
    
	@Transactional
    public static Result upload() {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart upload  = body.getFile("picture");
		
		Media med = new Media();
		
		if (upload != null) {
			
			med.title = upload.getFilename();
			med.mimetype = upload.getContentType();
			med.fileName = upload.getFilename();
			med.file = upload.getFile();
			med.create();
			
		    return ok("File uploaded");
		} else {
			flash("error", "Missing file");
			return redirect(routes.MediaController.upload());    
		}
    }
	
}
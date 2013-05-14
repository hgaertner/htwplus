package controllers;

import java.io.File;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.data.*;
import models.*;
import views.html.Media.*;
import play.db.jpa.*;
import scala.reflect.internal.Trees.This;

public class MediaController extends Controller {
	
	static Form<Media> mediaForm = Form.form(Media.class);	
	
    @Transactional(readOnly=true)	
    public static Result view(Long id) {
    	return TODO;
    }
    
    public static Result add() {
    	return ok(upload.render(mediaForm));
    }
    
	@Transactional
    public static Result upload() {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picture = body.getFile("picture");
		if (picture != null) {
			String fileName = picture.getFilename();
		    String contentType = picture.getContentType(); 
		    File file = picture.getFile();
		    return ok("File uploaded");
		} else {
			flash("error", "Missing file");
			return redirect(routes.MediaController.upload());    
		}
    }
	
}
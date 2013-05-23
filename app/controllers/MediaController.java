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
public class MediaController extends Controller {
	
	static Form<Media> mediaForm = Form.form(Media.class);
	
    @Transactional(readOnly=true)	
    public static Result view(Long id) {
    	Media media = Media.findById(id);
    	if(media == null) {
    		return redirect(routes.MediaController.add());
    	} else {
    		response().setHeader("Content-disposition","attachment; filename=" + media.fileName);
    		return ok(media.file);
    	}
    }
    
    public static Result add() {
    	return ok(upload.render(mediaForm));
    }
    
	@Transactional
    public static Result upload() {				
		MultipartFormData body = request().body().asMultipartFormData();
		List<Http.MultipartFormData.FilePart> uploads = body.getFiles();

		if(!uploads.isEmpty()) {
			for (FilePart upload : uploads) {
				Media med = new Media();
				med.title = upload.getFilename();
				med.mimetype = upload.getContentType();
				med.fileName = upload.getFilename();
				med.file = upload.getFile();
				try {
					med.create(request().username());
				} catch (Exception e) {
					return internalServerError(e.getMessage());
				}
			}
			flash("message", "File uploaded!");
		    return redirect(routes.Application.index());
		} else {
			flash("error", "Missing file");
			return redirect(routes.MediaController.upload());    
		}
		
    }
	
}
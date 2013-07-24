package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.data.*;
import models.*;
import views.html.Course.view;
import views.html.Media.*;
import play.db.jpa.*;
import scala.annotation.meta.param;
import scala.collection.generic.BitOperations.Int;
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
    		response().setHeader("Content-disposition","attachment; filename=\"" + media.fileName + "\"");
    		return ok(media.file);
    	}
    }
    
    @Transactional(readOnly=true)	
    public static Result multiView(String target, Long id) {

		Call ret = routes.Application.index();
		Group group = null;
		Course course = null;
		String filename = "result.zip";
		if(target.equals(Media.GROUP)) {
			group = Group.findById(id);
			filename = group.title + ".zip";
			ret = routes.GroupController.media(id);
		} else if (target.equals(Media.COURSE)) {
			course = Course.findById(id);
			filename = course.title + ".zip";
			ret = routes.CourseController.index();
		} else {
			return redirect(ret);
		}
    	
    	String[] selection = request().body().asFormUrlEncoded().get("selection");
    	List<Media> mediaList = new ArrayList<Media>();
    	
       	for (String s : selection) {
    		Media media = Media.findById(Long.parseLong(s));
    		mediaList.add(media);
       	}
       
		try {
			File file = createZIP(mediaList, filename);
			response().setHeader("Content-disposition","attachment; filename=\"" + filename + "\"");
	    	return ok(file);
		} catch (IOException e) {
			return redirect(ret);
		}

    }
    
    
    private static File createZIP(List<Media> media, String fileName) throws IOException {
    	
	    String path = Play.application().path().toString();
	    String relPath = Play.application().configuration().getString("media.relativePath");
    	File file = File.createTempFile("htwplus_temp", ".tmp", new File(path + "/" + relPath + "/tmp"));
    	
    	ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(file));
    	zipOut.setLevel(Deflater.NO_COMPRESSION);
        byte[] buffer = new byte[4092];
        int byteCount = 0;
    	for (Media m : media) {
    		zipOut.putNextEntry(new ZipEntry(m.fileName));
    		FileInputStream fis = new FileInputStream(m.file);
            byteCount = 0;
            while ((byteCount = fis.read(buffer)) != -1)
            {
            	zipOut.write(buffer, 0, byteCount);
            }
            fis.close();
            zipOut.closeEntry();
		}
    	
    	zipOut.flush();
    	zipOut.close();
    	return file;
    }
    
	@Transactional
    public static Result upload(String target, Long id) {	
		
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
		
		String[] contentLength = request().headers().get("Content-Length");
		double size = 0.0;
		if(contentLength != null){
			size = Double.parseDouble(contentLength[0]);
			size = size / 1042 / 1042;
		}
		
		if(size >= 20.0) {
			flash("error", "Du darfst maximal nur 20MB hochladen.");
			return redirect(ret);
		}
		
		MultipartFormData body = request().body().asMultipartFormData();
		List<Http.MultipartFormData.FilePart> uploads = body.getFiles();


		List<Media> mediaList = new ArrayList<Media>();
		
		if(!uploads.isEmpty()) {
			for (FilePart upload : uploads) {
				Media med = new Media();

				med.title = upload.getFilename();
				med.mimetype = upload.getContentType();
				med.fileName = upload.getFilename();
				med.file = upload.getFile();				
				med.owner = Component.currentAccount();
				
				if (med.file.length() / 1024.0 / 1024.0 >= 5.0) {
					flash("error", "Maximale Dateigröße beträgt 5MB.");
					return redirect(ret);
				}
				
				if(target.equals(Media.GROUP)) {
					med.group = group;
					if(med.existsInGroup(group)){
						flash("error", "Eine Datei mit diesem Namen existiert bereits.");
						return redirect(ret);
					}
					
				} else if (target.equals(Media.COURSE)) {
					med.course = course;
					if(med.existsInCourse(course)){
						flash("error", "Eine Datei mit diesem Namen existiert bereits.");
						return redirect(ret);
					}
				}
				
				mediaList.add(med);
			}
			
			for (Media m : mediaList) {
				try {
					m.create();
				} catch (Exception e) {
					return internalServerError(e.getMessage());
				}
			}
			flash("success", "Datei(en) erfolgreich hinzugefügt.");
		    return redirect(ret);
		} else {
			flash("error", "Missing file");
		    return redirect(ret);  
		}
		
    }
	
}
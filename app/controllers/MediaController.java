package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	final static String tempPrefix = "htwplus_temp";
	
    @Transactional(readOnly=true)	
    public static Result view(Long id) {
    	Media media = Media.findById(id);
    	if(Secured.viewMedia(media)) {
			if (media == null) {
				return redirect(routes.Application.index());
			} else {
				response().setHeader("Content-disposition","attachment; filename=\"" + media.fileName + "\"");
				return ok(media.file);
			}
    	} else {
    		flash("error", "Dazu hast du keine Berechtigung!");
    		return redirect(routes.Application.index());
     	}
    }
    
    @Transactional
    public static Result delete(Long id) {
    	Media media = Media.findById(id);
    	
    	Call ret = routes.Application.index();
    	if(media.belongsToGroup()){
    		Group group = media.group;
    		ret = routes.GroupController.media(group.id);
    	} 
    	if(media.belongsToCourse()){
    		Course course = media.course;
    		ret = routes.GroupController.media(course.id);
    	} 
    	
    	media.delete();
		flash("success", "Datei " + media.title + " erfolgreich gelöscht!");
    	return redirect(ret);
    }	
    
    @Transactional(readOnly=true)	
    public static Result multiView(String target, Long id) {
    	
		Call ret = routes.Application.index();
		Group group = null;
		Course course = null;
		String filename = "result.zip";
		
		if(target.equals(Media.GROUP)) {
			group = Group.findById(id);
			filename = createFileName(group.title);
			ret = routes.GroupController.media(id);
		} else if (target.equals(Media.COURSE)) {
			course = Course.findById(id);
			filename = createFileName(course.title);
			ret = routes.CourseController.index();
		} else {
			return redirect(ret);
		}
    	
    	String[] selection = request().body().asFormUrlEncoded().get("selection");
    	List<Media> mediaList = new ArrayList<Media>();
    	
    	if(selection != null) {
           	for (String s : selection) {
        		Media media = Media.findById(Long.parseLong(s));
        		if(Secured.viewMedia(media)) {
            		mediaList.add(media);	
        		} else {
        			flash("error", "Dazu hast du keine Berechtigung!");
        			return redirect(routes.Application.index());
        		}
           	}
    	} else {
    		flash("error", "Bitte wähle mindestens eine Datei aus.");
    		return redirect(ret);
    	}

		try {
			File file = createZIP(mediaList, filename);
			response().setHeader("Content-disposition","attachment; filename=\"" + filename + "\"");
	    	return ok(file);
		} catch (IOException e) {
			flash("error", "Etwas ist schiefgegangen. Bitte probiere es noch einmal!");
			return redirect(ret);
		}
    }
    
    private static String createFileName(String prefix) {
    	return prefix + "-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".zip";
    }
    
    
    private static File createZIP(List<Media> media, String fileName) throws IOException {
    	
    	cleanUpTemp();
	    String path = Play.application().path().toString();
	    String tmpPath = Play.application().configuration().getString("media.tempPath");
    	File file = File.createTempFile(tempPrefix, ".tmp", new File(path + "/" + tmpPath));
    	
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
    
    public static void cleanUpTemp() {
	    String path = Play.application().path().toString();
	    String tmpPath = Play.application().configuration().getString("media.tempPath");
	    File dir = new File(tmpPath);
	    System.out.println(path+"   "+tmpPath);
	    File[] files = dir.listFiles();
	    for (File file : files) {
			if(file.getName().startsWith(tempPrefix)) {
				file.delete();
			}
		}
    }
    
	@Transactional
    public static Result upload(String target, Long id) {	
		
	    final int maxTotalSize = Play.application().configuration().getInt("media.maxSize.total");
	    final int maxFileSize = Play.application().configuration().getInt("media.maxSize.file");
	    
		Call ret = routes.Application.index();
		Group group = null;
		Course course = null;
		
		// Where to put the media
		if(target.equals(Media.GROUP)) {
			group = Group.findById(id);
			ret = routes.GroupController.media(id);
		} else if (target.equals(Media.COURSE)) {
			course = Course.findById(id);
			ret = routes.CourseController.index();
		} else {
			return redirect(ret);
		}
		
		// Is it to big in total?
		String[] contentLength = request().headers().get("Content-Length");
		if(contentLength != null){
			int size = Integer.parseInt(contentLength[0]);
			if(Media.byteAsMB(size) > maxTotalSize) {
				flash("error", "Du darfst auf einmal nur " + maxTotalSize + " MB hochladen.");
				return redirect(ret);
			}
		} else {
			flash("error", "Etwas ist schiefgegangen. Bitte probiere es noch einmal!");
		    return redirect(ret);  	
		}
		
		// Get the data
		MultipartFormData body = request().body().asMultipartFormData();
		List<Http.MultipartFormData.FilePart> uploads = body.getFiles();

		List<Media> mediaList = new ArrayList<Media>();
		
		if(!uploads.isEmpty()) {
			
			// Create the Media models and perform some checks
			for (FilePart upload : uploads) {
				
				Media med = new Media();
				med.title = upload.getFilename();
				med.mimetype = upload.getContentType();
				med.fileName = upload.getFilename();
				med.file = upload.getFile();				
				med.owner = Component.currentAccount();
				
				if (Media.byteAsMB(med.file.length()) > maxFileSize) {
					flash("error", "Die Datei " + med.title + " ist größer als " + maxFileSize + " MB!");
					return redirect(ret);
				}
				
				String error = "Eine Datei mit dem Namen " + med.title + " existiert bereits";
				if(target.equals(Media.GROUP)) {
					med.group = group;
					if(med.existsInGroup(group)){
						flash("error", error);
						return redirect(ret);
					}
					
				} else if (target.equals(Media.COURSE)) {
					med.course = course;
					if(med.existsInCourse(course)){
						flash("error", error);
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
			flash("error", "Etwas ist schiefgegangen. Bitte probiere es noch einmal!");
		    return redirect(ret);  
		}
		
    }
	
}
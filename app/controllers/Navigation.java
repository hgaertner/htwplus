package controllers;

import java.util.HashMap;
import java.util.Map;

import play.mvc.Http.Context;
import play.mvc.*;

public class Navigation {

	public static enum Level {PROFILE,STREAM,FRIENDS,GROUPS,COURSES,HELP,USER}
	
	private static Map<Level,Call> callMapping = new HashMap<Navigation.Level, Call>();
	static
	{
		callMapping.put(Level.PROFILE, routes.ProfileController.me());
		callMapping.put(Level.STREAM, routes.Application.index());
		callMapping.put(Level.FRIENDS, routes.FriendshipController.index());
		callMapping.put(Level.GROUPS, routes.GroupController.index());
		callMapping.put(Level.HELP, routes.Application.help());
		callMapping.put(Level.USER, routes.Application.searchForAccounts(""));
	}
	
	private static Map<Level,String> titleMapping = new HashMap<Navigation.Level, String>();
	static
	{
		titleMapping.put(Level.PROFILE, "Profil");
		titleMapping.put(Level.STREAM, "Newsstream");
		titleMapping.put(Level.FRIENDS, "Freunde");
		titleMapping.put(Level.GROUPS, "Gruppen & Kurse");
		titleMapping.put(Level.HELP, "Hilfe");
		titleMapping.put(Level.USER, "User");
	}
	
	private static Call fallbackCall = routes.Application.index();

	final private static String levelIdent = "navLevel";
	final private static String titleIdent = "navTitle";
	final private static String parentTitleIdent = "navParentTitle";
	final private static String parentCallIdent = "navParentCall";
	
	public static void set(Level level) {
		Navigation.set(level,null,null,null);
	}
	
	public static void set(String title) {
		Navigation.set(null,title,null,null);
	}
	
	public static void set(Level level, String title) {
		Navigation.set(level,title,null,null);
	}
	
	public static void set(Level level, String title, String parentTitle, Call parentCall) {
		Context ctx = Context.current();
		ctx.args.put(levelIdent, level);
		ctx.args.put(titleIdent, title);
		ctx.args.put(parentTitleIdent, parentTitle);
		ctx.args.put(parentCallIdent, parentCall);
	}
	
	public static Level getLevel() {
		return (Level)Context.current().args.get(levelIdent);
	}
	
	public static Call getLevelRoute(Level level) {
		if(level != null) {
			if(callMapping.containsKey(level)){
				return callMapping.get(level);
			} else {
				return fallbackCall;
			}
		} else {
			return fallbackCall;
		}	
	}
	
	public static String getLevelTitle(Level level) {
		if(level != null) {
			if(titleMapping.containsKey(level)){
				return titleMapping.get(level);
			} else {
				return "UNKNOWN";
			}
		} else {
			return "UNKNOWN";
		}	
	}
	
	public static String getTitle() {
		return (String)Context.current().args.get(titleIdent);
	}
	
	public static String getParentTitle() {
		return (String)Context.current().args.get(parentTitleIdent);
	}
	
	public static Call getParentCall() {
		return (Call)Context.current().args.get(parentCallIdent);
	}
	
}

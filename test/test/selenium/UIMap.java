package test.selenium;

import org.openqa.selenium.remote.DesiredCapabilities;

public class UIMap {
	
	private UIMap(){}
	
	// General Settings
	public static String baseUrl = "http://localhost";
	//public static String baseUrl = "http://192.168.1.118";
	public static int port = 3333;
	
	public static String getFullUrl(){
		return baseUrl + ":" + String.valueOf(port);
	}
	
	public static String testDatabaseUrl = "jdbc:postgresql://ubuntu/play_test";
	
	// Driver local
	public static String webDriver = "firefox";
	
	// Remote
	public static Boolean activateRemote = false;
	public static String remoteUrl = "http://192.168.1.134:4444/wd/hub";
	public static DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();
	
	// Locators
	public static String submitSignupButtonID = "submitSignup";
	
}

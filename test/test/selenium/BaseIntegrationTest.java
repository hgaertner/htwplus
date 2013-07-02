package test.selenium;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement.GlobalScope;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import play.GlobalSettings;
import play.Logger;
import play.mvc.*;
import play.test.*;
import play.db.jpa.JPA;
import play.libs.F.*;
import test.selenium.pageobjects.LoginPage;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

import static org.fluentlenium.core.filter.FilterConstructor.*;

public abstract class BaseIntegrationTest {

	protected static TestServer server;
	protected static WebDriver driver;
	protected static String baseUrl = "http://192.168.126.1:3333";
	
	@BeforeClass
	public static void prepare() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("db.default.url", "jdbc:postgresql://ubuntu/play_test");
		server = new TestServer(3333, fakeApplication(settings));
		server.start();
		//driver = new ChromeDriver();	
		
		try {
			driver = new RemoteWebDriver(new URL("http://192.168.126.130:4444/wd/hub"), DesiredCapabilities.firefox());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    @AfterClass
    public static void tearDown() {
    	server.stop();
        driver.quit();
    }
  
}

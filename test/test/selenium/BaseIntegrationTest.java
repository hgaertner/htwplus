package test.selenium;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement.GlobalScope;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
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
	
	@BeforeClass
	public static void prepare() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("db.default.url", "jdbc:postgresql://ubuntu/play_test");
		server = new TestServer(3333, fakeApplication(settings));
		server.start();
	}
    
    @AfterClass
    public static void tearDown() {
    	server.stop();
    }
  
}

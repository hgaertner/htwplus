package test.selenium;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBElement.GlobalScope;

import org.hibernate.ejb.EntityManagerFactoryImpl;
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
import play.db.jpa.JPAPlugin;
import play.db.jpa.Transactional;
import play.libs.F.*;
import test.selenium.pageobjects.LoginPage;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

import static org.fluentlenium.core.filter.FilterConstructor.*;

@Transactional
public abstract class BaseIntegrationTest {

	protected static TestServer server;
	protected static WebDriver driver;
	
	@BeforeClass
	public static void prepare() throws Throwable {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("db.default.url", UIMap.testDatabaseUrl);
		FakeApplication app = Helpers.fakeApplication(settings);
		server = new TestServer(UIMap.port, app);
		server.start();
		
		if(UIMap.activateRemote == true) {
			try {
				driver = new RemoteWebDriver(new URL(UIMap.remoteUrl), UIMap.desiredCapabilities);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
			driver = new FirefoxDriver();
		}
	}
	
	@Before
	public void before(){
		driver.manage().deleteAllCookies();
	}
		
    @AfterClass
    @Transactional
    public static void tearDown() throws Throwable {	
		JPA.withTransaction(new Function0<Void>() {
	        public Void apply() {
	        	//JPA.em().createQuery("DELETE FROM Post p").executeUpdate();
	        	JPA.em().createQuery("DELETE FROM Account a WHERE a.id != 1").executeUpdate();
	    	    return null;
	        }
	    });
    	server.stop();
        driver.quit();
    } 
}

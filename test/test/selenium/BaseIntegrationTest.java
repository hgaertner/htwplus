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
	protected static String baseUrl = "http://localhost:3333";
	private static EntityManager em;
	
	@BeforeClass
	public static void prepare() throws Throwable {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("db.default.url", "jdbc:postgresql://ubuntu/play_test");
		FakeApplication app = Helpers.fakeApplication(settings);

		server = new TestServer(3333, app);
		server.start();
		driver = new FirefoxDriver();	
		
//		try {
//			driver = new RemoteWebDriver(new URL("http://192.168.126.130:4444/wd/hub"), DesiredCapabilities.firefox());
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
	        	JPA.em().createQuery("DELETE FROM Post p").executeUpdate();
	        	JPA.em().createQuery("DELETE FROM Account a WHERE a.id != 1").executeUpdate();
	    	    return null;
	        }
	    });
    	server.stop();
        driver.quit();
    }
  
}

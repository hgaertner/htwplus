package test.selenium;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import play.test.*;
import play.db.jpa.JPA;

import play.db.jpa.Transactional;
import play.libs.F.*;

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
				driver = new RemoteWebDriver(
						new URL(UIMap.remoteUrl),
						UIMap.desiredCapabilities);
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
	
	@After
	public void after() throws Throwable {
		JPA.withTransaction(new Function0<Void>() {
	        public Void apply() {
	        	JPA.em().createQuery("DELETE FROM Post p").executeUpdate();
	        	JPA.em().createQuery("DELETE FROM Account a WHERE a.id != 1")
	        		.executeUpdate();
	    	    return null;
	        }
	    });
	}
		
    @AfterClass
    @Transactional
    public static void tearDown(){	
    	server.stop();
        driver.quit();
    } 
}

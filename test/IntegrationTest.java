import org.junit.*;
import static org.junit.Assert.assertEquals;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import play.Logger;
import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

import static org.fluentlenium.core.filter.FilterConstructor.*;

public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */   
//    @Test
//    public void test() {
//        running(testServer(3333, fakeApplication()), HTMLUNIT, new Callback<TestBrowser>() {
//            public void invoke(TestBrowser browser) {
//                browser.goTo("http://localhost:3333");
//                //assertThat(browser.pageSource()).contains("Your new application is ready.");
//            }
//        });
//    }
    
	private static TestServer server;
	
	@BeforeClass
	public static void prepare() {
		server = new TestServer(3333, fakeApplication());	
	  	server.start();
	}
    
    @Test
    public void test() {
		WebDriver driver = new HtmlUnitDriver();
		driver.get("http://localhost:3333");
		String title = driver.getTitle();
		Logger.info(title);
        assertEquals(title, "HTW.plus()");
        driver.quit();
    }
    
    @AfterClass
    public static void tearDown() {
    	server.stop();
    }
  
}

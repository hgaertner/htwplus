package test.selenium;
import java.util.NoSuchElementException;

import javax.imageio.spi.RegisterableService;

import org.junit.*;

import static org.junit.Assert.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import play.Logger;
import play.mvc.*;
import play.test.*;
import play.libs.F.*;
import test.selenium.pageobjects.LoginPage;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


public class IntegrationTest extends BaseIntegrationTest {
	
//	@Test
//	public void testRegistration(){
//		driver.get("http://localhost:3333");
//		WebElement registerLink = driver.findElement(By.id("registerLink"));
//		registerLink.click();	
//	}
	private StringBuffer verificationErrors = new StringBuffer();

	
	@Test
	public void testGroupRoundtrip() throws InterruptedException {
		driver.get(baseUrl);
		LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
		loginPage.login("test@example.de", "test");
		
		driver.findElement(By.linkText("Gruppen")).click();
	
		assertTrue(driver.findElement(By.cssSelector("BODY")).getText()
				.matches("^[\\s\\S]*Meine Gruppen[\\s\\S]*$"));
		
	    driver.findElement(By.cssSelector("i.icon-plus")).click();
	    
	    // Additional Line
	    Thread.sleep(1000);
	    driver.findElement(By.id("title")).clear();
	    driver.findElement(By.id("title")).sendKeys("test");
	    driver.findElement(By.id("isClosed")).click();
	    driver.findElement(By.id("submitGroup")).click();
	    
	    // Additional Line
	    Thread.sleep(2000);
	    driver.findElement(By.cssSelector("button.btn")).click();
	    // Additional Line
	    Thread.sleep(1000);   
	    assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*test[\\s\\S]*$"));
	   
	    driver.findElement(By.cssSelector("i.icon-pencil")).click();
	    
	    for (int second = 0;; second++) {
	    	if (second >= 60) fail("timeout");
	    	try { if (isElementPresent(By.cssSelector("#editGroupForm > #description_field > dd > #description"))) break; } catch (Exception e) {}
	    	Thread.sleep(1000);
	    }
	 
	    driver.findElement(By.cssSelector("#editGroupForm > #description_field > dd > #description")).clear();
	    driver.findElement(By.cssSelector("#editGroupForm > #description_field > dd > #description")).sendKeys("Selenium");
	    driver.findElement(By.cssSelector("div.actual-modal > div.modal-footer > #submitGroup")).click();
	    // Additional Line
	    Thread.sleep(1000);
	    driver.findElement(By.cssSelector("div.actual-modal > div.modal-footer > button.btn")).click();
	    // Additional Line
	    Thread.sleep(1000);
	    driver.findElement(By.cssSelector("i.icon-trash")).click();
	    // Additional Line
	    Thread.sleep(1000);
	}
	
	private boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	
	
//    @Test
//    public void test() {	
//		String title = driver.getTitle();
//        assertEquals(title, "HTW.plus()");
//        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
//        
//        //WebDriverWait wait = new WebDriverWait(driver, 10);
//		//WebElement element = wait.until(loginPage.getPageLoadCondition());
//        
//        loginPage.login("test@example.de", "1234");
//        assertThat(loginPage.getFormContent()).contains("Nutzer oder Passwort nicht korrekt");
//    }
  
}

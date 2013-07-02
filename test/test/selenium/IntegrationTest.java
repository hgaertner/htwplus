package test.selenium;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

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
	
	@Test
	@Ignore
	public void testRegistration() throws InterruptedException{
		driver.get(baseUrl);
		
		// Implicit Wait
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		
		// Open Registration Modal
		WebElement registerLink = driver.findElement(By.id("registerLink"));
		registerLink.click();	
		
		// Get Fields 
		// Wait for Expected Condition
		WebElement firstnameField = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("firstname")));
		
		WebElement lastnameField = driver.findElement(By.id("lastname"));
		WebElement emailField = driver.findElement(By.xpath("//*[@id='email']"));
		WebElement passwordField = driver.findElement(By.name("password"));
		WebElement repeatPasswordField = driver.findElement(By.cssSelector("#repeatPassword_field dd input"));
	
		// Fill Form
		firstnameField.sendKeys("Bruce");
		lastnameField.sendKeys("Wayne");
		emailField.sendKeys("darkknight@gotham.com");
		passwordField.sendKeys("robin");
		repeatPasswordField.sendKeys("robin");
		
		// Submit
		WebElement submit = driver.findElement(By.id(UIMap.submitSignupButtonID));
		submit.click();	
		
		// To short Password
		WebElement passwordError = (new WebDriverWait(driver, 10))
			.until(ExpectedConditions.presenceOfElementLocated(By.className("error")));
		assertThat(passwordError.getText()).contains("mindestens 6 Zeichen");
		
		// Correction
		passwordField = driver.findElement(By.id("password"));
		repeatPasswordField = driver.findElement(By.id("repeatPassword"));
		passwordField.sendKeys("robin111");
		repeatPasswordField.sendKeys("robin111");
		submit = driver.findElement(By.id(UIMap.submitSignupButtonID));
		submit.click();	
		
		// Wait for Expected Condition
		(new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='registerModal']/div[3]/button")));
						
		// Successful Registration
		WebElement successMessage = driver.findElement(By.id("registerModal"));
		assertThat(successMessage.getText()).contains("Registrierung erfolgreich");
	}
	
	@Test
	public void testLoginPage() {
		// Important, do get login form in the first place
		driver.manage().deleteAllCookies();
		driver.get(baseUrl);
		
		LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
		loginPage.login("test@example.de", "1234");
		
		assertThat(loginPage.getFormContent()).contains(
				"Nutzer oder Passwort nicht korrekt");
		
		loginPage.login("test@example.de", "test");	
		assertThat(loginPage.isLoggedIn()).isTrue();
	
	}
	
	
	@Test
	@Ignore
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
	
  
}

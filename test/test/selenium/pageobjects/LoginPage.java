package test.selenium.pageobjects;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import test.selenium.UIMap;

public class LoginPage extends BasePage {
		
	@FindBy(xpath="//*[@id='loginForm']/input[1]")
	WebElement emailField;
	
	@FindBy(xpath="//*[@id='loginForm']/input[2]")
	WebElement passwordField;
	
	@FindBy(xpath="//*[@id='loginForm']/button")
	WebElement loginButton;
	
	@FindBy(id="loginForm")
	WebElement loginForm;
	
	public LoginPage(WebDriver driver) {
		super(driver);
		driver.get(UIMap.getFullUrl());
	}
	
	public ExpectedCondition<WebElement> getPageLoadCondition() {
		return ExpectedConditions.visibilityOf(emailField);
	}
	
	public void login(String email, String password) {
		emailField.sendKeys(email);
		passwordField.sendKeys(password);
		loginButton.click();
	}

	public String getFormContent() {
		return loginForm.getText();
	}
	
	public Boolean isLoggedIn(){
		Cookie cookie = driver.manage().getCookieNamed("PLAY_SESSION");
		if(cookie == null) {
			return false;
		} else {
			return true;
		}
	}
}

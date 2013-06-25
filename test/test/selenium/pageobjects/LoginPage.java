package test.selenium.pageobjects;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import play.Logger;

import com.thoughtworks.selenium.Selenium;

public class LoginPage {
	
	WebDriver driver;
	
	@FindBy(xpath="html/body/div[1]/div/div[3]/div/div/div[2]/form/input[1]")
	WebElement emailField;
	
	@FindBy(xpath="html/body/div[1]/div/div[3]/div/div/div[2]/form/input[2]")
	WebElement passwordField;
	
	@FindBy(xpath="html/body/div[1]/div/div[3]/div/div/div[2]/form/button")
	WebElement loginButton;
	
	@FindBy(id="loginForm")
	WebElement loginForm;
	
	public LoginPage(WebDriver driver) {
		this.driver = driver;
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
	
}

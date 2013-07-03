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

public abstract class BasePage {
	
	WebDriver driver;
	private String startUrl = null;
	
	public BasePage(WebDriver driver) {
		this.driver = driver;
	}
	
	public void setStartUrl(String startUrl){
		this.startUrl = startUrl;
	}
	
	public void gotoStartUrl(){
		if(startUrl != null) {
			driver.get(startUrl);
		}	
	}

	public abstract ExpectedCondition<WebElement> getPageLoadCondition();

}

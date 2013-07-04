package test.selenium.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

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

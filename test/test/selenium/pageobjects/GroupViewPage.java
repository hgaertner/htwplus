package test.selenium.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import test.selenium.UIMap;

public class GroupViewPage extends BasePage {
	
	public int id = 2800;
	
	@FindBy(xpath="//*[@id='content']/h1")
	WebElement groupTitle;
	
	@FindBy(id="postContent")
	WebElement postInput;
	
	@FindBy(xpath="//*[@id='addPost']/button")
	WebElement postButton;
	
	public GroupViewPage(WebDriver driver) {
		super(driver);

		LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
		loginPage.login("test@example.de", "test");
		
		driver.get(UIMap.getFullUrl() + "/gruppe/" + id);
	}

	@Override
	public ExpectedCondition<WebElement> getPageLoadCondition() {
		return ExpectedConditions.visibilityOf(driver.findElement(By.id("content")));
	}
	
	public String getGroupTitle() {
		return groupTitle.getText();
	}
	
	public void createNewPost(String text) {
		postInput.sendKeys(text);
		postButton.click();
	}
	
	public String getPageContent(){
		return driver.getPageSource();
	}

}

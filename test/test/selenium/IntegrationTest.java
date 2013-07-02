package test.selenium;
import org.junit.*;
import static org.junit.Assert.assertEquals;
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
    public void test() {
		WebDriver driver = new FirefoxDriver();
		driver.get("http://localhost:3333");
		String title = driver.getTitle();
        assertEquals(title, "HTW.plus()");
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        
        //WebDriverWait wait = new WebDriverWait(driver, 10);
		//WebElement element = wait.until(loginPage.getPageLoadCondition());
        
        loginPage.login("test@example.de", "1234");
        assertThat(loginPage.getFormContent()).contains("Nutzer oder Passwort nicht korrekt");
        driver.quit();
    }
  
}

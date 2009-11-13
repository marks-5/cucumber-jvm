package cuke4duke.webdriver;

import cuke4duke.After;
import cuke4duke.Before;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class WebDriverFacade {
    private static Constructor<WebDriver> driverConstructor = getDriverConstructor();

    private static Constructor<WebDriver> getDriverConstructor() {
        String driverName = System.getProperty("webdriver.impl", "org.openqa.selenium.htmlunit.HtmlUnitDriver");
        try {
            return (Constructor<WebDriver>) Thread.currentThread().getContextClassLoader().loadClass(driverName).getConstructor();
        } catch (Throwable problem) {
            problem.printStackTrace();
            throw new RuntimeException("Couldn't load " + driverName, problem);
        }
    }

    private final WebDriver browser;

    public WebDriverFacade() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        browser = driverConstructor.newInstance();
    }

    public WebDriver getWebDriver() {
        return browser;
    }

    @After
    public void closeBrowser() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        browser.close();
        browser.quit();
    }
}

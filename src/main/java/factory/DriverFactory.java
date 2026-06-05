package factory;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import utils.ReadConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * DriverFactory — Creates AppiumDriver based on platform in config.properties
 *
 * Supports: Android (UIAutomator2) and iOS (XCUITest)
 *
 * Usage:
 *   AppiumDriver driver = DriverFactory.createDriver(udid, deviceName, version);
 */
public class DriverFactory {

    private static final ReadConfig config = new ReadConfig();

    /**
     * Creates AppiumDriver based on platform from config.properties
     *
     * @param udid       Device UDID (from testng.xml parameter)
     * @param deviceName Device name (from testng.xml parameter)
     * @param version    Platform version (from testng.xml parameter)
     * @return           AppiumDriver (AndroidDriver or IOSDriver)
     */
    public static AppiumDriver createDriver(String udid, String deviceName, String version) {
        String platform = config.getPlatform();

        switch (platform.toLowerCase()) {
            case "android":
                return createAndroidDriver(udid, deviceName, version);
            case "ios":
                return createIOSDriver(udid, deviceName, version);
            default:
                throw new IllegalArgumentException(
                    "Unknown platform: [" + platform + "]. " +
                    "Set 'platform=Android' or 'platform=iOS' in config.properties");
        }
    }

    // ===== ANDROID =====
    private static AndroidDriver createAndroidDriver(String udid, String deviceName, String version) {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setCapability("platformName",     "Android");
        options.setCapability("automationName",   config.getAndroidAutomation());
        options.setCapability("deviceName",        deviceName);
        options.setCapability("appium:udid",       udid);
        options.setCapability("platformVersion",   version);
        options.setCapability("app",               System.getProperty("user.dir") + "/" + config.getAndroidAppPath());
        options.setCapability("appPackage",        config.getAndroidAppPackage());
        options.setCapability("appActivity",       config.getAndroidAppActivity());
        options.setCapability("autoGrantPermissions", config.isAutoGrantPermissions());
        options.setCapability("fullReset",         config.isFullReset());
        options.setCapability("newCommandTimeout", config.getNewCommandTimeout());

        // Unlock screen if configured
        if (!config.getAndroidUnlockType().isEmpty()) {
            options.setCapability("appium:unlockType", config.getAndroidUnlockType());
            options.setCapability("appium:unlockKey",  config.getAndroidUnlockKey());
        }

        try {
            URL serverUrl = new URL(config.getAppiumServerUrl() + ":" + config.getAppiumPort());
            AndroidDriver driver = new AndroidDriver(serverUrl, options);
            driver.manage().timeouts()
                  .implicitlyWait(Duration.ofSeconds(config.getImplicitTimeout()));
            return driver;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL. Check config.properties.", e);
        }
    }

    // ===== iOS =====
    private static IOSDriver createIOSDriver(String udid, String deviceName, String version) {
        XCUITestOptions options = new XCUITestOptions();

        options.setCapability("platformName",     "iOS");
        options.setCapability("automationName",   config.getIosAutomation());
        options.setCapability("deviceName",        deviceName);
        options.setCapability("udid",              udid);
        options.setCapability("platformVersion",   version);
        options.setCapability("app",               System.getProperty("user.dir") + "/" + config.getIosAppPath());
        options.setCapability("xcodeOrgId",        config.getIosXcodeOrgId());
        options.setCapability("xcodeSigningId",    config.getIosXcodeSigningId());
        options.setCapability("usePrebuiltWDA",    true);
        options.setCapability("appium:autoAcceptAlerts", true);
        options.setCapability("fullReset",         config.isFullReset());
        options.setCapability("newCommandTimeout", config.getNewCommandTimeout());

        try {
            URL serverUrl = new URL(config.getAppiumServerUrl() + ":" + config.getAppiumPort());
            IOSDriver driver = new IOSDriver(serverUrl, options);
            driver.manage().timeouts()
                  .implicitlyWait(Duration.ofSeconds(config.getImplicitTimeout()));
            return driver;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL. Check config.properties.", e);
        }
    }
}

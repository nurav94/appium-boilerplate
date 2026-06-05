package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ReadConfig — Reads all values from ConfigFiles/config.properties
 *
 * Usage:
 *   ReadConfig config = new ReadConfig();
 *   String platform = config.getPlatform();
 *   String udid     = config.getAndroidUdid();
 */
public class ReadConfig {

    private final Properties prop;
    private static final String CONFIG_PATH = "ConfigFiles/config.properties";

    public ReadConfig() {
        prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            prop.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to load config.properties from: " + CONFIG_PATH
                + "\nMake sure the file exists.", e);
        }
    }

    // ===== Helper =====
    private String get(String key) {
        String value = prop.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(
                "Missing config key: [" + key + "] in " + CONFIG_PATH);
        }
        return value.trim();
    }

    private String getOrDefault(String key, String defaultValue) {
        String value = prop.getProperty(key);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    // ===== PLATFORM =====
    public String getPlatform()           { return get("platform"); }
    public boolean isAndroid()            { return getPlatform().equalsIgnoreCase("Android"); }
    public boolean isIOS()                { return getPlatform().equalsIgnoreCase("iOS"); }

    // ===== ANDROID =====
    public String getAndroidDeviceName()  { return get("android.deviceName"); }
    public String getAndroidUdid()        { return get("android.udid"); }
    public String getAndroidVersion()     { return get("android.platformVersion"); }
    public String getAndroidAutomation()  { return getOrDefault("android.automationName", "UIAutomator2"); }
    public String getAndroidAppPath()     { return get("android.appPath"); }
    public String getAndroidAppPackage()  { return get("android.appPackage"); }
    public String getAndroidAppActivity() { return get("android.appActivity"); }
    public String getAndroidUnlockType()  { return getOrDefault("android.unlockType", ""); }
    public String getAndroidUnlockKey()   { return getOrDefault("android.unlockKey", ""); }

    // ===== iOS =====
    public String getIosDeviceName()      { return get("ios.deviceName"); }
    public String getIosUdid()            { return get("ios.udid"); }
    public String getIosVersion()         { return get("ios.platformVersion"); }
    public String getIosAutomation()      { return getOrDefault("ios.automationName", "xcuitest"); }
    public String getIosAppPath()         { return get("ios.appPath"); }
    public String getIosXcodeOrgId()      { return get("ios.xcodeOrgId"); }
    public String getIosXcodeSigningId()  { return get("ios.xcodeSigningId"); }

    // ===== APPIUM SERVER =====
    public String getAppiumServerUrl()    { return get("appium.serverUrl"); }
    public int    getAppiumPort()         { return Integer.parseInt(getOrDefault("appium.port", "4723")); }

    // ===== APP BEHAVIOUR =====
    public boolean isFullReset()          { return Boolean.parseBoolean(getOrDefault("app.fullReset", "true")); }
    public boolean isAutoGrantPermissions(){ return Boolean.parseBoolean(getOrDefault("app.autoGrantPermissions", "true")); }
    public int    getNewCommandTimeout()  { return Integer.parseInt(getOrDefault("app.newCommandTimeout", "60")); }

    // ===== TIMEOUTS =====
    public int getImplicitTimeout()       { return Integer.parseInt(getOrDefault("timeout.implicit", "10")); }
    public int getExplicitTimeout()       { return Integer.parseInt(getOrDefault("timeout.explicit", "15")); }
    public int getFluentTimeout()         { return Integer.parseInt(getOrDefault("timeout.fluent", "6")); }
    public int getFluentPollMs()          { return Integer.parseInt(getOrDefault("timeout.fluentPoll", "100")); }

    // ===== ADB =====
    public boolean isAdbLogcatEnabled()   { return Boolean.parseBoolean(getOrDefault("adb.logcat.enabled", "false")); }

    // ===== TEST DATA =====
    public String getTestDataPath()       { return getOrDefault("testdata.path", "TestData/sample_data.xlsx"); }
    public String getTestDataSheet()      { return getOrDefault("testdata.sheet", "LoginData"); }

    // ===== API =====
    public String getApiBaseUrl()         { return getOrDefault("api.baseUrl", "https://api.example.com"); }
}

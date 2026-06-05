# 📱 Appium Automation Boilerplate

A production-ready Appium automation framework supporting **Android and iOS**,
built with Java, TestNG, Maven, and Allure Reports.

Designed so that **anyone can set it up from scratch** and start automating
their mobile app within minutes.

---

## 🗂️ Table of Contents

1. [What This Framework Does](#what-this-framework-does)
2. [Prerequisites](#prerequisites)
3. [Installation Guide](#installation-guide)
4. [Project Structure](#project-structure)
5. [Configure for Your App](#configure-for-your-app)
6. [Running Tests](#running-tests)
7. [Viewing Allure Reports](#viewing-allure-reports)
8. [Writing Your First Test](#writing-your-first-test)
9. [Parallel Execution](#parallel-execution)
10. [Jenkins CI/CD Setup](#jenkins-cicd-setup)
11. [Troubleshooting](#troubleshooting)
12. [FAQ](#faq)

---

## What This Framework Does

| Feature | Details |
|---------|---------|
| **Platforms** | Android (UIAutomator2) + iOS (XCUITest) |
| **Language** | Java 11 |
| **Test Framework** | TestNG (annotations, parallel, data-driven) |
| **Build Tool** | Maven |
| **Reporting** | Allure Reports (steps, screenshots, video, ADB logs) |
| **Parallel Execution** | ThreadLocal driver — run on 3–5 devices simultaneously |
| **Data-Driven Testing** | Excel (.xlsx) via Apache POI + @DataProvider |
| **Retry on Failure** | RetryAnalyzer retries failed tests once automatically |
| **Screen Recording** | Video attached to every test in Allure |
| **ADB Logcat** | Android logs attached per test (optional) |
| **AI-Ready** | CLAUDE.md context file for AI-assisted test generation |

---

## Prerequisites

You need the following installed **before** cloning this project.

### 1. Java Development Kit (JDK 11+)

**Download:** https://adoptium.net

**Verify installation:**
```bash
java -version
# Should show: openjdk version "11.x.x" or higher
```

**Set JAVA_HOME** (if not set automatically):
```bash
# macOS/Linux — add to ~/.bashrc or ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home)
export PATH=$JAVA_HOME/bin:$PATH

# Windows — System Properties → Environment Variables
# JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-11.x.x
```

---

### 2. Apache Maven

**Download:** https://maven.apache.org/download.cgi

**Verify installation:**
```bash
mvn -version
# Should show: Apache Maven 3.x.x
```

**Set up** (if needed):
```bash
# macOS/Linux
export MAVEN_HOME=/path/to/maven
export PATH=$MAVEN_HOME/bin:$PATH
```

---

### 3. Node.js + Appium Server

**Install Node.js:** https://nodejs.org (LTS version)

**Verify:**
```bash
node -version    # v18.x.x or higher
npm -version     # 9.x.x or higher
```

**Install Appium:**
```bash
npm install -g appium
appium -version  # 2.x.x
```

**Install Appium Drivers:**
```bash
# For Android
appium driver install uiautomator2

# For iOS
appium driver install xcuitest

# Verify drivers installed
appium driver list
```

---

### 4. Android Setup (for Android testing)

**Install Android Studio:** https://developer.android.com/studio

After installation, open Android Studio:
- Go to **SDK Manager** → Install latest **Android SDK**
- Go to **AVD Manager** → Create a new virtual device (emulator)

**Set ANDROID_HOME:**
```bash
# macOS/Linux — add to ~/.bashrc or ~/.zshrc
export ANDROID_HOME=$HOME/Library/Android/sdk       # macOS
export ANDROID_HOME=$HOME/Android/Sdk               # Linux
export PATH=$ANDROID_HOME/emulator:$PATH
export PATH=$ANDROID_HOME/platform-tools:$PATH
export PATH=$ANDROID_HOME/tools:$PATH
```

**Verify ADB works:**
```bash
adb version
# Should show: Android Debug Bridge version x.x.x

# Start an emulator and verify it appears:
adb devices
# Should show: emulator-5554   device
```

---

### 5. iOS Setup (macOS only — for iOS testing)

**Requirements:** macOS + Xcode

**Install Xcode:** https://developer.apple.com/xcode/

**Install Xcode Command Line Tools:**
```bash
xcode-select --install
```

**Install additional tools:**
```bash
npm install -g appium-doctor    # check all iOS dependencies
brew install libimobiledevice   # for real device support
brew install ios-deploy         # for real device deployment
```

**Check iOS setup:**
```bash
appium-doctor --ios
# All items should show ✓
```

---

### 6. Appium Inspector (Recommended)

A GUI tool to inspect element locators in your app.

**Download:** https://github.com/appium/appium-inspector/releases

Use this to find `resource-id`, `content-desc`, `xpath` values for your app's elements.

---

## Installation Guide

### Step 1 — Clone the repository
```bash
git clone https://github.com/yourusername/appium-boilerplate.git
cd appium-boilerplate
```

### Step 2 — Install Maven dependencies
```bash
mvn clean install -DskipTests
# Downloads all required JARs from pom.xml
# First run takes 2–5 minutes
```

### Step 3 — Add your app
Place your app file inside the `installationFiles/` folder:
```
installationFiles/
  app-debug.apk    ← Android APK
  YourApp.app      ← iOS app (macOS only)
```

> **Note:** The `installationFiles/` folder is in `.gitignore` — APK/IPA files are not committed to Git (they are too large).

### Step 4 — Configure your device
Edit `ConfigFiles/config.properties` with your device details (see next section).

### Step 5 — Start Appium Server
```bash
appium
# Server starts at http://127.0.0.1:4723
# Keep this terminal open while running tests
```

### Step 6 — Start your emulator
```bash
# List available emulators
emulator -list-avds

# Start an emulator
emulator -avd Pixel_5_API_33

# Verify it appears
adb devices
```

### Step 7 — Run the sample test
```bash
mvn test -DsuiteXmlFile=TestSuites/Sanity.xml
```

---

## Project Structure

```
appium-boilerplate/
│
├── src/
│   ├── main/java/
│   │   ├── base/
│   │   │   └── Base.java              ← All utility methods + TestNG lifecycle
│   │   ├── factory/
│   │   │   └── DriverFactory.java     ← Creates Android/iOS driver
│   │   ├── listeners/
│   │   │   ├── RetryAnalyzer.java     ← Retries failed tests once
│   │   │   ├── RetryListener.java     ← Applies retry to all tests globally
│   │   │   └── TestListener.java      ← Logs pass/fail to console + extensible
│   │   └── utils/
│   │       ├── ReadConfig.java        ← Reads config.properties
│   │       └── ExcelUtils.java        ← Reads .xlsx test data files
│   │
│   └── test/java/
│       ├── pages/
│       │   └── SamplePage.java        ← Example page object (dual-platform)
│       └── tests/
│           └── SampleTest.java        ← Example test class (full pattern)
│
├── ConfigFiles/
│   └── config.properties              ← Device, app, timeout configuration
│
├── TestData/
│   └── sample_data.xlsx               ← Excel test data (DDT)
│
├── TestSuites/
│   ├── Sanity.xml                     ← Quick health check suite
│   └── Regression.xml                 ← Full regression (parallel)
│
├── installationFiles/                 ← Put your APK/IPA here (gitignored)
│
├── Documentations/
│   ├── AppiumAutomationGuide.md       ← Complete framework reference
│   └── ScreenTemplate.md              ← Template for screen documentation
│
├── allure-results/                    ← Generated after test run (gitignored)
│
├── pom.xml                            ← Maven dependencies and plugins
├── CLAUDE.md                          ← AI collaboration guide
├── .gitignore
└── README.md                          ← This file
```

---

## Configure for Your App

Open `ConfigFiles/config.properties` and update these values:

### Android Configuration
```properties
platform=Android
android.deviceName=Pixel_5_API_33        # Name shown in AVD Manager
android.udid=emulator-5554               # From: adb devices
android.platformVersion=13               # Android OS version
android.appPath=installationFiles/app-debug.apk

# Get appPackage and appActivity from your developer or:
# adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'
android.appPackage=com.your.app
android.appActivity=com.your.app.MainActivity
```

### iOS Configuration
```properties
platform=iOS
ios.deviceName=iPhone 14                 # Simulator name
ios.udid=YOUR_SIMULATOR_UDID             # From: xcrun simctl list
ios.platformVersion=16
ios.appPath=installationFiles/YourApp.app
ios.xcodeOrgId=YOUR_XCODE_ORG_ID        # From Apple Developer account
ios.xcodeSigningId=iPhone Developer
```

### How to find your Simulator UDID
```bash
xcrun simctl list devices | grep "iPhone 14"
# Copy the UDID from the output: (XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX)
```

### How to find appPackage and appActivity (Android)
```bash
# While your app is open on the emulator:
adb shell dumpsys window | grep -E 'mCurrentFocus'
# Output: mCurrentFocus=Window{... com.your.app/com.your.app.MainActivity}
```

---

## Running Tests

### Basic run
```bash
# Start Appium server first (in a separate terminal)
appium

# Run sanity suite
mvn test -DsuiteXmlFile=TestSuites/Sanity.xml

# Run regression suite
mvn test -DsuiteXmlFile=TestSuites/Regression.xml
```

### Clean run (recommended — clears previous results)
```bash
mvn clean test -DsuiteXmlFile=TestSuites/Sanity.xml
```

### Run a specific test class
```bash
mvn test -Dtest=SampleTest -DsuiteXmlFile=TestSuites/Sanity.xml
```

### Run a specific test method
```bash
mvn test -Dtest=SampleTest#tc01_Home_verifyHeaderIsDisplayed
```

### Run with custom device (override config.properties)
```bash
mvn test -DsuiteXmlFile=TestSuites/Sanity.xml \
         -Dandroid.udid=emulator-5556 \
         -Dandroid.platformVersion=14
```

---

## Viewing Allure Reports

### Option 1 — Serve report immediately after test run
```bash
allure serve allure-results
# Opens browser automatically at http://localhost:XXXX
```

### Option 2 — Generate static HTML report
```bash
allure generate allure-results --clean -o allure-report
allure open allure-report
```

### Install Allure CLI (if not installed)
```bash
# macOS
brew install allure

# Windows (via Scoop)
scoop install allure

# Linux
npm install -g allure-commandline
```

### What you'll see in the report
- **Overview** — Total pass/fail/skip with trends
- **Suites** — Tests grouped by class and suite
- **Behaviors** — Tests grouped by @Feature and @Story
- **Timeline** — Parallel execution visualized
- **Per test** — Steps, screenshots, video recording, ADB logcat

---

## Writing Your First Test

### Step 1 — Create a Page Object

Create `src/test/java/pages/LoginPage.java`:

```java
package pages;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import java.time.Duration;

public class LoginPage {

    public LoginPage(AppiumDriver driver) {
        PageFactory.initElements(
            new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    // Replace locator values with your app's actual locators
    // Use Appium Inspector to find them

    @AndroidFindBy(id = "com.your.app:id/email_input")
    @iOSXCUITFindBy(accessibility = "email_input")
    private WebElement emailField;

    @AndroidFindBy(id = "com.your.app:id/login_button")
    @iOSXCUITFindBy(accessibility = "login_button")
    private WebElement loginButton;

    public WebElement getEmailField()  { return emailField; }
    public WebElement getLoginButton() { return loginButton; }
}
```

### Step 2 — Create a Test Class

Create `src/test/java/tests/LoginTest.java`:

```java
package tests;

import base.Base;
import io.qameta.allure.*;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import pages.LoginPage;
import listeners.TestListener;

@Listeners({TestListener.class})
public class LoginTest extends Base {

    @Test(priority = 1, groups = "sanity")
    @Feature("Login")
    @Story("Valid Login")
    @Description("Verifies user can login with valid credentials")
    @Severity(SeverityLevel.CRITICAL)
    public void tc01_Login_verifyLoginWithValidCredentials() {

        SoftAssert softAssert = new SoftAssert();
        LoginPage page = new LoginPage(getDriver());

        Allure.step("Enter email", () -> sendKeys(page.getEmailField(), "user@test.com"));
        Allure.step("Tap Login button", () -> click(page.getLoginButton()));

        Allure.step("Verify home screen loads", () -> {
            if (isElementPresentIsDisplayed("Welcome", "", "yes")) {
                Allure.step("Login successful ✓", Status.PASSED);
            } else {
                Allure.step("Login failed ✗", Status.FAILED);
                getScreenshots("Login_Failed");
                softAssert.fail("Home screen not shown after login");
            }
        });

        softAssert.assertAll();
    }
}
```

### Step 3 — Add to TestSuites/Sanity.xml

```xml
<classes>
    <class name="tests.SampleTest"/>
    <class name="tests.LoginTest"/>   <!-- Add this line -->
</classes>
```

### Step 4 — Run
```bash
mvn clean test -DsuiteXmlFile=TestSuites/Sanity.xml
allure serve allure-results
```

---

## Parallel Execution

To run tests on multiple devices simultaneously:

### Step 1 — Start multiple emulators
```bash
# Start emulator 1
emulator -avd Pixel_5_API_33 &

# Start emulator 2
emulator -avd Pixel_6_API_34 &

# Verify both appear
adb devices
# emulator-5554   device
# emulator-5556   device
```

### Step 2 — Update Regression.xml

Uncomment the second device block in `TestSuites/Regression.xml` and set the correct UDID:

```xml
<suite parallel="tests" thread-count="2">

    <test name="Device1">
        <parameter name="udid" value="emulator-5554"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>

    <test name="Device2">
        <parameter name="udid" value="emulator-5556"/>
        <classes><class name="tests.HomeTest"/></classes>
    </test>

</suite>
```

### Step 3 — Run
```bash
mvn clean test -DsuiteXmlFile=TestSuites/Regression.xml
```

Both devices run simultaneously — each with their own isolated driver.

---

## Jenkins CI/CD Setup

### Prerequisites on Jenkins machine
- Java 11 installed
- Maven installed (configure in Global Tool Configuration)
- Appium server running (start as a background service)
- Android emulators available

### Create a Freestyle Job

1. **New Item** → Freestyle Project → name it "AppiumBoilerplate-Sanity"
2. **Source Code Management** → Git → your repo URL + credentials
3. **Build Triggers** → Build Periodically → `0 8 * * 1-5` (8 AM weekdays)
4. **Build Step** → Invoke Maven → Goals:
   ```
   clean test -DsuiteXmlFile=TestSuites/Sanity.xml
   ```
5. **Post-build Actions** → Allure Report → Results path: `allure-results`
6. **Save** → Build Now

### Install Allure Plugin in Jenkins

1. Manage Jenkins → Plugin Manager → Available
2. Search "Allure Jenkins Plugin" → Install
3. Manage Jenkins → Global Tool Configuration → Allure Commandline
4. Add → Name: "Allure" → Version: 2.27.0 → Save

### Jenkinsfile (Pipeline)

```groovy
pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk   'JDK 11'
    }

    triggers {
        cron('0 8 * * 1-5')  // 8 AM Mon–Fri
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/you/appium-boilerplate.git'
            }
        }
        stage('Run Tests') {
            steps {
                sh 'mvn clean test -DsuiteXmlFile=TestSuites/Sanity.xml'
            }
        }
        stage('Allure Report') {
            steps {
                allure([
                    reportBuildPolicy: 'ALWAYS',
                    results: [[path: 'allure-results']]
                ])
            }
        }
    }

    post {
        failure {
            emailext to: 'team@yourcompany.com',
                subject: "❌ Appium Tests FAILED: ${JOB_NAME}",
                body: "Check Jenkins: ${BUILD_URL}"
        }
    }
}
```

---

## Troubleshooting

### ❌ "Could not start Appium server"
```bash
# Check Appium is installed
appium -version

# Start Appium manually and check for errors
appium --log-level info

# Check port 4723 is free
lsof -i :4723    # macOS/Linux
netstat -ano | findstr :4723    # Windows
```

### ❌ "No devices found" / "Device not connected"
```bash
# Check device/emulator is running
adb devices
# Should show: emulator-5554   device

# If shows "offline" — restart ADB
adb kill-server
adb start-server
adb devices
```

### ❌ "App not installed" / "Cannot start activity"
```bash
# Verify APK exists in installationFiles/
ls installationFiles/

# Verify app package name
adb shell pm list packages | grep your.app.name

# Verify activity name
adb shell dumpsys window | grep mCurrentFocus
```

### ❌ "Element not found" — tests failing on locators
1. Open **Appium Inspector**
2. Start a session with your app
3. Click on the element in the screenshot
4. Copy the `resource-id`, `content-desc`, or XPath
5. Update your page object

### ❌ Tests fail in parallel but pass individually
This means shared state is being used. Check for:
- `static` fields used for test data → use local variables instead
- `new SoftAssert()` called once for multiple tests → create one per @Test method
- Page objects created once and reused → create a new instance per @Test method

### ❌ Allure report is empty
```bash
# Check allure-results/ folder was created after test run
ls allure-results/

# Make sure Allure CLI is installed
allure -version

# Generate report explicitly
allure generate allure-results --clean
allure open allure-report
```

### ❌ "fullReset" takes too long
Set `app.fullReset=false` in config.properties for faster runs during development.
Always use `true` in CI/CD for clean state.

---

## FAQ

**Q: Can I use this for iOS without a Mac?**
A: No. iOS automation with Appium requires macOS + Xcode. Android works on Mac, Windows, and Linux.

**Q: Can I run without Allure?**
A: Yes. Tests run normally — Allure steps are just annotations. Remove `allure-testng` dependency and the allure-maven plugin from pom.xml if you don't want reports.

**Q: How do I add API testing?**
A: REST Assured is already in pom.xml. Create a new test class, extend Base, use `given().when().then()` pattern. See `Documentations/AppiumAutomationGuide.md` for examples.

**Q: How many parallel devices are supported?**
A: As many as your machine can handle. Each emulator takes ~1.5GB RAM. A 16GB RAM machine can comfortably run 4–5 parallel emulators.

**Q: Can I use this for Selenium web testing too?**
A: The Base class is Appium-focused. For web, create a separate WebBase class using WebDriver, ChromeOptions, and the same TestNG lifecycle pattern.

**Q: How do I update Appium version?**
A: Update the `appium.version` property in pom.xml and run `mvn clean install -DskipTests`.

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -m "feat: add your feature"`
4. Push: `git push origin feature/your-feature`
5. Open a Pull Request

---

## Author

**Varun R**
Senior QA Automation Engineer | SDET | Automation Lead
- LinkedIn: [Your LinkedIn]
- GitHub: [Your GitHub]

---

## License

MIT License — free to use, modify, and distribute.

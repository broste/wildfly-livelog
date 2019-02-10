package de.broecheler.tools.wildfly.livelog;

import de.broecheler.tools.wildfly.livelog.testapp.api.LogEntry;
import de.broecheler.tools.wildfly.livelog.testapp.api.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.broecheler.tools.wildfly.livelog.testapp.api.LogLevel.INFO;
import static de.broecheler.tools.wildfly.livelog.testapp.api.LogLevel.WARN;
import static org.assertj.core.api.Assertions.assertThat;


public class LiveLogWebE2ETest {

    private static final String SERVER_NAME = "localhost";
    private static final String CSS_SELECTOR_LOG_FILE_LINE = "#logFileView .ace_line";
    private static final String CSS_SELECTOR_SUMMARY_ENTRY_LINE = "#logFileSummaryView li";
    private static final String ELEMENT_ID_WARNINGS_CHECKBOX = "warningsCb";
    private static final String ELEMENT_ID_AUTOSCROLL_CHECKBOX = "autoScrollCb";

    private WebDriver driver;

    @Before
    public void initDriver() {
        driver = new FirefoxDriver();
    }

    @After
    public void shutdownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void upon_page_load_existing_log_entries_are_displayed() {
        final String givenLogEntry = generateUniqueLogEntryOnServer(INFO);

        loadPage();

        waitForElement(CSS_SELECTOR_LOG_FILE_LINE, containingText(givenLogEntry));
        assertNoJavascriptErrors();
    }

    @Test
    public void new_log_entries_are_displayed_after_page_was_loaded() {
        loadPage();

        String newEntryText = generateUniqueLogEntryOnServer(INFO);
        // FIXME: Test flaky on Linux: StaleElementReference occurs sometimes when waiting for new test
        waitForElement(CSS_SELECTOR_LOG_FILE_LINE, containingText(newEntryText));
        assertNoJavascriptErrors();
    }

    @Test
    public void warnings_are_displayed_in_summary_view_and_can_be_hidden_using_warnings_checkbox() {
        loadPage();
        setCheckbox(ELEMENT_ID_WARNINGS_CHECKBOX, true);
        final String givenLogEntry1 = generateUniqueLogEntryOnServer(WARN);

        waitForElement(CSS_SELECTOR_LOG_FILE_LINE, containingText(givenLogEntry1));
        waitForElement(CSS_SELECTOR_SUMMARY_ENTRY_LINE, containingText(givenLogEntry1).and(cssDisplayBlock()));

        setCheckbox(ELEMENT_ID_WARNINGS_CHECKBOX, false);
        waitForElement(CSS_SELECTOR_SUMMARY_ENTRY_LINE, containingText(givenLogEntry1).and(cssDisplayNone()));

        final String givenLogEntry2 = generateUniqueLogEntryOnServer(WARN);
        waitForElement(CSS_SELECTOR_SUMMARY_ENTRY_LINE, containingText(givenLogEntry2).and(cssDisplayNone()));

        setCheckbox(ELEMENT_ID_WARNINGS_CHECKBOX, true);
        waitForElement(CSS_SELECTOR_SUMMARY_ENTRY_LINE, containingText(givenLogEntry1).and(cssDisplayBlock()));
        waitForElement(CSS_SELECTOR_SUMMARY_ENTRY_LINE, containingText(givenLogEntry2).and(cssDisplayBlock()));
    }

    @Test
    public void clicking_on_summary_item_scrolls_the_line_in_logfile_into_view() {
        loadPage();
        final String givenLogEntry = generateUniqueLogEntryOnServer(WARN);

        waitForElement(CSS_SELECTOR_LOG_FILE_LINE, containingText(givenLogEntry));
        WebElement summaryElement = waitForElement(CSS_SELECTOR_SUMMARY_ENTRY_LINE, containingText(givenLogEntry).and(cssDisplayBlock()));

        generateUniqueLogEntriesOnServer("some text", 50);
        waitForNoElementWith(CSS_SELECTOR_LOG_FILE_LINE, containingText(givenLogEntry));

        summaryElement.click();
        waitForElement(CSS_SELECTOR_LOG_FILE_LINE, containingText(givenLogEntry));
    }

    private void generateUniqueLogEntriesOnServer(String some_text, int numEntries) {
        for (int i = 1; i <= numEntries; i++) {
            createLogEntryOnServer(LogLevel.INFO, some_text + i);
        }
    }

    private String generateUniqueLogEntryOnServer(LogLevel logLevel) {
        final String givenLogEntry = "logEntry " + UUID.randomUUID();
        createLogEntryOnServer(logLevel, givenLogEntry);
        return givenLogEntry;
    }

    private void loadPage() {
        driver.get(String.format("http://%s:8080/livelog", SERVER_NAME));
        waitForTitle("LiveLog");
    }

    private void createLogEntryOnServer(LogLevel level, String message) {
        Response response = ClientBuilder.newClient()
                .target("http://localhost:8080/testapp/api/log")
                .request()
                .post(Entity.json(new LogEntry(level, message)));
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new IllegalStateException("Failed to create log entry");
        }
    }

    private void setCheckbox(String elementId, boolean checked) {
        WebElement warningsCheckbox = driver.findElements(By.id(elementId)).get(0);
        if (checked != warningsCheckbox.isSelected()) {
            warningsCheckbox.click();
        }
    }

    private void waitForNoElementWith(String cssSelector, Predicate<WebElement> condition) {
        waitFor(driver -> !findElement(driver, cssSelector, condition).isPresent());
    }

    private WebElement waitForElement(String cssSelector, Predicate<WebElement> condition) {
        waitFor(driver -> findElement(driver, cssSelector, condition).isPresent());
        return findElement(driver, cssSelector, condition).get();
    }

    private Optional<WebElement> findElement(WebDriver driver, String cssSelector, Predicate<WebElement> condition) {
        return driver.findElements(By.cssSelector(cssSelector)).stream().filter(condition).findFirst();
    }

    private void assertNoJavascriptErrors() {
        assertThat(driver.findElements(By.cssSelector("#jsErrors p")).stream().map(element -> element.getAttribute("innerHTML"))).as("JS errors").isEmpty();
    }

    private void waitForTitle(final String title) {
        waitFor(driver -> driver.findElement(By.tagName("title")).getAttribute("textContent").equals(title));
    }

    private void waitFor(Function<WebDriver, Boolean> condition) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until((ExpectedCondition<Boolean>) condition::apply);
    }

    private Predicate<WebElement> cssDisplayNone() {
        return webElement -> webElement.getCssValue("display").equals("none");
    }

    private Predicate<WebElement> cssDisplayBlock() {
        return webElement -> webElement.getCssValue("display").equals("block");
    }

    private Predicate<WebElement> containingText(String text) {
        return element -> element.getAttribute("innerText").contains(text);
    }

}

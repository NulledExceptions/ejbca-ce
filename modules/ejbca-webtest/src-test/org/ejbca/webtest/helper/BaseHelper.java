/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.webtest.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.ejbca.webtest.util.WebTestUtil;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * A base helper class for page operations of a web helper extensions.
 *
 * @version $Id: BaseHelper.java 30446 2018-11-09 10:16:38Z andrey_s_helmes $
 */
public class BaseHelper {

    private static final Logger log = Logger.getLogger(BaseHelper.class);

    protected WebDriver webDriver;

    /**
     * Built-in timeout for WebElement find.
     */
    public static final int DEFAULT_WAIT_TIMEOUT_SECONDS = 5;

    /**
     * Selector's switch to select an option by name or value.
     */
    public enum SELECT_BY {
        // Text of the option
        TEXT,
        // Value of the option
        VALUE;
    }

    /**
     * Public constructor.
     *
     * @param webDriver web driver.
     */
    public BaseHelper(final WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    /**
     * Finds child web elements by locator using the built-in timeout for the visible parent element. Throws
     *
     * @param groupId locator.
     *
     * @return a list of child web elements.
     */
    protected List<WebElement> findElements(final By groupId) {
        // Wait
        waitForElementBecomeVisibleByLocator(groupId);
        return webDriver.findElements(groupId);
    }

    /**
     * Finds an element by locator using the built-in timeout for the visibility check.
     *
     * @param elementId locator.
     *
     * @return a web element.
     */
    protected WebElement findElement(final By elementId) {
        // Wait
        waitForElementBecomeVisibleByLocator(elementId);
        return findElementWithoutWait(elementId);
    }

    /**
     * Finds an element by locator.
     *
     * @param elementId locator.
     *
     * @return a web element or null.
     */
    protected WebElement findElementWithoutWait(final By elementId) {
        try {
            return webDriver.findElement(elementId);
        }
        catch (NoSuchElementException ex) {
            log.debug("Cannot find WebElement [" + elementId.toString() + "]", ex);
        }
        return null;
    }

    /**
     * Finds an element by locator within given element.
     *
     * @param rootElement    an input element.
     * @param childElementId child locator.
     *
     * @return a web element or null.
     */
    protected WebElement findElement(final WebElement rootElement, final By childElementId) {
        assertNotNull("Root element cannot be null.", rootElement);
        // Wait
        waitForElementBecomeVisible(rootElement);
        try {
            return rootElement.findElement(childElementId);
        }
        catch (NoSuchElementException ex) {
            log.debug("Cannot find WebElement [" + childElementId.toString() + "] inside WebElement [" + rootElement.getTagName() + "]", ex);
        }
        return null;
    }

    /**
     * Finds child web elements by locator within given element.
     *
     * @param rootElement    an input element.
     * @param childElementId child locator.
     *
     * @return a list of child web elements.
     */
    protected List<WebElement> findElements(final WebElement rootElement, final By childElementId) {
        assertNotNull("Root element cannot be null.", rootElement);
        // Wait
        waitForElementBecomeVisible(rootElement);
        return rootElement.findElements(childElementId);
    }

    /**
     * Asserts a link (link, button, input, checkbox) by given locator exists and clicks it.
     *
     * @param linkId locator.
     */
    protected void clickLink(final By linkId) {
        final WebElement linkWebElement = findElement(linkId);
        assertNotNull("Page link was not found", linkWebElement);
        linkWebElement.click();
    }

    /**
     * Clicks a link (link, button, input, checkbox) if it exists.
     *
     * @param linkId locator.
     */
    protected void clickLinkIfExists(final By linkId) {
        final WebElement linkWebElement = findElement(linkId);
        if(linkWebElement != null) {
            linkWebElement.click();
        }
    }

    /**
     * Asserts a given input exists and fills a text into it.
     *
     * @param inputId locator.
     * @param inputString input text.
     */
    protected void fillInput(final By inputId, final String inputString) {
        final WebElement inputWebElement = findElement(inputId);
        assertNotNull("Page input was not found", inputWebElement);
        inputWebElement.clear();
        inputWebElement.sendKeys(inputString);
    }

    /**
     * Asserts a given textarea exists and fills a text into it.
     *
     * @param textareaId locator.
     * @param inputString input text.
     */
    protected void fillTextarea(final By textareaId, final String inputString, boolean doClear) {
        final WebElement textareaWebElement = findElement(textareaId);
        assertNotNull("Page textarea was not found", textareaWebElement);
        if(doClear) {
            textareaWebElement.clear();
        }
        textareaWebElement.sendKeys(inputString);
    }

    /**
     * Opens a given URL and asserts that actual URL matches the expected URI.
     *
     * @param webUrl      URL to open.
     * @param expectedUri URI to expect.
     */
    protected void openPageByUrlAndAssert(final String webUrl, final String expectedUri) {
        webDriver.get(webUrl);
        assertPageUri(expectedUri);
    }

    /**
     * Opens a given URL, clicks the link within opened page and asserts the resulting URL matches the expected URI.
     *
     * @param webUrl an URL to open.
     * @param pageLinkId link locator.
     * @param expectedUri URI to expect.
     */
    protected void openPageByLinkAndAssert(final String webUrl, final By pageLinkId, final String expectedUri) {
        webDriver.get(webUrl);
        clickLink(pageLinkId);
        assertPageUri(expectedUri);
    }

    /**
     * Asserts the actual URL matches the expected URI.
     *
     * @param expectedUri an URI to expect.
     */
    protected void assertPageUri(final String expectedUri) {
        assertEquals(
                "Wrong page URI expectation.",
                expectedUri,
                WebTestUtil.getUriPathFromUrl(webDriver.getCurrentUrl())
        );
    }

    /**
     * Selects a single option in the 'select' HTML element by name and asserts that the option is selected.
     *
     * @param selectId locator.
     * @param selectionOption option's names.
     */
    protected void selectOptionByName(final By selectId, final String selectionOption) {
        selectOptionsByName(selectId, Collections.singletonList(selectionOption), false);
    }

    /**
     * Selects a single option in the 'select' HTML element by value and asserts that the option is selected.
     *
     * @param selectId locator.
     * @param selectionOption option's value.
     */
    protected void selectOptionByValue(final By selectId, final String selectionOption) {
        selectOptionsByValue(selectId, Collections.singletonList(selectionOption), false);
    }

    /**
     * Selects options by name in the 'select' HTML element and asserts that all options are selected.
     *
     * @param selectId locator.
     * @param selectionOptions  a list of option names to select.
     */
    protected void selectOptionsByName(final By selectId, final List<String> selectionOptions) {
        selectOptionsByName(selectId, selectionOptions, true);
    }

    /**
     * Selects options by name in the 'select' HTML element and asserts that all options are selected.
     *
     * @param selectId locator.
     * @param selectionOptions  a list of option names to select.
     * @param useDeselectAll    deselection flag for the already selected options.
     */
    protected void selectOptionsByName(final By selectId, final List<String> selectionOptions, final boolean useDeselectAll) {
        final WebElement selectWebElement = findElement(selectId);
        assertNotNull("Page select was not found", selectWebElement);
        selectOptions(new Select(selectWebElement), selectionOptions, useDeselectAll, SELECT_BY.TEXT);
        // For assertion, reload the object as a selection may trigger the refresh/reload event and modify the DOM
        // which causes the org.openqa.selenium.StaleElementReferenceException
        final WebElement selectedWebElement = findElement(selectId);
        assertSelectionOfAllOptions(new Select(selectedWebElement), selectionOptions, SELECT_BY.TEXT);
    }

    /**
     * Selects options by value in the 'select' HTML element and asserts that all options are selected.
     *
     * @param selectId locator.
     * @param selectionOptions  A list of option values to select.
     * @param useDeselectAll deselection flag for the already selected options.
     */
    protected void selectOptionsByValue(final By selectId, final List<String> selectionOptions, final boolean useDeselectAll) {
        final WebElement selectWebElement = findElement(selectId);
        assertNotNull("Page select was not found", selectWebElement);
        selectOptions(new Select(selectWebElement), selectionOptions, useDeselectAll, SELECT_BY.VALUE);
        // For assertion, reload the object as a selection may trigger the refresh/reload event and modify the DOM
        // which causes the org.openqa.selenium.StaleElementReferenceException
        final WebElement selectedWebElement = findElement(selectId);
        assertSelectionOfAllOptions(new Select(selectedWebElement), selectionOptions, SELECT_BY.VALUE);
    }

    /**
     * Returns true if an element by a given locator is 'input', false otherwise.
     *
     * @param elementId locator.
     *
     * @return true if an element by a given locator is 'input', false otherwise.
     */
    protected boolean isInputElement(final By elementId) {
        return isInputElement(findElement(elementId));
    }

    /**
     * Returns true if an element is 'input', false otherwise.
     *
     * @param webElement a web element.
     *
     * @return true if an element is 'input', false otherwise.
     */
    protected boolean isInputElement(final WebElement webElement) {
        return webElement != null && webElement.getTagName().equalsIgnoreCase("input");
    }

    /**
     * Returns true if an element by a given locator is 'select', false otherwise.
     *
     * @param elementId locator.
     *
     * @return true if an element by a given locator is 'select', false otherwise.
     */
    protected boolean isSelectElement(final By elementId) {
        return isSelectElement(findElement(elementId));
    }

    /**
     * Returns true if an element is 'select', false otherwise.
     *
     * @param webElement a web element.
     *
     * @return true if an element is 'select', false otherwise.
     */
    protected boolean isSelectElement(final WebElement webElement) {
        return webElement != null && webElement.getTagName().equalsIgnoreCase("select");
    }

    /**
     * Returns true if an element by a given locator is 'td', false otherwise.
     *
     * @param elementId locator.
     *
     * @return true if an element by a given locator is 'td', false otherwise.
     */
    protected boolean isTdElement(final By elementId) {
        return isTdElement(findElement(elementId));
    }

    /**
     * Returns true if an element is 'td', false otherwise.
     *
     * @param webElement a web element.
     *
     * @return true if an element is 'td', false otherwise.
     */
    protected boolean isTdElement(final WebElement webElement) {
        return webElement != null && webElement.getTagName().equalsIgnoreCase("td");
    }

    /**
     * Asserts an element by locator exists.
     *
     * @param elementId      locator.
     * @param failureMessage failure message.
     */
    protected void assertElementExists(final By elementId, final String failureMessage) {
        if(findElement(elementId) == null) {
            fail(failureMessage);
        }
    }

    /**
     * Asserts an element by locator does not exist.
     *
     * @param elementId      locator.
     * @param failureMessage failure message.
     */
    protected void assertElementDoesNotExist(final By elementId, final String failureMessage) {
        if(findElementWithoutWait(elementId) != null) {
            fail(failureMessage);
        }
    }

    /**
     * Asserts a given element exists and returns its value (attribute 'value').
     *
     * @param elementId locator.
     *
     * @return element's value or null.
     */
    protected String getElementValue(final By elementId) {
        return getElementValue(findElement(elementId));
    }

    /**
     * Asserts a given element is not null and returns its value (attribute 'value').
     *
     * @param webElement non-null web element.
     *
     * @return element's value or null.
     */
    protected String getElementValue(final WebElement webElement) {
        if(webElement != null) {
            return webElement.getAttribute("value");
        }
        return null;
    }

    /**
     * Asserts a given element exists and returns its text.
     *
     * @param elementId locator.
     *
     * @return element's text or null.
     */
    protected String getElementText(final By elementId) {
        return getElementText(findElement(elementId));
    }

    /**
     * Asserts a given element is not null and returns its text.
     *
     * @param webElement webElement non-null web element.
     *
     * @return element's text or null.
     */
    protected String getElementText(final WebElement webElement) {
        if(webElement != null) {
            return webElement.getText();
        }
        return null;
    }

    /**
     * Asserts a given element exists and returns whether it is selected.
     *
     * @param elementId locator.
     *
     * @return true if element is selected, false otherwise.
     */
    protected boolean isSelectedElement(final By elementId) {
        return isSelectedElement(findElement(elementId));
    }

    /**
     * Asserts a given element is not null and returns whether it is selected.
     *
     * @param webElement non-null web element.
     *
     * @return true if element is selected, false otherwise.
     */
    protected boolean isSelectedElement(final WebElement webElement) {
        if(webElement != null) {
            return webElement.isSelected();
        }
        return false;
    }

    /**
     * Asserts a given element is not null and returns whether it is enabled.
     *
     * @param elementId locator.
     *
     * @return true if element is enabled, false otherwise.
     */
    protected boolean isEnabledElement(final By elementId) {
        return isEnabledElement(findElement(elementId));
    }

    /**
     * Asserts a given element is not null and returns whether it is enabled.
     *
     * @param webElement non-null web element.
     *
     * @return true if element is enabled, false otherwise.
     */
    protected boolean isEnabledElement(final WebElement webElement) {
        if(webElement != null) {
            return webElement.isEnabled();
        }
        return false;
    }

    /**
     * Returns the list of possible values for the non-null select.
     *
     * @param selectId locator.
     *
     * @return the list of values or null.
     */
    protected List<String> getSelectValues(final By selectId) {
        return getSelectValues(findElement(selectId));
    }

    /**
     * Returns the list of possible values for the non-null select.
     *
     * @param webElement non-null web element.
     *
     * @return the list of values or null.
     */
    protected List<String> getSelectValues(final WebElement webElement) {
        if(webElement != null) {
            final List<String> selectNames = new ArrayList<String>();
            final Select select = new Select(webElement);
            for(final WebElement selectOptionWebElement : select.getOptions()) {
                selectNames.add(getElementValue(selectOptionWebElement));
            }
            return selectNames;
        }
        return null;
    }

    /**
     * Returns the list of selected names for the non-null select.
     *
     * @param selectId locator.
     *
     * @return the list of selected names of a select or null.
     */
    protected List<String> getSelectSelectedNames(final By selectId) {
        return getSelectSelectedNames(findElement(selectId));
    }

    /**
     * Returns the list of selected names for the non-null select.
     *
     * @param webElement non-null web element.
     *
     * @return the list of selected names of a select or null.
     */
    protected List<String> getSelectSelectedNames(final WebElement webElement) {
        if(webElement != null) {
            final List<String> selectedNames = new ArrayList<String>();
            final Select select = new Select(webElement);
            for (final WebElement selected : select.getAllSelectedOptions()) {
                selectedNames.add(selected.getText());
            }
            return selectedNames;
        }
        return null;
    }

    /**
     * Asserts the appearance of the alert popup, its message and accepts/discards it.
     *
     * @param expectedAlertMessageText the expected alert's message.
     * @param isConfirmed a flag to accept or discard the alert.
     */
    protected void assertAndConfirmAlertPopUp(final String expectedAlertMessageText, boolean isConfirmed) {
        try {
            final Alert alert = webDriver.switchTo().alert();
            // Assert that the correct alert message is displayed (if not null)
            if(expectedAlertMessageText != null) {
                assertEquals("Unexpected alert message.", expectedAlertMessageText, alert.getText());
            }
            if(isConfirmed) {
                alert.accept();
            }
            else {
                alert.dismiss();
            }
            webDriver.switchTo().defaultContent();
        }
        catch (NoAlertPresentException e) {
            fail("Expected an alert but there was none");
        }
    }

    // Selects options of a select
    private void selectOptions(final Select selectObject, final List<String> options, final boolean useDeselectAll, final SELECT_BY selectBy) {
        if(useDeselectAll) {
            selectObject.deselectAll();
        }
        for (final String option : options) {
            switch (selectBy) {
                case TEXT:
                    selectObject.selectByVisibleText(option);
                    break;
                case VALUE:
                    selectObject.selectByValue(option);
                    break;
                default:
                    // Do nothing
            }
        }
    }

    // Asserts the given list of options was selected
    private void assertSelectionOfAllOptions(final Select selectObject, final List<String> options, final SELECT_BY selectBy) {
        for (final String option : options) {
            boolean isSelected = false;
            for (final WebElement selected : selectObject.getAllSelectedOptions()) {
                switch (selectBy) {
                    case TEXT:
                        // Assert that there is a selected option with the name
                        if (selected.getText().equals(option)) {
                            isSelected = true;
                        }
                        break;
                    case VALUE:
                        // Assert that there is a selected option with the value
                        if (getElementValue(selected).equals(option)) {
                            isSelected = true;
                        }
                        break;
                    default:
                        // Do nothing
                }
                if(isSelected) {
                    break;
                }
            }
            assertTrue("The option " + option + " was not found", isSelected);
        }
    }

    // Add a delay-timeout for DOM object search to make sure the document is fully loaded and we don't get a stale exception
    private void waitForElementBecomeVisibleByLocator(final By objectBy) {
        // A bug in EJBCA requires a wait here, otherwise it results in an XML Parsing Error
        final WebDriverWait wait = new WebDriverWait(webDriver, DEFAULT_WAIT_TIMEOUT_SECONDS);
        wait.until(ExpectedConditions.visibilityOfElementLocated(objectBy));
    }

    // Add a delay-timeout for DOM object search to make sure the document is fully loaded and we don't get a stale exception
    private void waitForElementBecomeVisible(final WebElement webElement) {
        // A bug in EJBCA requires a wait here, otherwise it results in an XML Parsing Error
        final WebDriverWait wait = new WebDriverWait(webDriver, DEFAULT_WAIT_TIMEOUT_SECONDS);
        wait.until(ExpectedConditions.visibilityOf(webElement));
    }

}
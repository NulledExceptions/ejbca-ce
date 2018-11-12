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
package org.ejbca.webtest.scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.lang.StringUtils;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.ejbca.webtest.WebTestBase;
import org.ejbca.webtest.helper.AuditLogHelper;
import org.ejbca.webtest.helper.CaHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;

/**
 * This test aims is to create CRLs and download CRLs and check if these operations are successful.
 * A new CA should always issue an (empty) CRL. This is done when the CA is created.
 * <br/>
 * Reference: <a href="https://jira.primekey.se/browse/ECAQA-6">ECAQA-6</a>
 * 
 * @version $Id: EcaQa6_CRLIssuance.java 30091 2018-10-12 14:47:14Z andrey_s_helmes $
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EcaQa6_CRLIssuance extends WebTestBase {

    private static WebDriver webDriver;
    // Helpers
    private static CaHelper caHelper;
    private static AuditLogHelper auditLogHelper;
    // Test Data
    private static class TestData {
        private static final String CA_NAME = "ECAQA-6-TestCA";
    }

    @BeforeClass
    public static void init() {
        // super
        beforeClass(true, null);
        webDriver = getWebDriver();
        // Init helpers
        caHelper = new CaHelper(webDriver);
        auditLogHelper = new AuditLogHelper(webDriver);
    }

    @AfterClass
    public static void exit() throws AuthorizationDeniedException {
        // Remove generated artifacts
        removeCaAndCryptoToken(TestData.CA_NAME);
        // super
        afterClass();
    }

    @Test
    public void stepA_addCA() {
        // Update default timestamp
        auditLogHelper.initFilterTime();
        // Add CA
        caHelper.openPage(getAdminWebUrl());
        caHelper.addCa(TestData.CA_NAME);
        caHelper.setValidity("1y");
        caHelper.saveCa();
        caHelper.assertExists(TestData.CA_NAME);
        // Verify Audit Log
        auditLogHelper.openPage(getAdminWebUrl());
        auditLogHelper.assertLogEntryByEventText(
                "CRL Store",
                "Success",
                TestData.CA_NAME,
                Arrays.asList(
                        "Stored CRL with CRLNumber=",
                        ", fingerprint=",
                        ", issuerDN 'CN=" + TestData.CA_NAME + "'."
                )
        );
        auditLogHelper.assertLogEntryByEventText(
                "CRL Create",
                "Success",
                TestData.CA_NAME,
                Arrays.asList(
                        "Created CRL with number ",
                        " for CA '" + TestData.CA_NAME + "' with DN 'CN=" + TestData.CA_NAME + "'."
                )
        );
    }

    @Test
    public void testB_crl() {
        // Update default timestamp
        auditLogHelper.initFilterTime();
        caHelper.openCrlPage(getAdminWebUrl());
        caHelper.assertCrlLinkWorks(TestData.CA_NAME);
        caHelper.openCrlPage(getAdminWebUrl());
        caHelper.clickCrlLinkAndAssertNumberIncreased(TestData.CA_NAME);
        // Verify Audit Log
        auditLogHelper.openPage(getAdminWebUrl());
        auditLogHelper.assertLogEntryByEventText(
                "CRL Store",
                "Success",
                TestData.CA_NAME,
                Arrays.asList(
                        "Stored CRL with CRLNumber=",
                        ", fingerprint=",
                        ", issuerDN 'CN=" + TestData.CA_NAME + "'."
                )
        );
        auditLogHelper.assertLogEntryByEventText(
                "CRL Create",
                "Success",
                TestData.CA_NAME,
                Arrays.asList(
                        "Created CRL with number ",
                        " for CA '" + TestData.CA_NAME + "' with DN 'CN=" + TestData.CA_NAME + "'."
                )
        );
    }
}
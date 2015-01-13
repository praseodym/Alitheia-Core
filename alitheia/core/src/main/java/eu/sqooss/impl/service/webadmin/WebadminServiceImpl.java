/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2008 - 2010 - Organization for Free and Open Source Software,  
 *                Athens, Greece.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package eu.sqooss.impl.service.webadmin;

import java.util.Hashtable;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.webadmin.WebadminService;

/**
 * This is the service which provides a web-based administration
 * interface. This user interface is used for the addition and removal
 * of projects from Alitheia Core and likewise for metric
 * plugins. Users of the WebAdmin interface are also able to receive
 * basic system information such as uptime, number of running/failed
 * jobs, etc.
 */
public class WebadminServiceImpl implements WebadminService {

    /**
     * The Velocity Engine is used to to provide services for
     * dynamically loading content into the page templates
     */
    private VelocityEngine ve;

    /**
     * Used for logging all system messages generated by the WebAdmin
     */
    private Logger logger = null;

    /**
     * This String is used to represent the current "message of the
     * day" available in the Alitheia Core
     */
    private String messageOfTheDay = null;

    private BundleContext bc;

    public WebadminServiceImpl() { }

    /**
     * Retrieves the "message of the day" String
     *
     * @return the message as a String
     */
    public String getMessageOfTheDay() {
        return messageOfTheDay;
    }

    /**
     * Sets the "message of the day" String
     *
     * @param s The text to be used as the "message of the day"
     */
    public void setMessageOfTheDay(String s) {
        messageOfTheDay = s;
    }

    /*
     * The utility method used for the initialization of the velocity engine.
     */
    private void initVelocity() {
        try {
            ve = new VelocityEngine();
            ve.setProperty("runtime.log.logsystem.class",
                           "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            ve.setProperty("runtime.log.logsystem.log4j.category",
                           Logger.NAME_SQOOSS_WEBADMIN);
            String resourceLoader = "classpath";
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, resourceLoader);
            ve.setProperty(resourceLoader + "." + RuntimeConstants.RESOURCE_LOADER + ".class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        }
        catch (Exception e) {
            logger.error("Velocity initialization",e);
        }
    }

	@Override
	public void setInitParams(BundleContext bc, Logger l) {
		this.logger = l;
		this.bc = bc;
	}

	@Override
	public boolean startUp() {        
        // Get a reference to the HTTPService, and then its object
        HttpService sobjHTTPService = null;
        ServiceReference srefHTTPService = bc.getServiceReference(
            HttpService.class.getName());
        
        if (srefHTTPService != null) {
            sobjHTTPService = (HttpService) bc.getService(srefHTTPService);
        }
        else {
            logger.error("Could not find a HTTP service!");
            return false;
        }

        initVelocity();
        
        // Register the front-end servlets
        if (sobjHTTPService != null) {
            try {
                sobjHTTPService.registerServlet(
                    "/",
                    new AdminServlet(bc, this, logger, ve),
                    new Hashtable(),
                    null);
            }
            catch (Exception e) {
                logger.error("AdminServlet",e);
                return false;
            }
        }
        return true;
	}
}


// vi: ai nosi sw=4 ts=4 expandtab


package it.infn.ct.MyJobs;

/**
 * ************************************************************************
 * Copyright (c) 2011: Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Consorzio COMETA (COMETA), Italy
 *
 * See http://www.infn.it and and http://www.consorzio-cometa.it for details on
 * the copyright holders.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * **************************************************************************
 */
import com.google.common.net.InetAddresses;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.model.Company;
//import com.liferay.portal.model.PortletPreferences;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserServiceUtil;
import com.liferay.portal.util.PortalUtil;
import it.infn.ct.GridEngine.JobService.JobCheckStatusService;
import it.infn.ct.GridEngine.UsersTracking.ActiveInteractions;
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.*;

/**
 * MyJobs Portlet Class
 */
public class MyJobs extends GenericPortlet {

    private final Log _log = LogFactoryUtil.getLog(MyJobs.class);

    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        PortletPreferences portletPreferences = (PortletPreferences) request.getPreferences();
        String Epr1 = (String) request.getParameter("EparaM1");
        String Epr3 = (String) request.getParameter("EparaM3");
        String Epr4 = (String) request.getParameter("EparaM4");

        boolean fgEnabled = ParamUtil.getBoolean(request, "fgEnabled", false);
        String fgHost = ParamUtil.getString(request, "fgHost", null);
        String fgPort = ParamUtil.getString(request, "fgPort", null);
        String fgAPIVer = ParamUtil.getString(request, "fgAPIVer", null);
        if (_log.isDebugEnabled()) {
            _log.debug("==========Portlet Preferences==========");
            _log.debug("fgEnabled: " + fgEnabled);
            _log.debug("fgHost: " + fgHost);
            _log.debug("fgPort: " + fgPort);
            _log.debug("fgAPIVer: " + fgAPIVer);
            _log.debug("=======================================");
        }

        boolean storeFGPreferences = true;
        if (fgEnabled) {
            if (fgHost != null && !fgHost.isEmpty()) {
                try {
                    InetAddress.getByName(fgHost);
                } catch (UnknownHostException ex) {
                    _log.warn(fgHost + " is not a valid hostname, trying if is a valid IP address...");
                    try {
                        InetAddresses.forString(fgHost);
                        _log.info(fgHost + " is valid IP address!");
                    } catch (IllegalArgumentException e) {
                        _log.warn(e.getMessage());
                        SessionErrors.add(request, "no-valid-host");
                        storeFGPreferences = false;
                    }
                }
            } else {
                _log.warn("Didn't specify a FG host!");
                storeFGPreferences = false;
                SessionErrors.add(request, "no-host");
            }

            int port;
            if (fgPort != null && !fgPort.isEmpty()) {
                try {
                    port = Integer.parseInt(fgPort);
                    if (!(port > 0) || !(port <= 65535)) {
                        _log.warn("Invalid port number port!");
                        storeFGPreferences = false;
                        SessionErrors.add(request, "no-valid-port");
                    }
                } catch (NumberFormatException ex) {
                    _log.warn("Only digits are allowed for FG port!");
                    storeFGPreferences = false;
                    SessionErrors.add(request, "digits-port");
                }
            } else {
                _log.warn("Didn't specify a FG port!");
                storeFGPreferences = false;
                SessionErrors.add(request, "no-port");
            }

            if (fgAPIVer == null || fgAPIVer.isEmpty()) {
                _log.warn("FG API Versions is mandatory.");
                storeFGPreferences = false;
                SessionErrors.add(request, "no-api-vers");
            }
        }

        _log.debug(storeFGPreferences);
        portletPreferences.setValue("singolo1", Epr1);
        portletPreferences.setValue("singolo3", Epr3);
        portletPreferences.setValue("singolo4", Epr4);

        if (storeFGPreferences) {
            portletPreferences.setValue("fgEnabled", String.valueOf(fgEnabled));
            portletPreferences.setValue("fgHost", String.valueOf(fgHost));
            portletPreferences.setValue("fgPort", String.valueOf(fgPort));
            portletPreferences.setValue("fgAPIVer", String.valueOf(fgAPIVer));
        }

        portletPreferences.store();

    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        super.serveResource(request, response);
    }

    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        try {

            PortletPreferences portletPreferences = (PortletPreferences) request.getPreferences();
            response.setContentType("text/html");
            User user = UserServiceUtil.getUserById(Long.parseLong(request.getRemoteUser()));
            Company company = PortalUtil.getCompany(request);
            UsersTrackingDBInterface utdbi = new UsersTrackingDBInterface();

            boolean checkJobStatus = (portletPreferences.getValue("singolo4", "")).equals("true");

            if (checkJobStatus) {
                JobCheckStatusService jobCheckStatusService = null;
                try {
                    jobCheckStatusService = InitialContext.<JobCheckStatusService>doLookup("JobCheckStatusService");
                    jobCheckStatusService.startJobCheckStatusThread(user.getScreenName(), "/tmp");
                } catch (NamingException ex) {
                    System.out.println("Cannot get JobCheckStatusService: " + ex);
                } catch (Exception ex) {
                    System.out.println("Cannot get JobCheckStatusService: " + ex);
                }
            }
            Vector<ActiveInteractions> othersList = new Vector<ActiveInteractions>();

            boolean fgEnabled = portletPreferences.getValue("fgEnabled", "").equals("true");
            if (fgEnabled){
                String fgHost = portletPreferences.getValue("fgHost", "");
                String fgPort = portletPreferences.getValue("fgPort", "");
                String fgAPIVer = portletPreferences.getValue("fgAPIVer", "");
                FGMyJobs fgMyJobs = new FGMyJobs(fgHost, fgPort, fgAPIVer, user.getScreenName());
                try {
                    fgMyJobs.generateActiveInteractionLists();
                    othersList = fgMyJobs.getJobList();
                    request.setAttribute("fg-error", "");
                    request.setAttribute("fgHost", fgHost);
                    request.setAttribute("fgPort", fgPort);
                    request.setAttribute("fgAPIVer", fgAPIVer);

                } catch (FGMyJobs.FuturegatewayException ex) {
                    request.setAttribute("fg-error", "fg-error");
                    _log.error(ex.getMessage());
                }
            }
//    System.out.println("DONE list:");
//            Vector<ActiveInteractions> doneList = fgMyJobs.getJobListDone();

//            doneList.addAll(utdbi.getDoneInteractionsByName(user.getScreenName()));
            othersList.addAll(utdbi.getActiveInteractionsByName(user.getScreenName()));

            request.setAttribute("jobList", othersList);
            request.setAttribute("jobListdone", utdbi.getDoneInteractionsByName(user.getScreenName()));
            request.setAttribute("vo", portletPreferences.getValue("singolo1", ""));
            request.setAttribute("proxy", portletPreferences.getValue("singolo3", ""));
            request.setAttribute("check", portletPreferences.getValue("singolo4", ""));
        } catch (PortalException ex) {
            Logger.getLogger(MyJobs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SystemException ex) {
            Logger.getLogger(MyJobs.class.getName()).log(Level.SEVERE, null, ex);
        }

        PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/MyJobs_view.jsp");
        dispatcher.include(request, response);

    }

    public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        PortletPreferences portletPreferences = request.getPreferences();
        request.setAttribute("EParaM1", portletPreferences.getValue("singolo1", ""));
        request.setAttribute("EParaM3", portletPreferences.getValue("singolo3", ""));
        request.setAttribute("EParaM4", portletPreferences.getValue("singolo4", ""));

        request.setAttribute("fgEnabled", Boolean.parseBoolean(portletPreferences.getValue("fgEnabled", "false")));
        request.setAttribute("fgHost", portletPreferences.getValue("fgHost", ""));
        request.setAttribute("fgPort", Integer.parseInt(portletPreferences.getValue("fgPort", "-1")));
        request.setAttribute("fgAPIVer", portletPreferences.getValue("fgAPIVer", ""));

        include("/WEB-INF/jsp/editMode.jsp", request, response);
    }

    protected void include(String path, RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(path);
        portletRequestDispatcher.include(renderRequest, renderResponse);

    }

    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    }
}

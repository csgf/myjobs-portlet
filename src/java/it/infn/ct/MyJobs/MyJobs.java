package it.infn.ct.MyJobs;

/**************************************************************************
Copyright (c) 2011:
Istituto Nazionale di Fisica Nucleare (INFN), Italy
Consorzio COMETA (COMETA), Italy

See http://www.infn.it and and http://www.consorzio-cometa.it for details on the
copyright holders.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
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
import java.util.Vector;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.*;

/**
 * MyJobs Portlet Class
 */
public class MyJobs extends GenericPortlet {
    
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        PortletPreferences portletPreferences = (PortletPreferences) request.getPreferences();
        String Epr1 = (String) request.getParameter("EparaM1");
        String Epr3 = (String) request.getParameter("EparaM3");
        String Epr4 = (String) request.getParameter("EparaM4");
        portletPreferences.setValue("singolo1", Epr1);
        portletPreferences.setValue("singolo3", Epr3);
        portletPreferences.setValue("singolo4", Epr4);
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

            FGMyJobs fgMyJobs = new FGMyJobs("151.97.41.48","8888","v1.0","mtorrisi");
            fgMyJobs.generateActiveInteractionLists();
//    System.out.println("DONE list:");
//            Vector<ActiveInteractions> doneList = fgMyJobs.getJobListDone();
            Vector<ActiveInteractions> othersList = fgMyJobs.getJobList();

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

        include("/WEB-INF/jsp/editMode.jsp", request, response);
    }

    protected void include(String path, RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(path);
        portletRequestDispatcher.include(renderRequest, renderResponse);

    }

    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    }
}

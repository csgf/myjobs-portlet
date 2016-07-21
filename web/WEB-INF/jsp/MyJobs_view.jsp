<%            /**
     * ************************************************************************
     * Copyright (c) 2011: Istituto Nazionale di Fisica Nucleare (INFN), Italy
     * Consorzio COMETA (COMETA), Italy
     *
     * See http://www.infn.it and and http://www.consorzio-cometa.it for details
     * on the copyright holders.
     *
     * Licensed under the Apache License, Version 2.0 (the "License"); you may
     * not use this file except in compliance with the License. You may obtain a
     * copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
     * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
     * License for the specific language governing permissions and limitations
     * under the License.
     * **************************************************************************
     */
    /**
     *
     *
     * @author s.monforte - r.ricceri
     */

%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayPortlet"%>
<%@page import="java.util.jar.Attributes.Name"%>
<%@page import="com.liferay.portal.theme.ThemeDisplay"%>
<%@page import="com.liferay.portal.kernel.util.WebKeys"%>
<%@page import="com.liferay.portal.model.Company"%>
<%@page import="com.liferay.portal.util.PortalUtil;"%>
<%@page import="it.infn.ct.GridEngine.Job.JSagaJobSubmission"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Vector"%>
<%@page import="com.google.common.base.Predicate"%>
<%@page import="com.google.common.collect.Iterables"%>
<%@page import="com.google.common.collect.Lists"%>
<%@page import="it.infn.ct.GridEngine.UsersTracking.ActiveInteractions"%>
<%@page import="com.liferay.portal.service.UserServiceUtil"%>
<%@page import="com.liferay.portal.model.User"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="javax.portlet.*"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>
<portlet:defineObjects />
<style type="text/css">
    a.tab {color:#B22222;}
    a.tab:hover {color:#B22222;}
    a.tab:link {color:#B22222;}
    @media screen {
        #view tbody td span { display: none;}
    }
    @media print {
        #view tbody td img { display: none;}
        #view tbody td span { display: block;}
    }
</style>
<%  PortletPreferences prefs = renderRequest.getPreferences();
    String tabNames = "Active Jobs List,Done Jobs List";
    String tabs1 = ParamUtil.getString(request, "tabs1", "Active Jobs List");
    PortletURL url = renderResponse.createRenderURL();
    pageContext.setAttribute("tabs1", tabs1);

%>


<%! private class supportsGateway implements Predicate<ActiveInteractions> {

        public supportsGateway(String gw) {
            this.value = gw;
        }

        public boolean apply(ActiveInteractions ai) {
            return value.equals(ai.getInteractionInfos()[1]) || "FutureGateway".equals(ai.getInteractionInfos()[1]);
        }
        private String value;
    }
%>

<%! private class isJobStatus implements Predicate<String[]> {

        public isJobStatus(String s) {
            this.status = s;
        }

        public boolean apply(String[] i) {
            return status.equals(i[5]);
        }
        private String status;
    }
%>
<%  User user = UserServiceUtil.getUserById(Long.parseLong(request.getRemoteUser()));
    Vector<ActiveInteractions> aiList = (Vector<ActiveInteractions>) request.getAttribute("jobList");
    Vector<ActiveInteractions> aiListDone = (Vector<ActiveInteractions>) request.getAttribute("jobListdone");
    Company company = PortalUtil.getCompany(request);
    String fgError = (String) request.getAttribute("fg-error");
    System.out.println("CICCIO: " + fgError);
%>

<liferay-ui:tabs
names="<%= tabNames%>"
url="<%= url.toString()%>"
    />

<c:if test="<%=!fgError.isEmpty()%>">
    <div class="portlet-msg-info">
        <liferay-ui:message key="Unable to contact Futuregateway" />
    </div>
</c:if>
<c:choose>
    <c:when test="${tabs1 == 'Active Jobs List'}" >
        <script type="text/javascript">
            function fnFormatDetails(text)
            {
                var sOut = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">';
                sOut += '<tr><td>'+text+'</td></tr>';
                sOut += '</table>';

                return sOut;
            }
 
            var oTable;
            function detailsText(sValue){
                var e = new RegExp(".*<span>(.*)</span>");
                var m = e.exec(sValue);
                if ( m != null ) {
                    return m[1];

                }
                return sValue;
            }
            Array.prototype.unique =                                                                                                                                                           
             function() {                                                                                                                                                                     
                   var a = [];                                                                                                                                                                    
                   var l = this.length;                                                                                                                                                           
                   for(var i=0; i<l; i++) {                                                                                                                                                       
                         for(var j=i+1; j<l; j++) {                                                                                                                                                   
                               // If this[i] is found later in the array                                                                                                                                  
                               if (this[i] === this[j])                                                                                                                                                   
                                     j = ++i;                                                                                                                                                                 
                         }                                                                                                                                                                            
                         a.push(this[i]);                                                                                                                                                             
                   }                                                                                                                                                                              
                   return a;                                                                                                                                                                      
             };
            TableTools.BUTTONS.download = {
                "sAction": "text",
                "sTag": "default",
                "sFieldBoundary": "",
                "sFieldSeperator": "\t",
                "sNewLine": "<br>",
                "sToolTip": "",
                "sButtonClass": "DTTT_button_text download_set",
                "sButtonClassHover": "DTTT_button_text_hover",
                "sButtonText": "Download Job output",
                "mColumns": "all",
                "bHeader": true,
                "bFooter": true,
                "oParams":  { 
                    mode : "set", 
                    cn : "<%=user.getScreenName()%>",
                    Path : "/tmp"
                },
                "sDiv": "",
                "fnMouseover": null,
                "fnMouseout": null,
                "fnInit": function(button,config) {
                    var descriptions = new Array(); 
                    descriptions.push("<option value='all'>All</option>");                                                                                                                                           
                             $.map( $("#view").dataTable().fnGetData(), function(v, i) {     
                                if (v[3] != "RUNNING" && v[3] != "SUBMITTED" && v[3] != "error")                                                                                   
                                    descriptions.push("<option value='"+v[1]+"'>"+v[1]+"</option>");                                                                                                   
                               });                                                                                                                                                                        
                               descriptions = descriptions.unique();
                    $('body').append(                                                                                                          
                    $('<div>', {id : "filter"}).hide().html(                                                                                   
                    "<select style='width:200px' name='ds'>" + descriptions.join() + "</select>"                                       
                ));
                   
                },
        
                "fnSelect": null,
                "fnComplete": null,
                "fnClick": function(button, config) {                                                                                                  
                           $('div#filter').dialog({                                                                                                           
                                       draggable: false,                                                                                                          
                                       resizable: false,                                                                                                          
                                       title: "Select a job set description",                                                                             
                                       maxWidth: 300,                                                                                                             
                                       modal:true,                                                                                                                
                                       buttons: {                                                                                                                 
                                                   "Ok" : function(){                                                                                                 
                                                       var val = $('#filter select option:selected').val();
                        
                                if(val =="all") config.oParams.mode = "all";
                                else config.oParams.mode="set";
                        
                                var url = "<%=renderRequest.getContextPath()%>/jobOutpuRetrive?" + 
                                    $.param(config.oParams) + "&" + $.param({ 'ds' : val}); 
                                window.open(url);
                                document.location.reload(true);
                                $('div#filter').dialog('close');                                                                                   
                                                   }                                                                                                                  
                               }                                                                                                                                  
                         });                                                                                                                                  
                   }
            };

         
            $.fn.dataTableExt.oJUIClasses.sSortAsc  = "chain-table-sort-header ui-state-active";
            $.fn.dataTableExt.oJUIClasses.sSortDesc  = "chain-table-sort-header ui-state-active";
            var oTable;
            $(document).ready(function() {
                oTable = $('#view').dataTable( {
                    "sDom": '<"H"<"clear"T>f<"clear"l><"right"p>t<"F"ip>',
                    "aaSorting": [[ 2, "desc" ]],
                    "bRetrieve": true,
                    "bJQueryUI": true,
                    "oTableTools": {
                        "sSwfPath": "<%=renderRequest.getContextPath()%>/datatables/extras/TableTools/media/swf/copy_cvs_xls_pdf.swf",
                        "aButtons": [
                            "copy",
                            "print",
                            
                            {
                                "sExtends":    "collection",
                                "sButtonText": "Save",
                                "aButtons":    [ "csv", "pdf" ]
                            },
                            "download"   
                        ]
                    },
                    "sPaginationType": "full_numbers"
                });
                new FixedHeader(oTable);
                $('#view tbody td img.more').live('click', function () {

                    var nTr = $(this).parents('tr')[0];
                    if ( oTable.fnIsOpen(nTr) )
                    {
                        /* This row is already open - close it */
                        this.src = "http://datatables.net/release-datatables/examples/examples_support/details_open.png";
                        oTable.fnClose(nTr);
                    }
                    else
                    {
                        /* Open this row */
                        var text = $("span",$(this).parent()).html();
                        this.src = "http://datatables.net/release-datatables/examples/examples_support/details_close.png";
                        oTable.fnOpen( nTr, fnFormatDetails(text), 'details' );
                    }
                } );
                
            });
           
            
        </script>
        <%
            boolean supportsGateway = false;

            if (aiList != null && !aiList.isEmpty()) {
                try {
                    Iterables.find(aiList, new supportsGateway(company.getName()));
                    supportsGateway = true;
                } catch (java.util.NoSuchElementException e) {
                }
            }
            if (supportsGateway) {
                
        %>
        <div>
            <span style="font-size:12px;">
                The table below shows the status of your jobs.<br>
                Statuses are automatically updated every 15 minutes so there is no need
                to reload this page more frequently. However, if you don't see your jobs
                in the table within a reasonable amount of time (a couple of hours at
                most), click on Help in the the MyWorkspace portlet and notify us the
                problem.<br>
                Once your jobs have finished, you have 96 hours to retrieve their
                output. Beyond that time, the output of your jobs will automatically be
                deleted from the Science Gateway in order not to fill its storage with
                undesired stuff.
            </span><br><br><br>

        </div>
        <table id="view" style="width:100%">
            <thead>
                <tr>
                    <th>
                        info job
                    </th>
                    <th>
                        Application Name
                    </th>
                    <th>
                        User Description
                    </th>
                    <th>
                        Started on (UTC)
                    </th>
                    <th>
                        Status
                    </th>

                </tr>
            </thead>
            <tbody>


                <%
                    for (ActiveInteractions ai : aiList) {
                        boolean isCollection = ai.getSubJobs() != null;
                        int nSubJobs = 0;
                        int nSubJobsDONE = 0;
                        int nSubJobsRUNNING = 0;
                        if (isCollection) {
                            nSubJobs = ai.getSubJobs().size();
                            nSubJobsDONE = Lists.newArrayList(
                                    Iterables.filter(ai.getSubJobs(), new isJobStatus("DONE"))).size();
                            nSubJobsRUNNING = Lists.newArrayList(
                                    Iterables.filter(ai.getSubJobs(), new isJobStatus("RUNNING"))).size();
                            
                        }

                        if (!ai.getInteractionInfos()[1].equals("FutureGateway") && !ai.getInteractionInfos()[1].equals(company.getName())) {
                            continue;
                        }
                        String status = ai.getInteractionInfos()[5];
                        if (status.equals("DONE")) {
                            
                            String URL = renderRequest.getContextPath() + "/jobOutpuRetrive?mode=single&futuregateway="+ai.getInteractionInfos()[1].equals("FutureGateway")+"&DBid=" + java.net.URLEncoder.encode(ai.getInteractionInfos()[0], "UTF-8")
                                    + "&Path=" + java.net.URLEncoder.encode("/tmp", "UTF-8");
                            String UrlColl = renderRequest.getContextPath() + "/jobOutpuRetrive?mode=collection&DBid=" + java.net.URLEncoder.encode(ai.getInteractionInfos()[0], "UTF-8")
                                    + "&Path=" + java.net.URLEncoder.encode("/tmp", "UTF-8");
                            System.out.println("#######prova log ##########ritaaaaaaaaaa" + ai.toString());
                %>
                <tr><td>

                        <%
                            if (isCollection) {
                        %>
                        Job Collections

                        <% }%>
                    </td>
                    <td align="center"><%= ai.getInteractionInfos()[2]%></td>
                    <td align="center"><%= ai.getInteractionInfos()[3]%></td>
                    <td align="center"><%= ai.getInteractionInfos()[4]%></td>
                    <td align="center">
                        <%
                            if (isCollection) {
                        %>
                        <a href="<%=UrlColl%>"><img   onmouseover=""src="<%=renderRequest.getContextPath()%>/datatables/media/images/download.png" width="24" height="24" /></a></td>
                    <% } else {%>
                         <a href="<%=URL%>"><img onmouseover=""src="<%=renderRequest.getContextPath()%>/datatables/media/images/download.png" width="24" height="24" /></a></td>   
                    <% }%>
                </tr>
                <%
                } //IF STATUS DONE CLOSE
                else {
                %>

                <tr><td>
                        <%
                            if (isCollection) {
                        %>
                        <img class="more" src="http://datatables.net/release-datatables/examples/examples_support/details_open.png">
                        <span><table width="100%">
                                <tr>
                    <th>
                        
                    </th>
                    <th>
                        
                    </th>
                    <th>
                        User Description
                    </th>
                    <th>
                        Started on (UTC)
                    </th>
                    <th>
                        Status
                    </th>

                </tr>
                                <%
                                    for (String[] sj : ai.getSubJobs()) {
                                %>
                                <tr>
                                    <td></td>
                                    <td></td>
                                    <td><%=sj[3]%></td>
                                    <td><%=sj[4]%></td>
                                    <td><%=sj[5]%></td>
                                </tr>
                                <%
                                    }
                                %>
                            </table></span>


                        <% } else {%>
                        
                        <%}%>
                    </td>
                    <td align="center"><%= ai.getInteractionInfos()[2]%></td>
                    <td align="center"><%= ai.getInteractionInfos()[3]%></td>
                    <td align="center"><%= ai.getInteractionInfos()[4]%></td>
                    <td align="center"><%= status%></td>
                </tr>


                <%
                        } //ELSE OTHER STATUS  close
                    } // for close
%>
            </tbody>
        </table>

        <%
        } else {
        %>
        <div>
            <span style="font-size:12px;">
                <b>There are no submitted jobs. </b></span></div>
                <%            } // ELSE TABELLA JOB NULL CLOSE

                %>
            </c:when>


    <c:when test="${tabs1 == 'Done Jobs List'}" >
        <script type="text/javascript">
            $.fn.dataTableExt.oJUIClasses.sSortAsc  = "chain-table-sort-header ui-state-active";
            $.fn.dataTableExt.oJUIClasses.sSortDesc  = "chain-table-sort-header ui-state-active";
            $(document).ready(function() {
                new FixedHeader(
                $('#done').dataTable( {
                    "aaSorting": [[ 2, "desc" ]],
                    "sDom": '<"H"<"clear"T>f<"clear"l><"right"p>t<"F"ip>',
                    "aaSorting": [[ 2, "desc" ]],
                    "bRetrieve": true,
                    "bJQueryUI": true,
                    "oTableTools": {
                        "sSwfPath": "<%=renderRequest.getContextPath()%>/datatables/extras/TableTools/media/swf/copy_cvs_xls_pdf.swf",
                        "aButtons": [
                            "copy",
                            "print",
                            {
                                "sExtends":    "collection",
                                "sButtonText": "Save",
                                "aButtons":    [ "csv", "pdf" ]
                            }
                        ]
                    },
                    "sPaginationType": "full_numbers"
                }) );
            });

        </script>

        <div>
            <span style="font-size:12px;">The table below shows your done jobs list.<br>
            </span><br><br><br>

        </div>
        <table id="done" style="width:100%">
            <thead>
                <tr>
                    <th>
                        Application Name
                    </th>
                    <th>
                        User Description
                    </th>
                    <th>
                        Started on (UTC)
                    </th>

                </tr>
            </thead>
            <tbody>

                <%
                

                    for (ActiveInteractions aidone : aiListDone) {
                        if (!aidone.getInteractionInfos()[1].equals("FutureGateway") && !aidone.getInteractionInfos()[1].equals(company.getName())) {
                            continue;
                        }

                %>
                <tr>
                    <td align="center"><%= aidone.getInteractionInfos()[2]%></td>
                    <td align="center"><%= aidone.getInteractionInfos()[3]%></td>
                    <td align="center"><%= aidone.getInteractionInfos()[4]%></td>

                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </c:when>
    <c:otherwise>
        No tabs
    </c:otherwise>

</c:choose>
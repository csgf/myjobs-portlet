<%-- 
    Document   : editMode
    Created on : 15-set-2011, 12.51.30
    Author     : pistagna
--%>
<%@page import="javax.portlet.*"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@taglib prefix="aui" uri="http://liferay.com/tld/aui"%>
<%@taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>
<portlet:defineObjects />
<script type="text/css">

    label
    {
        width: 4em;
        float: left;
        text-align: right;
        margin-right: 0.5em;
        display: block
    }

    .submit input
    {
        margin-left: 4.5em;
    }
    input
    {
        color: #781351;
        background: #fee3ad;
        border: 1px solid #781351
    }

    .submit input
    {
        color: #000;
        background: #ffa20f;
        border: 2px outset #d7b9c9
    }
    fieldset
    {
        border: 1px solid #781351;
        width: 20em
    }

    legend
    {
        color: #fff;
        background: #ffa20c;
        border: 1px solid #781351;
        padding: 2px 6px
    }

</script>
<portlet:defineObjects />

<%
    boolean fgEnabled = (Boolean) request.getAttribute("fgEnabled");
    
    String showFGPrefPanel = (fgEnabled) ? "block" : "none";
    System.out.println(showFGPrefPanel);
    String fgHost = (String) request.getAttribute("fgHost");
    int fgPort = (Integer) request.getAttribute("fgPort");
    String fgAPIVer = (String) request.getAttribute("fgAPIVer");
%>

<jsp:useBean id="EParaM4" class="java.lang.String" scope="request"/>

<form action="<portlet:actionURL>
      <portlet:param name="newActionStatus" value="CONFIGPORTLET"/>
</portlet:actionURL>" method="POST">
<fieldset>
    <legend>Parameters</legend>

    <p><label for="check"><b>Check Status:</b></label> <%=EParaM4%></p>

</fieldset>

<fieldset>
    <legend>Insert New Parameters</legend>

    <p><label for="check">Check status:</label> <input type="text" name="EparaM4"><br /></p>

    <aui:input type="checkbox" checked="true"
               name="fgEnabled" id="chkFg" label="Enable Futuregateway:" onChange="showFGPreferencePanel()"
               value="<%= fgEnabled%>" />
    <div id="fgPreferencePanel" style="display: <%= showFGPrefPanel %>">
        <liferay-ui:error key="no-valid-host" message="Please, specify a valid hostname or IP address" />
        <liferay-ui:error key="no-host" message="Please, specify a hostname or IP address" />
        <aui:input type="text" name="fgHost" label="Futuregateway host: " size="60" id="jobLabel" value="<%= fgHost%>" />
        <liferay-ui:error key="no-valid-port" message="Please, specify port number betwee [1, 65535]" />
        <liferay-ui:error key="no-port" message="Please, specify port number" />
        <liferay-ui:error key="digits-port" message="Only digits are accepted for port number" />
        <aui:input label="Futuregateway port: " name="fgPort" type="text" value="<%= (fgPort != -1) ? String.valueOf(fgPort) : ""%>"/>
        <liferay-ui:error key="no-api-vers" message="Futuregateway API version is mandatory" />
        <aui:input label="API version: " name="fgAPIVer" type="text" value="<%= fgAPIVer%>"/>
    </div>
    <p class="submit"><input type="submit" name="Submit" value="Save Parameters"></p>

</fieldset>
</form>

<script type="text/javascript">
    var chkFgId = "<portlet:namespace/>chkFg";

    AUI().ready('node', function (A) {
        if (chkFgId)
            showFGPreferencePanel();
    });

    function showFGPreferencePanel() {
        if (document.getElementById(chkFgId).value === "false") {
            document.getElementById('fgPreferencePanel').style.display = 'none';
        } else {
            document.getElementById('fgPreferencePanel').style.display = 'block';
        }
    }
</script>
<%-- 
    Document   : editMode
    Created on : 15-set-2011, 12.51.30
    Author     : pistagna
--%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
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

<p class="submit"><input type="submit" name="Submit" value="Save Parameters"></p>

</fieldset>
</form>

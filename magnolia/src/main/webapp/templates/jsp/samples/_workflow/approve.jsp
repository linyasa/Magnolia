<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
   <%@ page import = "info.magnolia.module.owfe.OWFEEngine,java.util.Iterator,openwfe.org.engine.workitem.WorkItem,openwfe.org.engine.workitem.InFlowWorkItem, openwfe.org.engine.expressions.FlowExpressionId,openwfe.org.engine.workitem.StringMapAttribute,com.ns.log.Log" %>
   <jsp:useBean id ="owfeBean" scope="application" class="info.magnolia.module.owfe.OWFEBean" />  
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Approve</title>
</head>
<body>

<%
String eid = request.getParameter("eid");
if (eid == null)
{
	out.println("error: the eid = null");
}
else 
	out.println("expression id = " + eid);
boolean result = true;
try{
	owfeBean.approveActivation(eid, request);
}
catch (Exception e)
{
	out.println("<pre>");
	out.println(Log.printExpStack(e));
	e.printStackTrace(); 
	result = false;
	out.println("</pre>");
}
%>
<br>
<%
if (result)
	out.println("<h3>activation succeded.</h3>");
else
	out.println("<h3>activation failed.</h3>");
%>
<br>
<a href="inbox.jsp"> back to inbox </a>
</body>
</html>

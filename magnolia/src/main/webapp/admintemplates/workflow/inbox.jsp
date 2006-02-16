<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import = "info.magnolia.module.owfe.OWFEEngine,java.util.Iterator,openwfe.org.engine.workitem.WorkItem,openwfe.org.engine.workitem.InFlowWorkItem, openwfe.org.engine.expressions.FlowExpressionId,openwfe.org.engine.workitem.StringMapAttribute" %>
<jsp:useBean id ="owfeBean" scope="application" class="info.magnolia.module.owfe.OWFEBean" />  
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
 <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/admindocroot/css/workflow.css" />
    
<title>Inbox</title>
</head>
<body>

<%
int size = owfeBean.getWorkItemsNumber(request);
%>
<table class="nicetable">

<tr>
<th class="nicetableheader">index</th>
<th class="nicetableheader">FlowExpressionId</th>
<th class="nicetableheader">Dispatch time</th>
<th class="nicetableheader">Attributes</th>
<th class="nicetableheader">action</th>
</tr>
<%
for (int i = 0; i < size; i++){ 
InFlowWorkItem wi = (InFlowWorkItem)owfeBean.getWorkItem(request, i);
if (wi != null){
String id = wi.getLastExpressionId().toParseableString();
%>
<tr class="nicetablerow"><td>
<%=i %></td>

<td width="30">
<%=id %>
<td width="30">
<%
out.println(wi.getDispatchTime());
%>
</td>
<td>
<form action="/.magnolia/save.html" method="get">
<input type="hidden" name="eid" value="<%=id%>">
<%

// list attribute
StringMapAttribute map = wi.getAttributes();
Iterator it = map.alphaStringIterator();

while (it.hasNext())
{
	String name = (String)it.next();
	if (name.equals("__definition__"))
		continue;
	String value = (String)wi.getAttribute(name).toString();	
	%>
	 
	<%=name%><input type="hidden" name="attributeName" value="<%=name%>"/>
	
	:<input name="value" value="<%=value%>"/>
	<!-- 
<textarea rows=1 name="<%=value%>" ><%=value%></textarea> -->
	<br>

<%} %>
<input type="submit" value="save"/>
</form>
</td>
 
<td><a href="/.magnolia/approve.html?eid=<%=id %>">approve</a>  <a href="/.magnolia/reject.html?eid=<%=id %>">reject</a></tr>
<%}} %>
</table>




</body>
</html>

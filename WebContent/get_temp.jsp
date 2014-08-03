<%@page import="org.snmp4j.mp.SnmpConstants"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ page import="com.homeautomation.manager.*" %>
    <%@ page import="javax.servlet.http.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%!
	SnmpGet snmpGet = new SnmpGet();
	String request_action = "";	
	%>
<%
	request_action = request.getParameter("action");
	snmpGet.snmpGet("127.0.0.1", "public", "1.3.6.1.2.1.33.1.1.1", SnmpConstants.version2c);
	if("thermostat_get".equals(request_action)){
		out.write("Success");
	}
	else  
		out.write("Failure");
%>
</body>
</html>
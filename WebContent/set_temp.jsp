
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
	Server server = new Server();
	Thermostat thermostat = new Thermostat();
	String temperature = "";	
	%>
<%
	new Thread(server).start();
	temperature = request.getParameter("temp");
	Thermostat.setTemperature(temperature);
	String s = Thermostat.getTemperature();
	if(s.equals(temperature))
		out.write("Success");
	else  
		out.write("Failure");
		%>
</body>
</html>
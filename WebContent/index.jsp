<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<p>
<center>
   Today's date: <%= (new java.util.Date()).toLocaleString()%>
</center>
</p>
<form action="set_temp.jsp">
<font size="10" style="bold">Thermostat Functions:</font><br><br>
  Set the Temperature: <input type="text" name="temp">
  <input type="submit" value="Submit">
</form>
<form action="get_temp.jsp/action=thermostat_get">
<input type="submit" value="Get Temperature">
</form>
</body>
</html>
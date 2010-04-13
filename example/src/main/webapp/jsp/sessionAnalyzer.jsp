<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<%@page import="java.util.List"%>
<%@page import="java.util.LinkedList"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.GregorianCalendar"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="java.util.Date"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>session analyzer</title>
</head>
<body>


	Integer i = new Integer(0);<br>
	Date date = new Date();<br>
	Calendar calendar = new GregorianCalendar();<br>
	List list = new LinkedList();<br>
	list.add(new Integer(0));<br>
	list.add(new Date());<br>
	list.add(calendar);<br>
	<br>
	session.putValue("Integer", i);<br>
	session.putValue("Date", date);<br>
	session.putValue("Calendar", calendar);<br>
	session.putValue("List", list);<br>
	<br>
	log saved to /tmp/capsule-example.log
<%
	Integer i = new Integer(0);
	Date date = new Date();
	Calendar calendar = new GregorianCalendar();	
	List list = new LinkedList();
	list.add(new Integer(0));
	list.add(new Date());
	list.add(calendar);

	session.putValue("Integer", i);
	session.putValue("Date", date);
	session.putValue("Calendar", calendar);
	session.putValue("List", list);
%>
</body>
</html>
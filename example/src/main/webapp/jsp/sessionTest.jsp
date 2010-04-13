<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Session Test JSP</title>
</head>
<body>
<h1>Session Test JSP</h1>

<ul>
<c:forEach var="attrib" items="${requestScope.existingSessionData}">
<li>${attrib.name}=${attrib.value}</li>
</c:forEach>
</div>
</ul>

</body>
</html>
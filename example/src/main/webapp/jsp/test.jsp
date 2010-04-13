<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Simple Test JSP</title>
<!-- Sam Skin CSS for TabView -->
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/container/assets/container.css">
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/tabview/assets/skins/sam/tabview.css"></link>
<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css" type="text/css" media="screen" title="main stylesheet" charset="utf-8"></link>

<!--Include YUI Loader: -->
<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/yuiloader/yuiloader-min.js"></script>

<!--Use YUI Loader to bring in your other dependencies: -->
<script type="text/javascript">
// Instantiate and configure YUI Loader:
(function() {
	var loader = new YAHOO.util.YUILoader({
		base: "",
		require: ["cookie","logger","tabview","yahoo-dom-event"],
		loadOptional: false,
		combine: true,
		filter: "DEBUG",
		allowRollup: true,
		onSuccess: function() {
			YAHOO.util.Event.onDOMReady(function() {
				YAHOO.widget.Logger.enableBrowserConsole();
				YAHOO.util.Event.addListener("CookieCorrupterForm", "submit", function(event) {
					YAHOO.util.Event.stopEvent(event);
				});
				YAHOO.util.Event.addListener("AddCookie", "click", function(event) {
					var name = YAHOO.util.Dom.get("CookieName").value;
					var val = YAHOO.util.Dom.get("CookieValue").value;
					YAHOO.log("AddCookie: [name=" + name + ", val=" + val + "]");
					YAHOO.util.Cookie.set(name, val);
				});
				YAHOO.util.Event.addListener("DeleteCookie", "click", function(event) {
					var name = YAHOO.util.Dom.get("CookieName").value;
					YAHOO.log("DeleteCookie: [name=" + name + "]");
					var cookie = YAHOO.util.Cookie.get(name);
					if (cookie) {
						YAHOO.log(name + " cookie found: " + YAHOO.lang.dump(cookie));
					}
					else {
						YAHOO.log(name + " cookie not found");
					}
					YAHOO.util.Cookie.remove(name);
				});
				YAHOO.util.Event.addListener("RefreshButton", "click", function(event) {
					window.location.href = "${pageContext.request.contextPath}/foo";
				});
				var myTabs = new YAHOO.widget.TabView("demo");
			});
		}
	});

	// Load the files using the insert() method.
	loader.insert();
})();
</script>
<script src="${pageContext.request.contextPath}/scripts/cookiefilter.js" type="text/javascript" charset="utf-8"></script>
</head>
<body class="yui-skin-sam">
<h1>Capsule Test JSP</h1>

<p><button id="RefreshButton">Refresh</button></p>

<div id="demo" class="yui-navset">
    <ul class="yui-nav">
        <li class="selected"><a href="#tab1"><em>Cookies</em></a></li>
        <li><a href="#tab2"><em>Session (in)</em></a></li>
        <li><a href="#tab3"><em>Session (out)</em></a></li>
        <li><a href="#tab4"><em>Session Form</em></a></li>
        <li><a href="#tab5"><em>Cookie Form</em></a></li>
    </ul>            
    <div class="yui-content">
        <div>
			<p>The cookies found in the request for this page.</p>
			<div id="cookies">
			<table>
				<tbody>
					<c:forEach var="attrib" items="${requestScope.CookieList}">
						<tr><td>${attrib.name}</td><td>${attrib.value}</td></tr>
					</c:forEach>
				</tbody>
			</table>
			</div>
		</div>
        <div>
			<h2>Session Data In</h2>
			<p>This is the session data after the capsule session cookie data has been processed.</p>
			<div id="sessionDataIn">
				<table>
					<tbody>
						<c:forEach var="attrib" items="${requestScope.ExistingSessionData}">
							<tr><td>${attrib.name}</td><td>${attrib.value}</td></tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
        <div>
			<h2>Session Data Out</h2>
			<p>This section shows the Session data after application and Capsule processing.</p>
			<div id="sessionDataOut">
				<table>
					<tbody>
						<c:forEach var="attrib" items="${requestScope.NewSessionData}">
							<tr><td>${attrib.name}</td><td>${attrib.value}</td></tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
        <div>
			<h2>Set Session Data</h2>
			<form id="SessionDataForm" method="POST" action="${pageContext.request.contextPath}/foo">
			<dl class="form">
				<dt>Session Attribute Name</dt>
				<dd><input type="text" name="SessionAttributeName" size="20" /></dd>

				<dt>Session Attribute Value</dt>
				<dd><input type="text" name="SessionAttributeValue" size="20" /></dd>

				<dt>Session Size</dt>
				<dd><input type="text" name="SessionSize" size="20" /></dd>

				<dt>Invalidate Session?</dt>
				<dd><input type="checkbox" name="InvalidateSession" value="true" /></dd>

			</dl>
			<div style="margin-top:0.5em"><input type="submit" id="sessionSubmit" value="Submit" /></div>
			</form>
		</div>
        <div>
			<h2>Add/Remove Cookies</h2>
			<form id="CookieCorrupterForm" method="GET" action="">
			<dl class="form">
				<dt>Name</dt>
				<dd><input type="text" id="CookieName" size="20" /></dd>
				
				<dt>Value</dt>
				<dd><input type="text" id="CookieValue" size="20" /></dd>
			</dl>
			<div style="margin-top:0.5em"><input type="button" id="AddCookie" value="Add Cookie" /> <input type="button" id="DeleteCookie" value="Delete Cookie" /></div>
			</form>
		</div>
    </div>
</div>
</body>
</html>
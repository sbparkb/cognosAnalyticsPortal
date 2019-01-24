<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType"%>
<%@ page import="kr.co.growtogether.cognos.CognosConnect"%>
<%@ page import="kr.co.growtogether.cognos.CognosObject"%>
<%@ page import="kr.co.growtogether.cognos.CognosControl"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Running Report...</title>
</head>
<body>
<%
	String gateway = "http://localhost:9300/bi/v1/disp";

	String storeId = request.getParameter("sId");

	CognosConnect conn = new CognosConnect();

	ContentManagerService_PortType cmService = (ContentManagerService_PortType)session.getAttribute("cmService");

	CognosControl ctrl = new CognosControl();

	CognosObject object = ctrl.getCognosObjectInfo(gateway, storeId, cmService, "true"); 
	String reportUrl = object.getExecuteUrl();
%>
<h3>Loading...</h3>
<script>
location.href = '<%=reportUrl%>';
</script>
</body>
</html>
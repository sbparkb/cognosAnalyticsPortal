<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType"%>
<%@ page import="kr.co.growtogether.cognos.CognosConnect"%>
<%@ page import="kr.co.growtogether.cognos.CognosObject"%>
<%@ page import="kr.co.growtogether.cognos.CognosControl"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Welcome to Report!</title>
</head>
<%
	String gateway = "http://localhost:9300/bi/v1/disp";

	String storeId = request.getParameter("sId");
	CognosConnect conn = new CognosConnect();

	ContentManagerService_PortType cmService = (ContentManagerService_PortType)session.getAttribute("cmService");

	CognosControl ctrl = new CognosControl();
	
	CognosObject object = ctrl.getCognosObjectInfo(gateway, storeId, cmService, "false");
	
	String reportUrl = object.getExecuteUrl();
System.out.println("reportUrl:" + reportUrl);	
%>
<body>
<h3>광고비 집행 내역</h3>
<form id="prompt" action="<%=reportUrl%>" method="post">
	<select name="p_LINE">
		<option>==선택==</option>
		<option value="991">캠핑 장비</option>
		<option value="992">등산 장비</option>
		<option value="993">개인 용품</option>
		<option value="994">야외 보호 장비</option>
		<option value="995">골프 장비</option>
	</select>
	<input type="submit" value="실행">
</form>
</body>
</html>
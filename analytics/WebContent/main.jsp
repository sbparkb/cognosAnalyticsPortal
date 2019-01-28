<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType"%>
<%@ page import="kr.co.growtogether.cognos.CognosConnect"%>
<%@ page import="kr.co.growtogether.cognos.CognosObject"%>
<%@ page import="kr.co.growtogether.cognos.CognosControl"%>
<%@ page import="java.util.ArrayList"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>Welcome Main!</title>
<style>
body{
	font-size: 0.9em;
}
li {
	cursor: pointer;
}
</style>
<script src="js/jquery-3.3.1.min.js"></script>
<script>
$(document).ready(function() {
	$('#leftMenu').on('click', 'li', function(event) {
		var id = $(this).attr('id');
		var type = $(this).attr('data-type');
		event.stopPropagation(); //상위 태그 이벤트로 전파 방지
		
		if(type == 'folder'){
			if($(this).hasClass('open')){
				$(this).children().remove();
				$(this).removeClass('open');
			}else{
				$.ajax({
					method : "POST",
					url : "MenuJSON",
					context: $(this),
					data : {storeId : id}
				}).done(function(data) {				
					var obj = JSON.parse(data);
					var size = obj.length;
					var ul = '<ul>';
					for(var i = 0; i < size; i++){
						if(obj[i].objectType =='folder'){
							ul += "<li id='"+obj[i].storeId+"' data-type='"+obj[i].objectType+"'>["+obj[i].defaultName+"]</li>";	
						}else{
							ul += "<li id='"+obj[i].storeId+"' data-type='"+obj[i].objectType+"'>"+obj[i].defaultName+"</li>";
						}					
					}				
					ul += "</ul>";
					$(this).append(ul);
					$(this).addClass('open');
				});
			}
		}else{
			$('#reportRun').attr('src','reportRun.jsp?sId='+id);
		}
	}); //leftMenu click
}); //ready

function promptRun(){
	var sId = 'iDAAE6FAF2FE24C86A9CF596E44BEC6D1';
	$('#reportRun').attr('src','prompt/P001.jsp?sId='+sId);
}

function logout(){
	$('#logout').attr('src','http://june-pc/bi/v1/disp/rds/auth/logoff');
	location.href = 'index.jsp';
}

function openRs(){
	$('#reportRun').attr('src','http://june-pc/bi/?perspective=authoring&id=1548405135009&isTemplate=false&UIProfile=Titan');
}
</script>
</head>
<%	
	String CMURL = "http://june-pc:9300/p2pd/servlet/dispatch";
	String camPassport = request.getParameter("cam_passport");
	
	CognosConnect conn = new CognosConnect();
	ContentManagerService_PortType cmService = conn.connectToCognosServer(CMURL, camPassport);
	
	session.setAttribute("cmService", cmService);
	
	CognosControl ctrl = new CognosControl();
	
	CognosObject obj = ctrl.getUserInfo(cmService);	
	
	String root = "/content";
	
	ArrayList<CognosObject> menuList = ctrl.getContentList(root, cmService);
	
	pageContext.setAttribute("menuList", menuList);
%>
<body>
<h3><%=obj.getDefaultName()%></h3><input type="button" onclick="logout()" value="로그아웃">&nbsp;&nbsp;<input type="button" onclick="openRs()" value="빈 보고서">
<hr>
<div style="width: 20%; height: 800px;float: left;">
<ul id="leftMenu">
	<c:forEach var="menu" items="${menuList}">
	<c:choose>
	<c:when test="${menu.objectType == 'folder'}">
		<li data-type="${menu.objectType}" id="${menu.storeId}">[${menu.defaultName}]</li>
	</c:when>
	<c:otherwise>
		<li data-type="${menu.objectType}" id="${menu.storeId}">${menu.defaultName}</li>
	</c:otherwise>
	</c:choose>
	</c:forEach>
</ul>
<ul>
	<li onclick="promptRun();">매개변수전달 보고서</li>
</ul>
</div>
<div style="width: 80%; height: 800px;float: left">	
	<iframe id="reportRun" name="reportRun" src="" style="width: 100%; height: 100%;"></iframe>
</div>
<iframe id="logout" src="" style="display: none;"></iframe>
</body>
</html>
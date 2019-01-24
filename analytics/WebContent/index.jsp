<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Welcome Login</title>
<script src="js/jquery-3.3.1.min.js"></script>
<script>
function setFocus(){
	document.getElementById("CAMUsername").focus();	
}

function logon(){
	/**
	* before submit
	* 아이디/비밀번호 검증 후 맞는 사용자만 Cognos Anayltics에 submit 한다.
	**/	
	$('#login').submit();
}
</script>
</head>
<body onload="setFocus()">
<div>
<form id="login" action="http://localhost:9300/bi/v1/disp?b_action=xts.run&m=portal/bridge.xts&c_env=/portal/custom/redirect.xml&c_mode=post" method="post">
<input type="hidden" name="h_CAM_action" value="logonAs">
<input type="hidden" name="CAMNamespace" value="DBAuth">
아이디<input type="text" id="CAMUsername" name="CAMUsername"><br>
비밀번호<input type="password" id="CAMPassword" name="CAMPassword"><br>
<input type="button" onclick="logon()" value="로그인">
</form>
</div>
<iframe src="http://localhost:9300/bi/v1/disp?b_action=xts.run&m=portal/logoff.xts" style="display: none;"></iframe>
</body>
</html>
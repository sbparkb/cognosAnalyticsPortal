<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="EUC-KR">
<title>store id 입력</title>
<style type="text/css">
input{
width: 300px;
}
</style>
</head>
<body>
<form action="getUrl.jsp" target="url" method="post">
<table>
	<tr>
		<td>dispatch</td>
		<td><input type="text" name="CMURL" value="http://june-pc:9300/p2pd/servlet/dispatch"></td>
	</tr>
	<tr>
		<td>gateway</td>
		<td><input type="text" name="gateway" value="http://june-PC:9300/bi/v1/disp"></td>
	</tr>
	<tr>
		<td>보고서ID</td>
		<td><input type="text" name="storeId" value="i51934401914C458EB3AF477B745962C9"></td>
	</tr>
	<tr>
		<td>아이디</td>
		<td><input type="text" name="uid" value="admin"></td>
	</tr>			
	<tr>
		<td>비밀번호</td>
		<td><input type="password" name="pwd" value="1234"></td>
	</tr>	
</table>
<input type="hidden" name="namespace" value="DBAuth">
<input type="submit" value="제출"><br>
</form>
<iframe name="url" style="width: 100%; height: 700px;">
</iframe>
</body>
</html>
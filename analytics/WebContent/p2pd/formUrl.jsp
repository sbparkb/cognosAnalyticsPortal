<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="EUC-KR">
<title>store id �Է�</title>
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
		<td>����ID</td>
		<td><input type="text" name="storeId" value="i51934401914C458EB3AF477B745962C9"></td>
	</tr>
	<tr>
		<td>���̵�</td>
		<td><input type="text" name="uid" value="admin"></td>
	</tr>			
	<tr>
		<td>��й�ȣ</td>
		<td><input type="password" name="pwd" value="1234"></td>
	</tr>	
</table>
<input type="hidden" name="namespace" value="DBAuth">
<input type="submit" value="����"><br>
</form>
<iframe name="url" style="width: 100%; height: 700px;">
</iframe>
</body>
</html>
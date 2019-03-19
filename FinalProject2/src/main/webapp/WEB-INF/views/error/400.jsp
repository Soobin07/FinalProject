<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset=UTF-8">
<title>Insert title here</title>

<link href="https://fonts.googleapis.com/css?family=Roboto+Mono:400,500" rel="stylesheet">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/resources/css/error/error.css">
   
</head>
<body>
	<div class="error_page_background">
			<div class="error_page_content_wrap">
				<h1>400</h1>
				<h3 style="margin-top:-70px;font-weight:500">Bad Request Error</h3>
				<h3>요청 실패입니다</h3>
				<a href="${pageContext.request.contextPath }/mainPage"><div class="main_logo"></div></a>
			</div>
	</div>
</body>
</html>
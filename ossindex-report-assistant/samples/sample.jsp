<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
%>
<!-- Layout boilerplate from http://www.getskeleton.com/ -->
<head>

	<!-- Basic Page Needs
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<!-- <meta charset="utf-8"> -->
	<!-- <meta charset="ISO-8859-1"> -->
	<!-- <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /> -->
	<title>OSS index</title>
	<meta name="description" content="">
	<meta name="author" content="">
	
	<!-- Mobile Specific Metas
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
	
	<!-- FONT
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<link href='https://fonts.googleapis.com/css?family=Raleway:400,300,600' rel='stylesheet' type='text/css'>
	
	<!-- CSS
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<link rel="stylesheet" href="<%= rootPath %>/css/skeleton/normalize.css">
	<link rel="stylesheet" href="<%= rootPath %>/css/skeleton/skeleton.css">

	<!-- Favicons
	================================================== -->
	<link rel="shortcut icon" href="<%= rootPath %>/favicon.ico" type="image/x-icon">
	
	<!-- VOR Layouts -->
	<link type="text/css" rel="Stylesheet" href="<%= rootPath %>/css/default.css" />
	<link type="text/css" rel="Stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css" />

	<!-- JAVASCRIPT DEPENDENCIES -->
	<script src="https://code.jquery.com/jquery-2.1.3.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.8.1/underscore-min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/backbone.js/1.1.2/backbone.js"></script>
	<script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
	
	<!-- Timeline -->
	<script type="text/javascript" src="/js/timeline/timeline.js"></script>
	<link rel="stylesheet" type="text/css" href="/css/timeline/timeline.css">
	
	
	<!-- Override template settings for Underscore -->
	<script>
	_.templateSettings = {
		    interpolate: /\{\{(.+?)\}\}/gim,
		    evaluate: /\{\{(.+?)\}\}/gim,
		    escape: /\{\{\-(.+?)\}\}/gim
		};
	</script>
</head>
<%
%>
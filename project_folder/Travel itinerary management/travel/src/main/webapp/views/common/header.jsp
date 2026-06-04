<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${empty pageTitle ? 'Trip Planner' : pageTitle.concat(' | Trip Planner')}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB"
          crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body class="d-flex flex-column min-vh-100">
<nav class="navbar navbar-expand-lg sticky-top">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">Trip<span>Log</span></a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavigation"
                aria-controls="mainNavigation" aria-expanded="false" aria-label="메뉴 열기">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNavigation">
            <ul class="navbar-nav ms-auto align-items-lg-center gap-lg-3">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/views/trip/list.jsp">여행 일정</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/views/user/login.jsp">로그인</a>
                </li>
                <li class="nav-item">
                    <a class="btn nav-action" href="${pageContext.request.contextPath}/views/user/register.jsp">회원가입</a>
                </li>
            </ul>
        </div>
    </div>
</nav>
<main class="flex-grow-1">

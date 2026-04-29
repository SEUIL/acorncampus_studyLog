<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 공통 푸터. header.jsp 의 <main> 을 닫고 페이지 마무리 --%>

</main>

<footer class="site-footer">
    <p>&copy; 2025 StudyLog. All rights reserved.</p>
</footer>

<%-- JS는 </body> 직전에 로드: HTML 렌더링 완료 후 실행 보장 --%>
<script src="${pageContext.request.contextPath}/resources/js/main.js"></script>

</body>
</html>

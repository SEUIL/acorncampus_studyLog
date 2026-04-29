package com.acorncampus_studylog.filter;

import com.acorncampus_studylog.dto.UserDto;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 로그인/권한 검사 필터. web.xml에서 /l_check/* 매핑.
 * 순서: 미로그인 → /user/login.do | 정지 계정 → 세션 삭제 후 login.do?banned=true | 비관리자 /admin 접근 → /
 */
public class LoginCheckFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req     = (HttpServletRequest)  request;
        HttpServletResponse resp    = (HttpServletResponse) response;
        HttpSession         session = req.getSession(false); // false: 세션 없으면 null 반환 (새로 만들지 않음)

        // 세션에서 로그인 사용자 정보 조회
        UserDto loginUser = (session != null)
                ? (UserDto) session.getAttribute("loginUser")
                : null;

        // 미로그인
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/user/login.do");
            return;
        }

        // 정지 계정
        if ("Y".equals(loginUser.getIsBanned())) {
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/user/login.do?banned=true");
            return;
        }

        // 관리자 페이지 권한 없음
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (requestURI.startsWith(contextPath + "/admin/") && !"ADMIN".equals(loginUser.getRole())) {
            resp.sendRedirect(contextPath + "/");
            return;
        }

        chain.doFilter(request, response);
    }
}

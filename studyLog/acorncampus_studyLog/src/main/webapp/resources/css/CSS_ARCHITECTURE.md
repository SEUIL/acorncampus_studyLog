# CSS 아키텍처 문서

> 작성일: 2026-05-07  
> 작업자: 철진

---

## 1. 리팩토링 목표

기존에는 페이지별 CSS 파일마다 `.sidebar`, `.dashboard-wrapper`, `.btn`, `.breadcrumb` 등  
**동일한 스타일이 수십 개 파일에 중복**되어 있었습니다.  
수정 시 모든 파일을 일일이 찾아가야 했고, 일관성 유지가 어려웠습니다.

**목표:** 전역 → 컴포넌트 → 페이지 순의 3계층 구조로 재편하여  
중복 제거, 의존관계 명확화, 유지보수성을 향상시킵니다.

---

## 2. 리팩토링 규칙 (14개)

### [전역 파일]
1. **`global_theme.css`에는 진짜 전역 속성만 넣습니다.**  
   허용 범위: 웹폰트 import, `* { box-sizing }` 리셋, `:root` CSS 변수, `.dark-theme` 변수 오버라이드, `body` 베이스 스타일.  
   레이아웃·컴포넌트 클래스는 이 파일에 넣지 않습니다.

2. **`body { opacity: 0 }`은 global_theme.css에 선언합니다.**  
   페이지 진입 fade-in은 `page-transition.js`가 담당합니다.  
   (사이드바가 있는 페이지: `sideBar.jsp`가 로드 / 인증 페이지: 직접 로드)

### [컴포넌트 파일]
3. **재사용 컴포넌트는 `resources/css/components/`에 별도 파일로 분리합니다.**  
   파일명 규칙: `{역할}.css` (예: `button.css`, `form.css`)

4. **컴포넌트 CSS는 `global_theme.css`의 CSS 변수에만 의존합니다.**  
   다른 컴포넌트 CSS를 import하거나 참조하지 않습니다.

5. **컴포넌트 파일 상단에 의존 관계와 사용 범위를 주석으로 명시합니다.**
   ```css
   /* 의존: global_theme.css
      사용: 버튼이 있는 모든 페이지 */
   ```

6. **한 컴포넌트 클래스는 딱 하나의 파일에서만 정의합니다.**  
   `.btn`이 `button.css`에 있으면 다른 어떤 파일에도 `.btn`을 정의하지 않습니다.

### [사이드바 특수 규칙]
7. **`.sidebar` 베이스 스타일은 `layout.css`에 정의합니다.**  
   admin 페이지처럼 `sidebar.css`를 로드하지 않는 페이지를 위한 폴백입니다.  
   (`overflow-y: auto` 기본값)

8. **`sidebar.css`는 `layout.css`의 `.sidebar`를 확장·오버라이드합니다.**  
   `overflow: visible`(툴팁·토글 버튼 외부 노출), 접기/펼치기 `transition`을 추가합니다.  
   `sideBar.jsp`가 직접 link하므로 일반 페이지에서만 적용됩니다.

### [페이지 파일]
9. **페이지 전용 스타일은 `resources/css/pages/{카테고리}/`에 위치합니다.**  
   카테고리: `admin`, `auth`, `community`, `common`, `post`, `series`, `user`, `workspace`

10. **페이지 CSS 파일 상단에 의존 컴포넌트 목록을 명시합니다.**
    ```css
    /* 의존: global_theme.css, components/layout.css, components/button.css */
    ```

11. **페이지 CSS는 해당 페이지에서만 필요한 스타일만 담습니다.**  
    컴포넌트에 이미 정의된 클래스는 절대 재정의하지 않습니다.  
    단, `max-width` 등 페이지별 레이아웃 조정 오버라이드는 허용합니다.

### [JSP 연결 규칙]
12. **각 JSP `<head>`에 필요한 컴포넌트 CSS를 빠짐없이 명시적으로 link합니다.**  
    - 로드 순서: `global_theme.css` → 컴포넌트들 → 페이지 CSS  
    - `sideBar.jsp`가 inject하는 `sidebar.css`는 body 안에서 늦게 로드되므로  
      layout.css 이후에 cascade되어 정상적으로 오버라이드됩니다.

13. **`sideBar.jsp`는 자체적으로 `button.css`, `jandi.css`, `sidebar.css`를 link합니다.**  
    이 파일들은 `sideBar.jsp`를 include하는 JSP에서 중복으로 link하지 않아도 됩니다.  
    단, 페이지 본문에서 `button.css` 클래스를 쓴다면 명시적으로 link하는 것을 권장합니다.

14. **`header.jsp`를 include하는 JSP는 반드시 `button.css`와 `ui.css`를 로드해야 합니다.**  
    header.jsp가 사용하는 클래스: `.breadcrumb`(ui.css), `.btn`, `.btn-outline`(button.css)

---

## 3. 파일 구조

```
resources/css/
│
├── CSS_ARCHITECTURE.md          ← 이 문서
│
├── global_theme.css             ← 전역 전용 (변수, 리셋, body)
├── style.css                    ← (레거시, 인증 페이지용 구 스타일)
│
├── components/                  ← 재사용 컴포넌트 (모든 페이지에서 필요에 따라 link)
│   ├── layout.css               ← 대시보드 레이아웃 (.dashboard-wrapper, .sidebar, .main-content, .top-bar, .page-header)
│   ├── button.css               ← 버튼 전체 (.btn, .btn-primary, .btn-outline, .btn-sm, .btn-danger, .icon-btn)
│   ├── form.css                 ← 입력 폼 (.admin-input, .admin-select, .controls-bar)
│   ├── typography.css           ← 타이포그래피 (h1, h2, h3, p.text-sub)
│   ├── ui.css                   ← 공통 UI (.breadcrumb, .tag, .empty-state)
│   ├── table.css                ← 테이블 (.admin-table-wrapper, .admin-table)
│   ├── tabs.css                 ← 탭 (.board-tabs, .board-tab)
│   ├── series.css               ← 시리즈 카드 (.series-grid, .series-card, .series-card-new, .series-meta)
│   └── jandi.css                ← 기여도 그래프 색상 (.level-1 ~ .level-4, 라이트/다크)
│
└── pages/                       ← 페이지 전용 스타일
    ├── common/
    │   └── sidebar.css          ← 사이드바 컴포넌트 스타일 + 접기/펼치기 (sideBar.jsp가 직접 link)
    ├── auth/
    │   ├── index.css            ← 인트로/랜딩 페이지
    │   ├── login.css            ← 로그인 페이지
    │   └── register.css        ← 회원가입 페이지
    ├── admin/
    │   ├── admin_main.css       ← 관리자 대시보드 (그래프, 요약 카드, 메뉴 그리드)
    │   ├── admin_post_list.css  ← 관리자 게시글 목록
    │   ├── admin_report_list.css← 관리자 신고 목록
    │   ├── admin_tag_list.css   ← 관리자 태그 목록
    │   └── admin_user_list.css  ← 관리자 회원 목록 (통계 배너, 그래프)
    ├── community/
    │   └── community_main.css   ← 커뮤니티 메인 (인기글 카드, 검색 패널, 피드)
    ├── post/
    │   ├── post_detail.css      ← 게시글 상세 (max-width 900px, 본문/댓글/좋아요/모달)
    │   ├── post_list.css        ← 게시글 목록 (.post-link, .empty-row)
    │   └── post_write.css       ← 게시글·시리즈 작성/수정 (max-width 1000px, 에디터, 토글)
    ├── series/
    │   ├── series_detail.css    ← 시리즈 상세 (max-width 1000px, 게시글 목록 스타일)
    │   └── series_list.css      ← 시리즈 목록 (.series-author, .series-stats)
    ├── user/
    │   └── profile_update.css   ← 프로필 수정 폼 (아바타, 탭, 입력 필드)
    └── workspace/
        └── workspace_main.css   ← 내 작업 공간 (.workspace-header)
```

---

## 4. 페이지별 CSS 로드 매핑

### 일반 페이지 (sideBar.jsp 포함 → sidebar.css 자동 로드)

| JSP 파일 | 로드하는 컴포넌트 CSS | 페이지 CSS |
|----------|----------------------|-----------|
| `views/main.jsp` (커뮤니티) | typography, layout, **button**, ui, series | community_main.css |
| `views/user/mypage.jsp` | typography, layout, **button**, **ui**, series | workspace_main.css |
| `views/post/list.jsp` | typography, layout, button, form, table, tabs, ui | post_list.css |
| `views/post/detail.jsp` | typography, layout, button, ui | post_detail.css |
| `views/post/write.jsp` | typography, layout, button | post_write.css |
| `views/series/list.jsp` | layout, button, form, series, ui | series_list.css |
| `views/series/detail.jsp` | layout, button, ui | series_detail.css |
| `views/series/write.jsp` | typography, layout, button | post_write.css |
| `views/search/result.jsp` | typography, layout, button, form, table, tabs, ui | post_list.css |
| `views/user/update.jsp` | typography, layout, button, ui | profile_update.css |

> **주의:** `mypage.jsp`와 `main.jsp`는 `header.jsp`를 include하므로  
> `button.css`와 `ui.css`가 반드시 필요합니다. (2026-05-07 누락 수정 완료)

### 관리자 페이지 (sideBar.jsp 미포함 → sidebar.css 없음, layout.css의 .sidebar 베이스 적용)

| JSP 파일 | 로드하는 컴포넌트 CSS | 페이지 CSS |
|----------|----------------------|-----------|
| `views/admin/main.jsp` | typography, layout, button | admin_main.css |
| `views/admin/post/list.jsp` | typography, layout, button, form, table, tabs | admin_post_list.css |
| `views/admin/user/list.jsp` | typography, layout, button, form, table | admin_user_list.css |
| `views/admin/report/list.jsp` | typography, layout, button, form, table | admin_report_list.css |
| `views/admin/tag/list.jsp` | typography, layout, button, form, table | admin_tag_list.css |

---

## 5. CSS 로드 순서 (cascade)

```
[<head> 안]
1. global_theme.css       ← CSS 변수 정의
2. 외부 CDN (Font Awesome)
3. components/typography.css
4. components/layout.css  ← .sidebar 베이스 포함
5. components/button.css
6. components/form.css
   ... (필요한 컴포넌트)
7. pages/{카테고리}/{페이지}.css  ← 페이지 오버라이드

[<body> 안 — sideBar.jsp inject]
8. components/button.css  ← sideBar.jsp가 link (헤드보다 늦지만 동일 선택자면 덮어씀)
9. components/jandi.css
10. pages/common/sidebar.css  ← .sidebar 오버라이드 (overflow: visible, transition)
```

> sidebar.css가 layout.css보다 늦게 로드되기 때문에 `.sidebar` 오버라이드가 정상 작동합니다.

---

## 6. 수정 이력

| 날짜 | 내용 |
|------|------|
| 2026-05-07 | 4단계 CSS 리팩토링 완료 (전역 정리 → 컴포넌트 분리 → 페이지 정리 → JSP 연결) |
| 2026-05-07 | `sidebar.css` `.sidebar` 베이스 스타일 누락 수정, `layout.css`에 폴백 추가 |
| 2026-05-07 | `mypage.jsp` button.css + ui.css 누락 추가, `main.jsp` button.css 누락 추가, `update.jsp` ui.css 누락 추가 |

(function () {
    'use strict';

    /* ══════════════════════════════════════════════════════
       interactions.js
       ① Scroll Reveal  — 카드·섹션 등장 애니메이션
       ② Reading Progress Bar — 게시글 읽기 진행바
    ══════════════════════════════════════════════════════ */

    /* ── CSS 주입 ────────────────────────────────────────── */
    var style = document.createElement('style');
    style.textContent = [
        /* Scroll Reveal 기본 상태 */
        '.reveal{opacity:0;transform:translateY(22px);transition:opacity 0.48s ease,transform 0.48s ease;}',
        '.reveal.visible{opacity:1;transform:translateY(0);}'
    ].join('');
    document.head.appendChild(style);

    /* ══════════════════════════════════════════════════════
       ① Scroll Reveal
    ══════════════════════════════════════════════════════ */

    /* 리빌 적용 대상 셀렉터 */
    var REVEAL_SELECTORS = [
        '.popular-card',
        '.series-card',
        '.community-feed',
        '.search-panel',
        '.post-container',
        '.comments-section',
        '.write-container',
        '.profile-update-wrapper',
        '.workspace-header',
        '.page-header',
        '.section-header'
    ].join(',');

    var targets = document.querySelectorAll(REVEAL_SELECTORS);

    /* reveal 클래스 부여 */
    targets.forEach(function (el) {
        el.classList.add('reveal');
    });

    /* 그리드 아이템은 순서대로 stagger */
    var staggerSelectors = [
        '.popular-grid .popular-card',
        '.series-grid .series-card'
    ];
    staggerSelectors.forEach(function (sel) {
        document.querySelectorAll(sel).forEach(function (el, i) {
            el.style.transitionDelay = (i * 0.07) + 's';
        });
    });

    /* IntersectionObserver로 뷰포트 진입 시 visible 토글 */
    var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.08, rootMargin: '0px 0px -30px 0px' });

    targets.forEach(function (el) { observer.observe(el); });


})();

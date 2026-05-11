(function () {
    'use strict';

    /* ══════════════════════════════════════════════════════
       interactions.js
       ① Scroll Reveal  — 카드·섹션 등장 애니메이션
       ② Like Reaction  — 좋아요/비추천 이모지 버스트
    ══════════════════════════════════════════════════════ */

    /* ── CSS 주입 ────────────────────────────────────────── */
    var style = document.createElement('style');
    style.textContent = [
        /* Scroll Reveal 기본 상태 */
        '.reveal{opacity:0;transform:translateY(22px);transition:opacity 0.48s ease,transform 0.48s ease;}',
        '.reveal.visible{opacity:1;transform:translateY(0);}',
        /* Like Reaction 파티클 */
        '@keyframes like-burst{0%{opacity:1;transform:translateY(0) scale(1);}100%{opacity:0;transform:translateY(-80px) scale(1.4);}}',
        '.like-particle{position:fixed;pointer-events:none;font-size:22px;animation:like-burst 0.7s ease-out forwards;z-index:99999;}'
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


    /* ══════════════════════════════════════════════════════
       ② Like Reaction — 좋아요/비추천 이모지 버스트
       외부에서 호출: window.spawnLikeReaction(btn, 'L'|'D')
    ══════════════════════════════════════════════════════ */
    window.spawnLikeReaction = function (btn, likeType) {
        var emojis = likeType === 'L'
            ? ['👍', '👍', '✨', '🎉', '💙']
            : ['👎', '👎', '💢', '😤', '🙅'];

        var rect = btn.getBoundingClientRect();
        var cx   = rect.left + rect.width  / 2;
        var cy   = rect.top  + rect.height / 2;

        for (var i = 0; i < 6; i++) {
            (function (idx) {
                setTimeout(function () {
                    var el = document.createElement('span');
                    el.className = 'like-particle';
                    el.textContent = emojis[idx % emojis.length];
                    /* 버튼 주변에 랜덤하게 퍼짐 */
                    var spreadX = (Math.random() - 0.5) * 80;
                    var spreadY = (Math.random() - 0.5) * 30;
                    el.style.left = (cx + spreadX) + 'px';
                    el.style.top  = (cy + spreadY) + 'px';
                    el.style.animationDelay = (Math.random() * 0.12) + 's';
                    document.body.appendChild(el);
                    el.addEventListener('animationend', function () {
                        if (el.parentNode) el.parentNode.removeChild(el);
                    });
                }, idx * 55);
            })(i);
        }
    };



})();

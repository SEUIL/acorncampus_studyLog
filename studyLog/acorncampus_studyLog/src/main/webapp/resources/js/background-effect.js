/**
 * background-effect.js — 페이지 배경 연출
 *
 * 라이트 모드: 낙하 잎 파티클 + 언덕 + 해
 * 다크  모드: 배경 별 + 별똥별 + 야간 언덕 + 달
 *
 * 적용 범위: sideBar.jsp 를 include 하는 모든 인증 페이지
 * 백엔드 의존 없음 — 순수 JS + CSS
 */
(function () {
    'use strict';

    /* ================================================================
       설정 상수
    ================================================================ */
    const LEAF_TYPES   = ['green', 'blossom', 'autumn'];
    const LEAF_EMOJI   = { green: '🍃', blossom: '🌸', autumn: '🍂' };
    const MAX_LEAVES   = 20;
    const SPAWN_MS     = 900;

    const LEAF_COLORS  = {
        green:   ['#66BB6A', '#81C784', '#4CAF50', '#A5D6A7', '#388E3C'],
        blossom: ['#F8BBD9', '#F48FB1', '#FCE4EC', '#F06292', '#FF80AB'],
        autumn:  ['#FF8F00', '#E65100', '#FF7043', '#BF360C', '#FFA000'],
    };

    /* ================================================================
       상태 변수
    ================================================================ */
    let canvas, ctx;
    let leaves        = [];
    let shootingStars = [];
    let stars         = [];
    let hillTick      = 0;
    let spawnTimer    = null;
    let starTimeout   = null;
    let switcherEl    = null;
    let sunEl         = null;
    let moonEl        = null;
    let isDark        = false;
    let currentLeaf   = localStorage.getItem('bgLeafType') || 'green';

    /* ================================================================
       초기화
    ================================================================ */
    function init() {
        isDark = document.body.classList.contains('dark-theme');

        /* 캔버스 */
        canvas = document.createElement('canvas');
        canvas.id = 'bg-effect-canvas';
        canvas.style.cssText = [
            'position:fixed', 'top:0', 'left:0',
            'width:100%', 'height:100%',
            'pointer-events:none',
            'z-index:0',
        ].join(';');
        document.body.insertBefore(canvas, document.body.firstChild);
        ctx = canvas.getContext('2d');
        resize();
        window.addEventListener('resize', resize);

        /* 천체 CSS 키프레임 주입 + 해/달 DOM 생성 */
        injectCelestialStyles();
        initCelestialBodies();

        /* 테마 변경 감지 */
        new MutationObserver(function () {
            const nowDark = document.body.classList.contains('dark-theme');
            if (nowDark !== isDark) {
                triggerSkyTransition();          /* 캔버스 페이드로 즉각 전환 가림 */
                triggerCelestialTransition(nowDark);
                isDark = nowDark;
                teardown();
                setup();
            }
        }).observe(document.body, { attributes: true, attributeFilter: ['class'] });

        setup();
        loop();
    }

    function resize() {
        canvas.width  = window.innerWidth;
        canvas.height = window.innerHeight;
    }

    /* ================================================================
       모드 전환
    ================================================================ */
    function setup() {
        if (isDark) {
            removeSwitcher();
            spawnStars();
            scheduleShootingStar();
        } else {
            createSwitcher();
            spawnInitialLeaves();
            spawnTimer = setInterval(function () {
                if (leaves.length < MAX_LEAVES) leaves.push(makeLeaf(false));
            }, SPAWN_MS);
        }
    }

    function teardown() {
        leaves        = [];
        shootingStars = [];
        stars         = [];
        if (spawnTimer)  { clearInterval(spawnTimer);  spawnTimer  = null; }
        if (starTimeout) { clearTimeout(starTimeout);  starTimeout = null; }
    }

    /* ================================================================
       애니메이션 루프
    ================================================================ */
    function loop() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        hillTick += 0.003;

        if (!isDark) {
            drawHills(false);
            leaves = leaves.filter(function (l) { return l.y < canvas.height + 80; });
            leaves.forEach(function (l) { moveLeaf(l); drawLeaf(l); });
        } else {
            stars.forEach(function (s) { movestar(s); drawStar(s); });
            shootingStars = shootingStars.filter(function (s) { return s.alpha > 0; });
            shootingStars.forEach(drawShootingStar);
            drawHills(true);
        }

        requestAnimationFrame(loop);
    }

    /* ================================================================
       공통 — 언덕 (라이트: 낮 풍경 / 다크: 밤 실루엣)
    ================================================================ */
    function drawHills(night) {
        var w = canvas.width;
        var h = canvas.height;
        var t = hillTick;

        var color1, color2;
        if (night) {
            color1 = 'rgba(18,  38,  68,  0.35)';
            color2 = 'rgba(10,  22,  45,  0.45)';
        } else {
            color1 = 'rgba(144, 195, 215, 0.25)';
            color2 = 'rgba(110, 185, 145, 0.55)';
        }

        /* 레이어 1 — 왼쪽 봉우리: 왼쪽 높고 오른쪽으로 내려감 */
        ctx.beginPath();
        ctx.moveTo(0, h);
        ctx.lineTo(0, h * 0.52 + Math.sin(t * 0.4) * 6);
        ctx.bezierCurveTo(
            w * 0.20, h * 0.38 + Math.sin(t * 0.35) * 7,
            w * 0.50, h * 0.55 + Math.sin(t * 0.3)  * 6,
            w,        h * 0.80 + Math.sin(t * 0.4)  * 4
        );
        ctx.lineTo(w, h);
        ctx.closePath();
        ctx.fillStyle = color1;
        ctx.fill();

        /* 레이어 2 — 오른쪽 봉우리: 오른쪽 높고 왼쪽으로 내려감 */
        ctx.beginPath();
        ctx.moveTo(0, h);
        ctx.lineTo(0, h * 0.82 + Math.sin(t * 0.5 + 1) * 4);
        ctx.bezierCurveTo(
            w * 0.38, h * 0.72 + Math.sin(t * 0.4 + 1) * 5,
            w * 0.65, h * 0.42 + Math.sin(t * 0.35 + 1) * 7,
            w,        h * 0.35 + Math.sin(t * 0.45 + 1) * 5
        );
        ctx.lineTo(w, h);
        ctx.closePath();
        ctx.fillStyle = color2;
        ctx.fill();
    }

    /* ================================================================
       라이트 모드 — 잎 파티클
    ================================================================ */
    function spawnInitialLeaves() {
        for (var i = 0; i < MAX_LEAVES; i++) leaves.push(makeLeaf(true));
    }

    function makeLeaf(scattered) {
        var colors = LEAF_COLORS[currentLeaf];
        return {
            x:         Math.random() * canvas.width,
            y:         scattered ? Math.random() * canvas.height : -40,
            size:      9 + Math.random() * 9,
            color:     colors[Math.floor(Math.random() * colors.length)],
            rot:       Math.random() * Math.PI * 2,
            rotSpeed:  (Math.random() - 0.5) * 0.035,
            vx:        (Math.random() - 0.5) * 0.7,
            vy:        0.55 + Math.random() * 0.75,
            alpha:     0.45 + Math.random() * 0.3,
            swing:     Math.random() * Math.PI * 2,
            swingSpd:  0.012 + Math.random() * 0.014,
            type:      currentLeaf,
        };
    }

    function moveLeaf(l) {
        l.swing += l.swingSpd;
        l.x     += Math.sin(l.swing) * 0.55 + l.vx;
        l.y     += l.vy;
        l.rot   += l.rotSpeed;
    }

    function drawLeaf(l) {
        ctx.save();
        ctx.translate(l.x, l.y);
        ctx.rotate(l.rot);
        ctx.globalAlpha = l.alpha;
        ctx.fillStyle   = l.color;

        if (l.type === 'green')   drawGreen(l.size);
        if (l.type === 'blossom') drawBlossom(l.size);
        if (l.type === 'autumn')  drawAutumn(l.size);

        ctx.restore();
    }

    function drawLeafShape(s) {
        ctx.beginPath();
        ctx.moveTo(0, -s);
        ctx.bezierCurveTo( s * 0.55, -s * 0.5,  s * 0.55,  s * 0.3,  0,  s * 0.7);
        ctx.bezierCurveTo(-s * 0.55,  s * 0.3, -s * 0.55, -s * 0.5,  0, -s);
        ctx.fill();
        ctx.strokeStyle = 'rgba(255,255,255,0.3)';
        ctx.lineWidth   = 0.8;
        ctx.beginPath();
        ctx.moveTo(0, -s * 0.75);
        ctx.lineTo(0,  s * 0.55);
        ctx.stroke();
    }

    function drawGreen(s)   { drawLeafShape(s); }
    function drawBlossom(s) { drawLeafShape(s); }
    function drawAutumn(s)  { drawLeafShape(s); }

    /* ================================================================
       라이트 모드 — 잎 전환 버튼
    ================================================================ */
    function createSwitcher() {
        if (switcherEl) return;
        switcherEl = document.createElement('button');
        switcherEl.id    = 'leaf-switcher';
        switcherEl.title = '배경 잎 변경';
        switcherEl.textContent = LEAF_EMOJI[currentLeaf];
        switcherEl.style.cssText = [
            'position:fixed', 'bottom:24px', 'right:24px',
            'width:34px', 'height:34px', 'border-radius:50%',
            'border:1px solid rgba(0,0,0,0.1)',
            'background:rgba(255,255,255,0.72)',
            'backdrop-filter:blur(6px)',
            'font-size:15px', 'cursor:pointer', 'z-index:100',
            'display:flex', 'align-items:center', 'justify-content:center',
            'box-shadow:0 2px 8px rgba(0,0,0,0.1)',
            'transition:transform 0.18s',
        ].join(';');

        switcherEl.addEventListener('mouseenter', function () {
            switcherEl.style.transform = 'scale(1.15)';
        });
        switcherEl.addEventListener('mouseleave', function () {
            switcherEl.style.transform = 'scale(1)';
        });
        switcherEl.addEventListener('click', function () {
            var idx     = LEAF_TYPES.indexOf(currentLeaf);
            currentLeaf = LEAF_TYPES[(idx + 1) % LEAF_TYPES.length];
            localStorage.setItem('bgLeafType', currentLeaf);
            switcherEl.textContent = LEAF_EMOJI[currentLeaf];
            leaves = [];
            spawnInitialLeaves();
        });

        document.body.appendChild(switcherEl);
    }

    function removeSwitcher() {
        if (switcherEl) { switcherEl.remove(); switcherEl = null; }
    }

    /* ================================================================
       낮밤 전환 — 캔버스 페이드 (배경만 부드럽게, UI는 그대로)
    ================================================================ */
    function triggerSkyTransition() {
        /* 캔버스만 페이드 아웃 → 즉각 전환 가림 → 페이드 인 */
        canvas.style.transition = 'opacity 0.45s ease';
        canvas.style.opacity    = '0';

        setTimeout(function () {
            /* 새 모드 배경이 그려진 후 서서히 등장 */
            canvas.style.transition = 'opacity 1.3s ease';
            canvas.style.opacity    = '1';
        }, 500);
    }

    /* ================================================================
       낮밤 전환 — 해 / 달 (상시 표시 + 전환 애니메이션)
    ================================================================ */
    function injectCelestialStyles() {
        if (document.getElementById('celestial-styles')) return;
        var s = document.createElement('style');
        s.id  = 'celestial-styles';
        s.textContent = [
            '@keyframes sunSet{',
            '0%  {transform:translate(0,0)      scale(1)    rotate(0deg);  opacity:1;}',
            '100%{transform:translate(80px,68vh) scale(0.5)  rotate(20deg); opacity:0;}',
            '}',
            '@keyframes sunRise{',
            '0%  {transform:translate(80px,68vh) scale(0.5)  rotate(-20deg);opacity:0;}',
            '100%{transform:translate(0,0)       scale(1)    rotate(0deg);  opacity:1;}',
            '}',
            '@keyframes moonSet{',
            '0%  {transform:translate(0,0)       scale(1);   opacity:1;}',
            '100%{transform:translate(-80px,68vh) scale(0.5); opacity:0;}',
            '}',
            '@keyframes moonRise{',
            '0%  {transform:translate(-80px,68vh) scale(0.5); opacity:0;}',
            '100%{transform:translate(0,0)        scale(1);   opacity:1;}',
            '}',
        ].join('');
        document.head.appendChild(s);
    }

    /* 해/달 DOM을 최초 1회 생성 — 이후 상시 고정 표시 */
    function initCelestialBodies() {
        /* 공통 고정 위치 — 스크린샷 빨간 원 위치 기준 */
        var BASE = [
            'position:fixed',
            'top:10%', 'right:20%',
            'z-index:0',
            'pointer-events:none',
            'transition:opacity 0.4s ease',
        ].join(';');

        sunEl = makeSun();
        sunEl.style.cssText += ';' + BASE;

        moonEl = makeMoon();
        moonEl.style.cssText += ';' + BASE;

        document.body.appendChild(sunEl);
        document.body.appendChild(moonEl);

        applyCelestialVisibility();
    }

    /* 현재 모드에 맞게 해/달 표시 여부 적용 */
    function applyCelestialVisibility() {
        if (!sunEl || !moonEl) return;
        sunEl.style.opacity  = isDark ? '0' : '1';
        moonEl.style.opacity = isDark ? '1' : '0';
        /* transform 초기화 (애니메이션 후 리셋) */
        sunEl.style.animation  = '';
        moonEl.style.animation = '';
    }

    /* 테마 전환 시 호출 — 애니메이션 후 고정 상태로 복귀 */
    function triggerCelestialTransition(toDark) {
        if (!sunEl || !moonEl) return;

        var DUR  = '1.8s';
        var EASE = 'cubic-bezier(0.4,0,0.2,1)';

        /* transition 일시 해제 (애니메이션이 제어) */
        sunEl.style.transition  = 'none';
        moonEl.style.transition = 'none';
        sunEl.style.opacity     = '1';
        moonEl.style.opacity    = '1';

        if (toDark) {
            sunEl.style.animation  = 'sunSet   ' + DUR + ' ' + EASE + ' forwards';
            moonEl.style.animation = 'moonRise ' + DUR + ' ' + EASE + ' forwards';
        } else {
            moonEl.style.animation = 'moonSet  ' + DUR + ' ' + EASE + ' forwards';
            sunEl.style.animation  = 'sunRise  ' + DUR + ' ' + EASE + ' forwards';
        }

        /* 애니메이션 종료 후 고정 상태로 복귀 */
        setTimeout(function () {
            sunEl.style.transition  = 'opacity 0.4s ease';
            moonEl.style.transition = 'opacity 0.4s ease';
            applyCelestialVisibility();
        }, 1900);
    }

    /* 해 DOM 생성 */
    function makeSun() {
        var wrap = document.createElement('div');
        wrap.style.cssText = 'position:relative;width:64px;height:64px;';

        for (var i = 0; i < 8; i++) {
            var ray = document.createElement('div');
            ray.style.cssText = [
                'position:absolute', 'top:50%', 'left:50%',
                'width:3px', 'height:18px',
                'margin-left:-1.5px', 'margin-top:-32px',
                'background:rgba(255,215,0,0.75)',
                'border-radius:2px',
                'transform-origin:50% 32px',
                'transform:rotate(' + (i * 45) + 'deg)',
            ].join(';');
            wrap.appendChild(ray);
        }

        var body = document.createElement('div');
        body.style.cssText = [
            'position:absolute', 'top:50%', 'left:50%',
            'transform:translate(-50%,-50%)',
            'width:44px', 'height:44px', 'border-radius:50%',
            'background:radial-gradient(circle at 38% 35%,#FFF9C4,#FFD600 55%,#FF8F00)',
            'box-shadow:0 0 20px 8px rgba(255,200,0,0.65),0 0 50px 16px rgba(255,140,0,0.22)',
        ].join(';');
        wrap.appendChild(body);
        return wrap;
    }

    /* 달 DOM 생성 — SVG mask로 테두리 없는 초승달 */
    function makeMoon() {
        var wrap = document.createElement('div');
        wrap.style.cssText = 'position:relative;width:56px;height:56px;';

        var ns  = 'http://www.w3.org/2000/svg';
        var svg = document.createElementNS(ns, 'svg');
        svg.setAttribute('width',   '56');
        svg.setAttribute('height',  '56');
        svg.setAttribute('viewBox', '0 0 56 56');

        /* mask: 흰 배경에서 내부 원을 검정으로 뚫어 초승달 모양 투명 컷아웃 */
        var defs = document.createElementNS(ns, 'defs');
        var mask = document.createElementNS(ns, 'mask');
        mask.setAttribute('id', 'crescent-mask');

        var maskBg = document.createElementNS(ns, 'rect');
        maskBg.setAttribute('width',  '56');
        maskBg.setAttribute('height', '56');
        maskBg.setAttribute('fill',   'white');

        var maskCut = document.createElementNS(ns, 'circle');
        maskCut.setAttribute('cx',   '38');
        maskCut.setAttribute('cy',   '20');
        maskCut.setAttribute('r',    '21');
        maskCut.setAttribute('fill', 'black');

        mask.appendChild(maskBg);
        mask.appendChild(maskCut);
        defs.appendChild(mask);
        svg.appendChild(defs);

        /* 달 본체 — mask로 초승달 형태로 잘림 */
        var moon = document.createElementNS(ns, 'circle');
        moon.setAttribute('cx',   '26');
        moon.setAttribute('cy',   '28');
        moon.setAttribute('r',    '23');
        moon.setAttribute('fill', '#EDE8C0');
        moon.setAttribute('mask', 'url(#crescent-mask)');

        /* 은은한 글로우 — 달 뒤에 넓은 원 */
        var glow = document.createElementNS(ns, 'circle');
        glow.setAttribute('cx',      '26');
        glow.setAttribute('cy',      '28');
        glow.setAttribute('r',       '23');
        glow.setAttribute('fill',    'rgba(237,232,192,0.18)');
        glow.setAttribute('filter',  'blur(6px)');

        svg.appendChild(glow);
        svg.appendChild(moon);
        wrap.appendChild(svg);
        return wrap;
    }

    /* ================================================================
       다크 모드 — 배경 별
    ================================================================ */
    function spawnStars() {
        var skyHeight = canvas.height * 0.45;
        var count = Math.floor(canvas.width * skyHeight / 5000);
        for (var i = 0; i < count; i++) {
            stars.push({
                x:             Math.random() * canvas.width,
                y:             Math.random() * skyHeight,
                r:             0.4 + Math.random() * 1.2,
                baseAlpha:     0.4 + Math.random() * 0.5,
                alpha:         0.4 + Math.random() * 0.5,
                twinkleSpd:    0.005 + Math.random() * 0.012,
                twinkleOffset: Math.random() * Math.PI * 2,
                vx:            (Math.random() - 0.5) * 0.04,
                vy:            (Math.random() - 0.5) * 0.03,
            });
        }
    }

    function movestar(s) {
        s.x += s.vx;
        s.y += s.vy;
        var skyHeight = canvas.height * 0.45;
        if (s.x < 0)            s.x = canvas.width;
        if (s.x > canvas.width) s.x = 0;
        if (s.y < 0)            s.y = skyHeight;
        if (s.y > skyHeight)    s.y = 0;
        s.twinkleOffset += s.twinkleSpd;
        s.alpha = s.baseAlpha + Math.sin(s.twinkleOffset) * 0.2;
    }

    function drawStar(s) {
        ctx.beginPath();
        ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(255,255,255,' + s.alpha.toFixed(2) + ')';
        ctx.fill();
    }

    /* ================================================================
       다크 모드 — 별똥별
    ================================================================ */
    function scheduleShootingStar() {
        var delay = 800 + Math.random() * 2200;
        starTimeout = setTimeout(function () {
            if (isDark) {
                shootingStars.push(makeShootingStar());
                scheduleShootingStar();
            }
        }, delay);
    }

    function makeShootingStar() {
        return {
            x:        Math.random() * canvas.width  * 0.75,
            y:        Math.random() * canvas.height * 0.45,
            len:      140 + Math.random() * 160,
            speed:    18  + Math.random() * 10,
            alpha:    1.0,
            angle:    Math.PI / 4 + (Math.random() - 0.5) * 0.28,
            progress: 0,
        };
    }

    function drawShootingStar(s) {
        s.progress += s.speed;
        s.alpha    -= 0.006;
        if (s.alpha <= 0) return;

        var x2 = s.x + Math.cos(s.angle) * s.progress;
        var y2 = s.y + Math.sin(s.angle) * s.progress;
        var x1 = s.x + Math.cos(s.angle) * Math.max(0, s.progress - s.len);
        var y1 = s.y + Math.sin(s.angle) * Math.max(0, s.progress - s.len);

        var grad = ctx.createLinearGradient(x1, y1, x2, y2);
        grad.addColorStop(0, 'rgba(255,255,255,0)');
        grad.addColorStop(1, 'rgba(255,255,255,' + Math.max(0, s.alpha).toFixed(2) + ')');

        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.strokeStyle = grad;
        ctx.lineWidth   = 2.2;
        ctx.stroke();
    }

    /* ================================================================
       시작
    ================================================================ */
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

}());

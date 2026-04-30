document.addEventListener('DOMContentLoaded', function() {
    var svg = document.getElementById('jelly-container');
    if (!svg) return;

    function buildPath(x, topY, w, bottom, ox, oy) {
        var cx = x + w / 2;
        var rx = w / 2;
        return (
            'M' + x + ',' + bottom +
            ' L' + (x + ox) + ',' + (topY + rx + oy) +
            ' Q' + (x + ox) + ',' + (topY + oy) +
            ' '  + (cx + ox) + ',' + (topY + oy) +
            ' Q' + (x + w + ox) + ',' + (topY + oy) +
            ' '  + (x + w + ox) + ',' + (topY + rx + oy) +
            ' L' + (x + w) + ',' + bottom +
            ' Z'
        );
    }

    function lerp(a, b, t) { return a + (b - a) * t; }

    var charDefs = [
        {
            id: 'char-purple',
            x: 85, topY: 48, w: 76, bottom: 360,
            maxLean: 26, maxStretch: 18,
            eyeR: 7.5,
            eyes: [ {cx: 104, cy: 100}, {cx: 142, cy: 100} ],
            hasEyeWhite: true
        },
        {
            id: 'char-orange',
            x: 110, topY: 186, w: 170, bottom: 360,
            maxLean: 30, maxStretch: 18,
            eyeR: 13,
            eyes: [ {cx: 165, cy: 235}, {cx: 225, cy: 235} ],
            hasEyeWhite: true,
            smile:      { p1x: 175, p1y: 258, cpx: 195, cpy: 274, p2x: 215, p2y: 258 },
            smileFocus: { p1x: 178, p1y: 258, cpx: 195, cpy: 258, p2x: 212, p2y: 258 }
        },
        {
            id: 'char-pink',
            x: 238, topY: 150, w: 92, bottom: 360,
            maxLean: 24, maxStretch: 18,
            eyeR: 9,
            eyes: [ {cx: 263, cy: 204}, {cx: 305, cy: 204} ],
            hasEyeWhite: true
        },
        {
            id: 'char-yellow',
            x: 295, topY: 250, w: 120, bottom: 360,
            maxLean: 22, maxStretch: 14,
            eyeR: 9,
            eyes: [ {cx: 325, cy: 316}, {cx: 385, cy: 316} ],
            hasEyeWhite: true,
            linemouth:      { x1: 328, y1: 338, x2: 382, y2: 338 },
            linemouthFocus: { x1: 334, y1: 338, x2: 376, y2: 338 }
        }
    ];

    /* DOM 수집 + 눈 감기용 line 요소 동적 생성 */
    var chars = [];
    for (var i = 0; i < charDefs.length; i++) {
        var def   = charDefs[i];
        var group = document.getElementById(def.id);
        if (!group) {
            console.warn('[index-interaction] 요소를 찾을 수 없음: #' + def.id);
            continue;
        }

        var eyeClosedEls = [];
        if (def.hasEyeWhite) {
            for (var j = 0; j < def.eyes.length; j++) {
                var cl = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                cl.setAttribute('stroke', '#222');
                cl.setAttribute('stroke-width', '3');
                cl.setAttribute('stroke-linecap', 'round');
                cl.setAttribute('opacity', '0');
                group.appendChild(cl);
                eyeClosedEls.push(cl);
            }
        }

        chars.push({
            def:          def,
            bodyEl:       group.querySelector('.body'),
            eyeWEls:      group.querySelectorAll('.eye-white'),
            pupilEls:     group.querySelectorAll('.pupil'),
            mouthEl:      group.querySelector('.mouth'),
            eyeClosedEls: eyeClosedEls,
            ox: 0, oy: 0,
            targetOx: 0, targetOy: 0,
            hasMouse: false, mouseX: 0, mouseY: 0,
            eyeShift:  0, targetEyeShift:  0,
            eyeExpand: 0, targetEyeExpand: 0,
            eyeClose:  0, targetEyeClose:  0
        });
    }

    if (chars.length === 0) return;

    /* ---------------------------------------------------------------
       모드 상태  'idle' | 'email' | 'password'
    --------------------------------------------------------------- */
    var mode      = 'idle';
    var nodOffset = 0, nodTarget = 0, nodTimer = null;
    var peekTimer = null, isPeeking = false;

    function enterEmailFocus() {
        mode = 'email';
        clearPeekTimer();
        for (var i = 0; i < chars.length; i++) {
            var ch = chars[i];
            ch.hasMouse        = true;
            ch.mouseX          = 650;
            ch.mouseY          = 280;
            ch.targetOx        =  ch.def.maxLean    * 0.8;
            ch.targetOy        = -ch.def.maxStretch * 0.45;
            ch.targetEyeShift  = 5;
            ch.targetEyeExpand = 4;
            ch.targetEyeClose  = 0;
        }
    }

    function enterPasswordFocus() {
        mode = 'password';
        nodOffset = 0; nodTarget = 0;
        clearTimeout(nodTimer);
        clearPeekTimer();
        for (var i = 0; i < chars.length; i++) {
            var ch = chars[i];
            ch.hasMouse        = false;
            ch.targetOx        = -ch.def.maxLean * 0.85;
            ch.targetOy        = 0;
            ch.targetEyeShift  = -6;
            ch.targetEyeExpand = 0;
            ch.targetEyeClose  = 1;
        }
        schedulePeek();
    }

    function exitFocus() {
        mode = 'idle';
        clearPeekTimer();
        nodOffset = 0; nodTarget = 0;
        clearTimeout(nodTimer);
        for (var i = 0; i < chars.length; i++) {
            var ch = chars[i];
            ch.hasMouse        = false;
            ch.targetOx        = 0;
            ch.targetOy        = 0;
            ch.targetEyeShift  = 0;
            ch.targetEyeExpand = 0;
            ch.targetEyeClose  = 0;
        }
    }

    /* ---------------------------------------------------------------
       보라 캐릭터 염탐 (비밀번호 모드 한정)
    --------------------------------------------------------------- */
    function clearPeekTimer() {
        clearTimeout(peekTimer);
        isPeeking = false;
    }

    function schedulePeek() {
        var delay = 1000 + Math.random() * 1500;
        peekTimer = setTimeout(startPeek, delay);
    }

    function startPeek() {
        if (mode !== 'password') return;
        isPeeking = true;
        var purple = chars[0];
        purple.targetOx       = purple.def.maxLean * 0.55;
        purple.targetEyeShift = 0;
        purple.targetEyeClose = 0;
        purple.hasMouse       = true;
        purple.mouseX         = 650;
        purple.mouseY         = 280;

        peekTimer = setTimeout(endPeek, 900 + Math.random() * 600);
    }

    function endPeek() {
        if (mode !== 'password') return;
        isPeeking = false;
        var purple = chars[0];
        purple.targetOx       = -purple.def.maxLean * 0.85;
        purple.targetEyeShift = -6;
        purple.targetEyeClose = 1;
        purple.hasMouse       = false;
        schedulePeek();
    }

    /* ---------------------------------------------------------------
       타이핑 끄덕임 (이메일 모드만)
    --------------------------------------------------------------- */
    function triggerNod() {
        nodTarget = -10;
        clearTimeout(nodTimer);
        nodTimer = setTimeout(function() { nodTarget = 0; }, 160);
    }

    /* ---------------------------------------------------------------
       입력 이벤트 연결
    --------------------------------------------------------------- */
    var emailInput    = document.getElementById('email');
    var passwordInput = document.getElementById('password');

    if (emailInput) {
        emailInput.addEventListener('focus', enterEmailFocus);
        emailInput.addEventListener('blur',  exitFocus);
        emailInput.addEventListener('input', function() {
            if (mode === 'email') triggerNod();
        });
    }
    if (passwordInput) {
        passwordInput.addEventListener('focus', enterPasswordFocus);
        passwordInput.addEventListener('blur',  exitFocus);
    }

    /* ---------------------------------------------------------------
       캐릭터 하나 업데이트
    --------------------------------------------------------------- */
    function applyChar(ch) {
        var def = ch.def;
        var ox  = ch.ox;
        var oy  = ch.oy;
        var es  = ch.eyeShift;
        var ee  = ch.eyeExpand;
        var cp  = ch.eyeClose;

        ch.bodyEl.setAttribute('d', buildPath(def.x, def.topY, def.w, def.bottom, ox, oy));

        for (var j = 0; j < ch.eyeWEls.length; j++) {
            ch.eyeWEls[j].setAttribute('cx', def.eyes[j].cx + ox + es);
            ch.eyeWEls[j].setAttribute('cy', def.eyes[j].cy + oy);
            ch.eyeWEls[j].setAttribute('r',  Math.max(0, (def.eyeR + ee) * (1 - cp)));
        }

        for (var j = 0; j < ch.pupilEls.length; j++) {
            var eye = def.eyes[j];
            var ecx = eye.cx + ox + es;
            var ecy = eye.cy + oy;
            ch.pupilEls[j].setAttribute('opacity', Math.max(0, 1 - cp * 2));
            if (ch.hasMouse && cp < 0.4) {
                var pdx  = ch.mouseX - ecx;
                var pdy  = ch.mouseY - ecy;
                var dist = Math.sqrt(pdx * pdx + pdy * pdy) || 1;
                var move = Math.min(dist * 0.05, 3);
                ch.pupilEls[j].setAttribute('cx', ecx + (pdx / dist) * move);
                ch.pupilEls[j].setAttribute('cy', ecy + (pdy / dist) * move);
            } else {
                ch.pupilEls[j].setAttribute('cx', ecx);
                ch.pupilEls[j].setAttribute('cy', ecy);
            }
        }

        for (var j = 0; j < ch.eyeClosedEls.length; j++) {
            var eye  = def.eyes[j];
            var ecx  = eye.cx + ox + es;
            var ecy  = eye.cy + oy;
            var half = def.eyeR * 0.75;
            ch.eyeClosedEls[j].setAttribute('x1', ecx - half);
            ch.eyeClosedEls[j].setAttribute('y1', ecy);
            ch.eyeClosedEls[j].setAttribute('x2', ecx + half);
            ch.eyeClosedEls[j].setAttribute('y2', ecy);
            ch.eyeClosedEls[j].setAttribute('opacity', cp);
        }

        if (ch.mouthEl) {
            var smile = (mode === 'email' && def.smileFocus) ? def.smileFocus : def.smile;
            var line  = (mode === 'email' && def.linemouthFocus) ? def.linemouthFocus : def.linemouth;

            if (smile) {
                var s = smile;
                ch.mouthEl.setAttribute('d',
                    'M'  + (s.p1x + ox + es * 0.5) + ',' + (s.p1y + oy) +
                    ' Q' + (s.cpx + ox + es * 0.5) + ',' + (s.cpy + oy) +
                    ' '  + (s.p2x + ox + es * 0.5) + ',' + (s.p2y + oy)
                );
            } else if (line) {
                var l = line;
                ch.mouthEl.setAttribute('x1', l.x1 + ox + es * 0.5);
                ch.mouthEl.setAttribute('y1', l.y1 + oy);
                ch.mouthEl.setAttribute('x2', l.x2 + ox + es * 0.5);
                ch.mouthEl.setAttribute('y2', l.y2 + oy);
            }
        }
    }

    /* ---------------------------------------------------------------
       매 프레임
    --------------------------------------------------------------- */
    function tick() {
        requestAnimationFrame(tick);

        nodOffset = lerp(nodOffset, nodTarget, 0.18);

        for (var i = 0; i < chars.length; i++) {
            var ch = chars[i];
            ch.ox        = lerp(ch.ox,        ch.targetOx,        0.14);
            ch.eyeShift  = lerp(ch.eyeShift,  ch.targetEyeShift,  0.12);
            ch.eyeExpand = lerp(ch.eyeExpand, ch.targetEyeExpand, 0.12);
            ch.eyeClose  = lerp(ch.eyeClose,  ch.targetEyeClose,  0.12);

            var combinedOy = Math.max(-ch.def.maxStretch, ch.targetOy + nodOffset);
            ch.oy = lerp(ch.oy, combinedOy, 0.14);

            applyChar(ch);
        }
    }

    /* ---------------------------------------------------------------
       마우스 추적 (idle 모드만)
    --------------------------------------------------------------- */
    window.addEventListener('mousemove', function(e) {
        if (mode !== 'idle') return;
        var r    = svg.getBoundingClientRect();
        var svgX = (e.clientX - r.left) / r.width  * 500;
        var svgY = (e.clientY - r.top)  / r.height * 400;

        for (var i = 0; i < chars.length; i++) {
            var ch  = chars[i];
            var def = ch.def;
            var cx  = def.x + def.w / 2;

            ch.hasMouse = true;
            ch.mouseX   = svgX;
            ch.mouseY   = svgY;

            var dx = svgX - cx;
            ch.targetOx = Math.max(-def.maxLean, Math.min(def.maxLean, dx * 0.07));

            var dy = svgY - def.bottom;
            ch.targetOy = Math.max(-def.maxStretch, Math.min(0, dy * 0.05));
        }
    });

    document.addEventListener('mouseleave', function() {
        if (mode !== 'idle') return;
        for (var i = 0; i < chars.length; i++) {
            chars[i].targetOx = 0;
            chars[i].targetOy = 0;
            chars[i].hasMouse = false;
        }
    });

    tick();
});

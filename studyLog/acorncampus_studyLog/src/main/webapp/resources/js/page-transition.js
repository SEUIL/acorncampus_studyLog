/**
 * page-transition.js — 페이지 진입/이탈 전환 효과
 *
 * 설계 원칙:
 *   - CSS animation 미사용: animation-fill-mode 가 opacity/transform 을 점유하면
 *     transition 이 발동하지 않는 구조적 충돌이 생김
 *   - CSS 에서 body { opacity:0 } 으로 초기 상태 설정 (첫 paint 전에 적용됨)
 *   - JS transition 만으로 진입/이탈 모두 제어 → CSS 클래스 토글 불필요
 *
 * 포함 위치:
 *   - sideBar.jsp 의 <aside> 앞 (dashboard 페이지 공통)
 *   - index.jsp 의 </body> 앞 (auth 페이지)
 */
(function () {
    var body = document.body;

    /**
     * 페이지 진입 fade-in
     * 두 번의 rAF 을 사용하는 이유:
     *   rAF1: 브라우저가 초기 opacity:0 상태를 실제로 렌더링하도록 한 프레임 확보
     *   rAF2: 그 다음 프레임에서 opacity:1 설정 → 브라우저가 0→1 transition 을 인식
     *   rAF 한 번만 쓰면 초기 상태와 목표 상태가 같은 프레임에 처리되어 transition 이 생략됨
     */
    requestAnimationFrame(function () {
        requestAnimationFrame(function () {
            body.style.opacity   = '1';
            body.style.transform = 'translateY(0)';
        });
    });
})();

/**
 * 페이지 이탈 fade-out 후 이동
 * header.jsp 의 전환 버튼 onclick 에서 호출
 * CSS 클래스 방식 미사용: 인라인 style 로 직접 제어하면 cascade/specificity 간섭 없음
 */
function navigateWithTransition(url) {
    var body = document.body;
    body.style.transition    = 'opacity 0.2s ease-in, transform 0.2s ease-in';
    body.style.opacity       = '0';
    body.style.transform     = 'translateY(-10px)';
    body.style.pointerEvents = 'none';
    setTimeout(function () { location.href = url; }, 220);
}

/**
 * milkdown-editor.js
 * Milkdown WYSIWYG 마크다운 에디터 초기화 모듈
 * write.jsp에서 <script type="module">으로 import해서 사용
 *
 * 버전 고정: 패키지 간 호환성 보장을 위해 7.3.6으로 통일
 * gfm 제거: @milkdown/preset-gfm@7.3.6이 core@7.3.6에 없는 pasteRulesCtx를 요구해 충돌 — commonmark만 사용
 * listener 순서: .use(listener) 이후 두 번째 .config()에서 listenerCtx 접근 (순서 오류 수정)
 */

import { Editor, rootCtx, defaultValueCtx, editorViewCtx } from 'https://esm.sh/@milkdown/core@7.3.6'
import { commonmark }            from 'https://esm.sh/@milkdown/preset-commonmark@7.3.6?deps=@milkdown/core@7.3.6'
import { history }               from 'https://esm.sh/@milkdown/plugin-history@7.3.6?deps=@milkdown/core@7.3.6'
import { listener, listenerCtx } from 'https://esm.sh/@milkdown/plugin-listener@7.3.6?deps=@milkdown/core@7.3.6'

let _markdown = ''
let _editor   = null

/**
 * Milkdown 에디터 초기화
 * @param {HTMLElement} el        - 에디터를 마운트할 DOM 요소
 * @param {string} initialContent - 초기 마크다운 내용 (수정 모드 시 기존 본문)
 * @throws 초기화 실패 시 Error — 호출부에서 try/catch 후 폴백 처리
 */
export async function initEditor(el, initialContent = '') {
    _markdown = initialContent

    _editor = await Editor.make()
        .config(ctx => {
            ctx.set(rootCtx, el)
            ctx.set(defaultValueCtx, initialContent)
        })
        .use(commonmark)
        .use(history)
        .use(listener)
        .config(ctx => {
            ctx.get(listenerCtx).markdownUpdated((_ctx, markdown) => {
                _markdown = markdown
            })
        })
        .create()

    try {
        _editor.action(ctx => {
            const view = ctx.get(editorViewCtx)
            const HINT = '/ 를 입력해 입력 메뉴 열기'
            view.dom.dataset.placeholder = HINT

            const sync = () => {
                const empty = view.state.doc.textContent === ''
                view.dom.classList.toggle('pm-placeholder', empty)
            }

            setTimeout(sync, 50)
            view.dom.addEventListener('input', sync)
            view.dom.addEventListener('keyup', sync)
        })
    } catch (e) {
        console.warn('[Milkdown] placeholder 설정 실패', e)
    }
}

/** 현재 에디터 마크다운 내용 반환 (form submit 시 호출) */
export function getMarkdown() {
    return _markdown
}

/** ProseMirror 이미지 노드를 커서 위치에 직접 삽입 */
export function insertImage(url, alt = '') {
    if (!_editor) return
    _editor.action(ctx => {
        const view  = ctx.get(editorViewCtx)
        const { state, dispatch } = view
        const node  = state.schema.nodes.image?.create({ src: url, alt, title: '' })
        if (!node) return
        dispatch(state.tr.replaceSelectionWith(node))
        view.focus()
    })
}

/** ProseMirror 링크 마크를 커서 위치에 직접 삽입 */
export function insertLink(text, url) {
    if (!_editor) return
    _editor.action(ctx => {
        const view  = ctx.get(editorViewCtx)
        const { state, dispatch } = view
        const mark  = state.schema.marks.link?.create({ href: url, title: '' })
        if (!mark) return
        const node  = state.schema.text(text, [mark])
        const { from, to } = state.selection
        dispatch(state.tr.replaceWith(from, to, node))
        view.focus()
    })
}

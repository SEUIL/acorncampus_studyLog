import { insertImage, insertLink } from './milkdown-editor.js'

/**
 * milkdown-slash.js
 * Notion 스타일 / 슬래시 커맨드 메뉴
 *
 * 사용법: initSlashCommand(editorEl, contextPath)
 *
 * insert: 문자열  → / 삭제 후 prefix 삽입, Milkdown 입력 규칙이 자동 변환
 * special: 문자열 → 커스텀 핸들러 실행 (코드 블록 / 이미지 / 파일 링크)
 */

const COMMANDS = [
    { icon: '¶',    label: '텍스트',      desc: '기본 단락',         insert: ''          },
    { icon: 'H1',   label: '제목 1',      desc: '큰 섹션 제목',      insert: '# '        },
    { icon: 'H2',   label: '제목 2',      desc: '중간 섹션 제목',    insert: '## '       },
    { icon: 'H3',   label: '제목 3',      desc: '작은 섹션 제목',    insert: '### '      },
    { icon: '•',    label: '불릿 리스트', desc: '순서 없는 목록',    insert: '- '        },
    { icon: '1.',   label: '번호 리스트', desc: '순서 있는 목록',    insert: '1. '       },
    { icon: '"',    label: '인용구',      desc: '인용 블록',         insert: '> '        },
    { icon: '</>',  label: '코드 블록',   desc: '코드 입력',         special: 'codeblock'},
    { icon: '🖼',    label: '이미지',      desc: '이미지 업로드',     special: 'image'    },
    { icon: '📎',    label: '파일 링크',   desc: 'URL로 파일 연결',   special: 'filelink' },
    { icon: '—',    label: '구분선',       desc: '수평선 삽입',       special: 'hr'       },
]

export function initSlashCommand(editorEl, contextPath = '') {
    const menu = buildMenu()
    document.body.appendChild(menu)

    /* ── / 감지 ── */
    editorEl.addEventListener('input', () => {
        if (getBlockText() === '/') {
            const rect = getCursorRect()
            if (rect) show(rect)
        } else {
            hide()
        }
    })

    editorEl.addEventListener('keydown', e => {
        if (e.key === 'Escape') hide()
    })

    document.addEventListener('mousedown', e => {
        if (!menu.contains(e.target)) hide()
    })

    window.addEventListener('scroll', e => {
        if (menu.style.display !== 'none') {
            if (menu.contains(e.target)) return   // 메뉴 내부 스크롤 무시
            const rect = getCursorRect()
            if (rect) reposition(rect)
            else hide()
        }
    }, true)

    /* ── 유틸 ── */
    function getBlockText() {
        const sel = window.getSelection()
        if (!sel || !sel.rangeCount) return ''
        let node = sel.getRangeAt(0).startContainer
        if (node.nodeType === Node.TEXT_NODE) node = node.parentNode
        const BLOCK = ['P', 'H1', 'H2', 'H3', 'H4', 'LI', 'BLOCKQUOTE']
        while (node && !BLOCK.includes(node.nodeName)) node = node.parentNode
        return node ? node.textContent.trim() : ''
    }

    function getCursorRect() {
        const sel = window.getSelection()
        if (!sel || !sel.rangeCount) return null
        const rect = sel.getRangeAt(0).getBoundingClientRect()
        return (rect.width === 0 && rect.height === 0) ? null : rect
    }

    function show(rect) {
        reposition(rect)
        menu.style.display = 'block'
    }

    function reposition(rect) {
        const menuH  = menu.offsetHeight || 360
        const spaceB = window.innerHeight - rect.bottom
        const top    = spaceB > menuH + 8 ? rect.bottom + 6 : rect.top - menuH - 6
        menu.style.top  = top + 'px'
        menu.style.left = Math.min(rect.left, window.innerWidth - 296) + 'px'
    }

    function hide() { menu.style.display = 'none' }

    /* ── 커맨드 실행 ── */
    function applyInsert(insert) {
        hide()
        document.execCommand('delete', false, null)    // / 삭제
        if (insert) document.execCommand('insertText', false, insert)
    }

    function handleSpecial(type) {
        hide()
        document.execCommand('delete', false, null)    // / 삭제
        if      (type === 'codeblock') handleCodeBlock()
        else if (type === 'hr')        handleHr()
        else if (type === 'image')     handleImage()
        else if (type === 'filelink')  handleFileLink()
    }

    /* ── 코드 블록 ──
       execCommand로 ``` 삽입 후 한 프레임 뒤 ProseMirror에 Enter 전달
       ProseMirror의 입력 규칙이 ``` + Enter를 코드 블록으로 변환 */
    function handleCodeBlock() {
        document.execCommand('insertText', false, '```')
        requestAnimationFrame(() => {
            const pm = editorEl.querySelector('.ProseMirror')
            if (!pm) return
            pm.dispatchEvent(new KeyboardEvent('keydown', {
                key: 'Enter', code: 'Enter', keyCode: 13,
                bubbles: true, cancelable: true
            }))
        })
    }

    /* ── 구분선 ── */
    function handleHr() {
        document.execCommand('insertText', false, '---')
        requestAnimationFrame(() => {
            const pm = editorEl.querySelector('.ProseMirror')
            if (!pm) return
            pm.dispatchEvent(new KeyboardEvent('keydown', {
                key: 'Enter', code: 'Enter', keyCode: 13,
                bubbles: true, cancelable: true
            }))
        })
    }

    /* ── 이미지 업로드 ── */
    function handleImage() {
        const input = document.createElement('input')
        input.type   = 'file'
        input.accept = 'image/*'

        input.onchange = async () => {
            const file = input.files?.[0]
            if (!file) return

            const formData = new FormData()
            formData.append('image', file)

            try {
                const res  = await fetch(contextPath + '/post/upload.do', {
                    method: 'POST', body: formData
                })
                const data = await res.json()
                if (data.url) {
                    insertImage(data.url, file.name)
                } else {
                    alert('이미지 업로드에 실패했습니다.')
                }
            } catch (e) {
                console.error('[slash/image] 업로드 실패:', e)
                alert('이미지 업로드 중 오류가 발생했습니다.')
            }
        }

        input.click()
    }

    /* ── 파일 링크 (업로드) ── */
    function handleFileLink() {
        const input  = document.createElement('input')
        input.type   = 'file'

        input.onchange = async () => {
            const file = input.files?.[0]
            if (!file) return

            const formData = new FormData()
            formData.append('file', file)

            try {
                const res  = await fetch(contextPath + '/l_check/post/file-upload.do', {
                    method: 'POST', body: formData
                })
                const data = await res.json()
                if (data.url) {
                    insertLink(file.name, data.url)
                } else {
                    alert('파일 업로드에 실패했습니다.')
                }
            } catch (e) {
                console.error('[slash/file] 업로드 실패:', e)
                alert('파일 업로드 중 오류가 발생했습니다.')
            }
        }

        input.click()
    }

    function focusPM() {
        const pm = editorEl.querySelector('.ProseMirror')
        pm?.focus()
    }

    /* ── 메뉴 DOM 구성 ── */
    function buildMenu() {
        const m = document.createElement('div')
        m.className = 'milkdown-slash-menu'

        const header = document.createElement('div')
        header.className   = 'slash-header'
        header.textContent = '블록 추가'
        m.appendChild(header)

        COMMANDS.forEach(cmd => {
            const item = document.createElement('button')
            item.type      = 'button'
            item.className = 'slash-item'
            item.innerHTML = `
                <span class="slash-icon">${cmd.icon}</span>
                <span class="slash-text">
                    <strong>${cmd.label}</strong>
                    <small>${cmd.desc}</small>
                </span>`

            item.addEventListener('mousedown', e => {
                e.preventDefault()
                if (cmd.special) handleSpecial(cmd.special)
                else             applyInsert(cmd.insert)
            })
            m.appendChild(item)
        })

        return m
    }
}

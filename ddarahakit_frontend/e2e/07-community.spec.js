import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

const QUESTION_POST_IDX = 102

test.describe('커뮤니티 댓글(답변) 작성', () => {
    test('답변 작성 → 목록 반영(새로고침 없이) + 에디터 초기화', async ({ page }) => {
        const marker = 'E2E답변-' + Date.now()

        await login(page)
        await page.goto(`/community/${QUESTION_POST_IDX}`)

        // 답변 작성 에디터(Quill) 대기
        const editor = page.locator('.comment-editor-wrapper .ql-editor')
        await expect(editor).toBeVisible({ timeout: 15000 })

        // 전체 새로고침 감지 마커
        await page.evaluate(() => { window.__noReloadComment = 'kept' })

        // Quill 에 입력(키 이벤트로 text-change 트리거)
        await editor.click()
        await page.keyboard.type(marker)

        const submit = page.getByRole('button', { name: '답변 등록' })
        await expect(submit).toBeEnabled({ timeout: 8000 })
        await submit.click()

        // 새 답변이 목록에 표시(refetch)
        await expect(page.getByText(marker).first()).toBeVisible({ timeout: 15000 })

        // 전체 새로고침이 아니어야 함(D-3: reload→refetch)
        const kept = await page.evaluate(() => window.__noReloadComment === 'kept')
        expect(kept, '답변 등록 후 전체 새로고침이 일어나면 안 됨').toBe(true)

        // 작성 에디터는 초기화(key 리마운트)
        await expect(editor).not.toContainText(marker)
    })
})

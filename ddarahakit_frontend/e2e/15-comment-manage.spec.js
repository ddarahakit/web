import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

const QUESTION_POST_IDX = 102

test.describe('커뮤니티 댓글 수정/삭제', () => {
    test('내 댓글 작성 → 수정 → 삭제 (모두 새로고침 없이)', async ({ page }) => {
        const marker = 'E2E관리-' + Date.now()
        page.on('dialog', (d) => d.accept()) // 삭제 confirm 자동 수락

        await login(page)
        await page.goto(`/community/${QUESTION_POST_IDX}`)

        // 1) 댓글 작성
        const createEditor = page.locator('.comment-editor-wrapper .ql-editor')
        await expect(createEditor).toBeVisible({ timeout: 15000 })
        await createEditor.click()
        await page.keyboard.type(marker)
        await page.getByRole('button', { name: '답변 등록' }).click()
        await expect(page.getByText(marker).first()).toBeVisible({ timeout: 15000 })

        // 2) 내 댓글 카드 찾기 → 수정
        const card = page.locator('div.bg-white.rounded-2xl').filter({ hasText: marker }).last()
        await card.getByRole('button', { name: '수정' }).click()
        const editEditor = card.locator('.ql-editor')
        await expect(editEditor).toBeVisible()
        await editEditor.click()
        await page.keyboard.press('End')
        await page.keyboard.type('-수정됨')
        await card.getByRole('button', { name: '저장' }).click()
        await expect(page.getByText(marker + '-수정됨').first()).toBeVisible({ timeout: 15000 })

        // 3) 삭제 (confirm 자동 수락) → 목록에서 사라짐
        const editedCard = page.locator('div.bg-white.rounded-2xl').filter({ hasText: marker + '-수정됨' }).last()
        await editedCard.getByRole('button', { name: '삭제' }).click()
        await expect(page.getByText(marker + '-수정됨')).toHaveCount(0, { timeout: 15000 })
    })
})

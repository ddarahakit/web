import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

test.describe('커뮤니티 게시글 작성', () => {
    test('새 글 작성 → 상세 페이지로 이동 + 제목 표시', async ({ page }) => {
        const title = 'E2E게시글-' + Date.now()

        await login(page)
        await page.goto('/community/reg')

        // 카테고리(질문) 선택 → postType 설정 (라디오가 hidden 이라 force)
        await page.locator('#cat-question').check({ force: true })

        // 제목 입력
        const titleInput = page.locator('input[placeholder="제목을 입력하세요"]')
        await titleInput.fill(title)
        await titleInput.blur()

        // 본문(Quill) 입력
        const editor = page.locator('.ql-editor')
        await expect(editor).toBeVisible({ timeout: 15000 })
        await editor.click()
        await page.keyboard.type('E2E 자동 작성 본문입니다.')

        // 게시
        const submit = page.getByRole('button', { name: '게시하기' })
        await expect(submit).toBeEnabled({ timeout: 8000 })
        await submit.click()

        // 작성한 글 상세로 이동 + 제목 노출
        await expect(page).toHaveURL(/\/community\/\d+/, { timeout: 15000 })
        await expect(page.getByText(title).first()).toBeVisible({ timeout: 15000 })
    })
})

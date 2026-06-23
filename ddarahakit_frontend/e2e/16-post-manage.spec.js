import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

test.describe('커뮤니티 게시글 수정/삭제', () => {
    test('내 글 작성 → 수정 → 삭제', async ({ page }) => {
        const title = 'E2E글관리-' + Date.now()
        const editedTitle = title + '-수정'
        page.on('dialog', (d) => d.accept()) // 삭제 confirm 자동 수락

        await login(page)

        // 1) 작성
        await page.goto('/community/reg')
        await page.locator('#cat-question').check({ force: true })
        await page.locator('input[placeholder="제목을 입력하세요"]').fill(title)
        const editor = page.locator('.ql-editor')
        await editor.click()
        await page.keyboard.type('E2E 자동 작성 본문')
        await page.getByRole('button', { name: '게시하기' }).click()
        await expect(page).toHaveURL(/\/community\/\d+/, { timeout: 15000 })
        await expect(page.getByText(title).first()).toBeVisible()

        // 2) 수정 (더보기 메뉴 → 수정 → 편집 페이지)
        await page.locator('button:has(i.fa-ellipsis-vertical)').click()
        await page.getByRole('button', { name: '수정' }).click()
        await expect(page).toHaveURL(/\/community\/edit\/\d+/, { timeout: 15000 })
        const titleInput = page.locator('input[placeholder="제목을 입력하세요"]')
        await titleInput.fill(editedTitle)
        await titleInput.blur()
        await page.getByRole('button', { name: '수정하기' }).click()
        await expect(page).toHaveURL(/\/community\/\d+/, { timeout: 15000 })
        await expect(page.getByText(editedTitle).first()).toBeVisible({ timeout: 15000 })

        // 3) 삭제 (더보기 메뉴 → 삭제 → confirm → 목록 이동)
        await page.locator('button:has(i.fa-ellipsis-vertical)').click()
        await page.getByRole('button', { name: '삭제' }).click()
        await expect(page).toHaveURL(/\/community\/list/, { timeout: 15000 })
    })
})

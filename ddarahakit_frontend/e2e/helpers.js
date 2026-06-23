// E2E 공용 헬퍼

export const TEST_USER = { email: 'e2e@test.com', password: 'Test1234!' }

// 구매(시드)된 코스/강의
export const PAID_COURSE_IDX = 11      // Vue.js (35강)
export const FIRST_LECTURE_IDX = 400   // 첫 강의

/** 이메일/비밀번호 로그인 후 /course/list 도착까지 대기 */
export async function login(page) {
    await page.goto('/user/login')
    await page.fill('#email', TEST_USER.email)
    await page.locator('#email').blur()
    await page.fill('#password', TEST_USER.password)
    await page.locator('#password').blur()
    // 유효성 통과 시 버튼 활성화 → 클릭(혹은 Enter 폴백)
    const btn = page.locator('#submitBtn')
    await btn.waitFor({ state: 'visible' })
    if (await btn.isEnabled().catch(() => false)) {
        await btn.click()
    } else {
        await page.locator('#password').press('Enter')
    }
    await page.waitForURL('**/course/list', { timeout: 20000 })
}

import { defineConfig } from '@playwright/test'

// E2E: 로컬 dev 프론트(Vite, http://localhost:81) + dev 백엔드(:8080) 대상.
// 백엔드 CORS 가 :81 을 허용하지 않아도 되도록 --disable-web-security 로 실행한다.
// 시스템 Chrome 사용(브라우저 다운로드 불필요).
export default defineConfig({
    testDir: './e2e',
    timeout: 45000,
    expect: { timeout: 12000 },
    fullyParallel: false,
    workers: 1,
    retries: 0,
    reporter: [['list']],
    use: {
        baseURL: 'http://localhost:81',
        headless: true,
        channel: 'chrome',
        actionTimeout: 12000,
        navigationTimeout: 25000,
        launchOptions: {
            args: [
                '--disable-web-security',
                '--disable-features=IsolateOrigins,site-per-process',
                '--disable-site-isolation-trials',
            ],
        },
        screenshot: 'only-on-failure',
        trace: 'retain-on-failure',
    },
})

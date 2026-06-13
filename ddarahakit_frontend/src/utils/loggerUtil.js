import { createLogger } from 'vue-logger-plugin'

//운영 모드 여부
const isProduction = import.meta.env.MODE === 'production'

//로거 객체
const logger = createLogger({
    enabled: true,
    consoleEnabled: true,
    level: isProduction ? 'error' : 'debug', //log < error < warn < info < debug
    callerInfo: true
})

export default logger

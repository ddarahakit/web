/**
 * 로그인 정보 저장소 객체
 *
 * 로그인 정보를 저장소에 저장한다.
 */
import { inject, ref, toRef } from 'vue'
import { defineStore } from 'pinia'
import { EncryptStorage } from 'encrypt-storage'
import api from '@/api/user'

//암복호화 객체
const encryptStorage = new EncryptStorage(import.meta.env.VITE_STORAGE_SECRET_KEY, { prefix: '@IT' })

//로그인 정보 객체
const useAuthStore = defineStore('auth', () => {
    //로그인 여부
    const isLogin = ref(false)

    //프로필 이미지 (헤더 등에서 반응형으로 참조 → 변경 시 즉시 반영)
    const profileImage = ref('')

    /**
     * 토큰 정보 저장
     *
     * 현재 시간에 ms(ttl) 시간을 더해 만료 시간을 설정한다.
     * 유효시간은 10분이나 보정을 위해 11분으로 최종시간을 지정한다.
     */
    const setWithExpiry = (mode, key, results, ttl) => {
        //현재 시간
        const now = new Date()

        //스토리지에 정보할 정보
        let item = {}

        if (mode === 'LOGIN') {
            //스토리지 로그인 정보 세팅
            item = {
                userIdx: results.idx,
                userEmail: results.email,
                userName: results.name,
                userRole: results.role,
                userProfileImage: results.profileImageUrl
            }
        } else if (mode === 'LXTS') {
            //기존 스토리지 정보 획득
            item = getWithExpiry('store')
        }

        //유효시간
        item.expiry = now.getTime() + Number(ttl) + Number(import.meta.env.VITE_LOGIN_EXTRA_TIME)

        //스토리지에 저장
        encryptStorage.setItem(key, JSON.stringify(item))
    }

    /**
     * 토큰 정보 획득
     *
     * 키 값에 해당하는 스토리지 정보를 반환한다.
     */
    const getWithExpiry = (key) => {
        //키에 해당하는 스토리지 정보 획득
        const itemStr = encryptStorage.getItem(key)

        //미존재 시 null 반환
        if (!itemStr) {
            return null
        }

        const item = itemStr
        const now = new Date()

        //유효시간이 만료된 경우 스토리지 정보 삭제 후 null 반환
        if (now.getTime() > Number(item.expiry)) {
            encryptStorage.removeItem(key)
            return null
        }

        return item
    }

    /**
     * 로그인 여부
     *
     * 스토리지에 존재하는 로그인 여부를 반환한다.
     * @returns true or false
     */
    const checkLogin = () => {
        return getWithExpiry('store') !== null
    }

    /**
     * 로그인
     *
     * 토근 정보를 스토리지에 저장한다.
     */
    const login = (results) => {
        //스토리지 로그인 정보 저장
        setWithExpiry('LOGIN', 'store', results, import.meta.env.VITE_LOGIN_VALIDITY_TIME)

        //초기 설정
        initSettings()
    }

    /**
     * 로그아웃
     *
     * 브라우저 스토리지의 인증 정보를 정리하고 로그인 상태를 변경한다.
     * (HttpOnly 쿠키는 JS로 지울 수 없어 백엔드 /user/logout 응답의 Set-Cookie 로 삭제된다)
     */
    const logout = () => {
        //인증 세션 정보 삭제
        encryptStorage.removeItem('store')

        //혹시 남아있을 수 있는 인증/세션 스토리지 정리
        try {
            //로그인 플로우(OAuth 등)에서 쓰던 세션 스토리지 비우기
            sessionStorage.clear()
            //@IT 프리픽스로 저장된 잔여 인증 키 제거 (store 외 토큰성 데이터 방어)
            for (let i = localStorage.length - 1; i >= 0; i--) {
                const key = localStorage.key(i)
                if (key && (key.startsWith('@IT') && key.includes('store'))) {
                    localStorage.removeItem(key)
                }
            }
        } catch (e) {
            //스토리지 접근 불가(프라이빗 모드 등) 시 무시
        }

        //초기 설정
        initSettings()
    }

    /**
     * 느슨한 로그아웃
     *
     * 회원탈퇴/계정전환 시 API 토큰 만료 및 스토리지 로그인 정보 삭제 및 로그인 상태를 변경한다.
     * API 토큰 만료는 성공 여부와 상관없이 나머지 로그아웃 과정을 진행한다.
     */
    const looseLogout = async () => {
        //API: 사용자 로그아웃
        await api.userLogout()

        //스토리지 로그인 정보 삭제
        encryptStorage.removeItem('store')

        //초기 설정
        initSettings()
    }

    /**
     * 로그인 유효시간 연장
     *
     * 스토리지 로그인 정보 삭제 후 새로 발급 받은 토큰을 저장한다.
     */
    const loginTmXts = (results) => {
        //스토리지 로그인 정보 저장
        setWithExpiry('LXTS', 'store', results, import.meta.env.VITE_LOGIN_VALIDITY_TIME)
    }

    /**
     * 아이디 저장 정보 저장
     *
     * 로그인 '아이디 저장' 선택시 스토리지에 해당 값을 저장한다.
     * @param {*} value
     */
    const setRecycle = (value) => {
        const item = {
            value: value
        }

        encryptStorage.setItem('recycle', JSON.stringify(item))
    }

    /**
     * 닉네임 저장 정보 획득
     *
     * 닉네임 저장 스토리지 정보를 반환한다.
     */
    const getUserName = () => {
        const store = encryptStorage.getItem('store')

        if (!store || !store.userName) {
            return null
        }

        return store.userName
    }

    /**
     * 회원 번호 저장 정보 획득
     *
     * 회원 번호 저장 스토리지 정보를 반환한다.
     */
    const getUserIdx = () => {
        const store = encryptStorage.getItem('store')

        if (!store || !store.userIdx) {
            return null
        }

        return store.userIdx
    }

     /**
     * 권한 저장 정보 획득
     *
     * 권한한 저장 스토리지 정보를 반환한다.
     */
     const getUserRole = () => {
        const store = encryptStorage.getItem('store')

        if (!store || !store.userRole) {
            return null
        }

        return store.userRole
    }

    /**
     * 프로필 이미지 저장 정보 획득
     *
     * 스토리지에 저장된 사용자 프로필 이미지 URL 을 반환한다.
     */
    const getUserProfileImage = () => {
        const store = encryptStorage.getItem('store')

        if (!store || !store.userProfileImage) {
            return null
        }

        return store.userProfileImage
    }

    /**
     * 프로필 이미지 갱신
     *
     * 이미지 변경 시 스토리지 값을 동기화해 헤더 등에 반영되도록 한다.
     */
    const setUserProfileImage = (url) => {
        const store = encryptStorage.getItem('store')

        if (!store) {
            return
        }

        store.userProfileImage = url
        encryptStorage.setItem('store', JSON.stringify(store))

        //반응형 값 갱신 → 헤더 등에 즉시 반영
        profileImage.value = url || ''
    }

    /**
     * 아이디 저장 정보 삭제
     *
     * 로그인 '아이디 저장' 해제시 스토리지에 해당 값을 삭제한다.
     */
    const removeRecycle = () => {
        encryptStorage.removeItem('recycle')
    }
    

    /**
     * 초기 설정
     *
     * 로그인 여부 및 사용자 정보를 현행화한다.
     */
    const initSettings = () => {
        //로그인 여부
        isLogin.value = checkLogin()

        //프로필 이미지 반응형 값을 스토리지와 동기화
        if (isLogin.value) {
            const store = encryptStorage.getItem('store')
            profileImage.value = (store && store.userProfileImage) ? store.userProfileImage : ''
        } else {
            profileImage.value = ''
        }
    }


    return {
        isLogin,
        profileImage,
        getWithExpiry,
        checkLogin,
        login,
        logout,
        looseLogout,
        loginTmXts,
        setRecycle,
        getUserName,
        getUserIdx,
        getUserRole,
        getUserProfileImage,
        setUserProfileImage,
        removeRecycle,
        initSettings,

    }
})

export default useAuthStore

<script setup>
const backend = import.meta.env.VITE_API_BASE_URL

const loginWithKakao = () => {
    window.location.href = `${backend}/oauth2/authorization/kakao`
}

const loginWithGoogle = () => {
    window.location.href = `${backend}/oauth2/authorization/google`
}

</script>

<template>
    <div class="signup_hr">
        <p class="signup_hr_text">간편 로그인</p>
    </div>
    <div class="social_signup_icon_list_wrapper">
        <ul class="social_signup_icon_list">
            <li>
                <div class="social_signup_icon_wrapper">
                    <a class="social_signup_icon_btn kakao" @click="loginWithKakao">
                        <img src="@/assets/images/kakao.svg" class="social_signup_icon_image">
                    </a>
                </div>
            </li>
            <li>
                <div class="social_signup_icon_wrapper">
                    <a class="social_signup_icon_btn google" @click="loginWithGoogle">
                        <svg xmlns="http://www.w3.org/2000/svg" width="30px" height="30px" viewBox="0 0 18 18">
                            <path fill="#4285F4"
                                d="M17.785 9.169c0-.738-.06-1.276-.189-1.834h-8.42v3.328h4.942c-.1.828-.638 2.073-1.834 2.91l-.016.112 2.662 2.063.185.018c1.694-1.565 2.67-3.867 2.67-6.597z">
                            </path>
                            <path fill="#34A853"
                                d="M9.175 17.938c2.422 0 4.455-.797 5.94-2.172l-2.83-2.193c-.758.528-1.774.897-3.11.897-2.372 0-4.385-1.564-5.102-3.727l-.105.01-2.769 2.142-.036.1c1.475 2.93 4.504 4.943 8.012 4.943z">
                            </path>
                            <path fill="#FBBC05"
                                d="M4.073 10.743c-.19-.558-.3-1.156-.3-1.774 0-.618.11-1.216.29-1.774l-.005-.119L1.254 4.9l-.091.044C.555 6.159.206 7.524.206 8.969c0 1.445.349 2.81.957 4.026l2.91-2.252z">
                            </path>
                            <path fill="#EB4335"
                                d="M9.175 3.468c1.684 0 2.82.728 3.468 1.335l2.531-2.471C13.62.887 11.598 0 9.175 0 5.667 0 2.638 2.013 1.163 4.943l2.9 2.252c.727-2.162 2.74-3.727 5.112-3.727z">
                            </path>
                        </svg>
                    </a>
                </div>
            </li>
        </ul>
    </div>
</template>
<style scoped>
.signup_hr {
    width: 100%;
    display: flex;
    gap: 0.375rem;
    align-items: center;
    justify-content: center;
    flex-direction: row;
    margin-top: 30px;
}

.signup_hr:before {
    flex: 1 1 100%;
    content: "";
    display: block;
    height: 1px;
    background-color: rgb(233, 236, 239);
}

.signup_hr:after {
    flex: 1 1 100%;
    content: "";
    display: block;
    height: 1px;
    background-color: rgb(233, 236, 239);
}


.signup_hr_text {
    text-align: center;
    flex: 0 0 80px;
    color: rgb(173, 181, 189);
    font-size: 0.75rem;
    text-decoration: none;
    line-height: 1.5;
    text-underline-position: under;
}

.social_signup_icon_list_wrapper {
    width: 100%;
    display: flex;
    gap: 1.5rem;
    align-items: center;
    flex-direction: column;
}

.social_signup_icon_list {
    display: flex;
    gap: 0.5rem;
    margin: 20px 0px;
}

.social_signup_icon_wrapper {
    position: relative;
    margin: 0px 10px;
}


.social_signup_icon_btn {
    cursor: pointer;
    appearance: none;
    font-size: 1rem;
    text-align: left;
    text-decoration: none;
    box-sizing: border-box;
    position: relative;
    border-radius: 0.5rem;
    padding: 0px;
    line-height: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 3.75rem;
    min-height: 3.75rem;
    width: 3.75rem;
    min-width: 3.75rem;
    border: 0.0625rem solid transparent;
    color: rgb(255, 255, 255);
    box-shadow: rgba(0, 0, 0, 0.2) 0px 1px 2px 0px;
}

.kakao {
    background-color: rgb(255, 212, 59);
}

.google {
    background-color: rgb(255, 255, 255);
}

.social_signup_icon_image {
    font-size: 30px;
    color: rgb(44, 46, 51);
    overflow: visible;
    box-sizing: content-box;
    height: 1em;
    overflow: visible;
    vertical-align: -.125em;
}
</style>

/**
 * 카테고리 정보 저장소 객체
 *
 * 카테고리 정보를 저장소에 저장한다.
 */
import { reactive } from 'vue'
import { defineStore } from 'pinia'

//카테고리 정보 객체
const useCategoryStore = defineStore('category', () => {
    //카테고리 정보
    const category = reactive([])

    /**
     * 카테고리 정보 설정
     *
     * 카테고리 정보를 설정한다.
     */
    const setCategory = (category) => {
        //카테고리 정보 설정
        category = category
    }

    return {
        category,
        setCategory
    }
})

export default useCategoryStore

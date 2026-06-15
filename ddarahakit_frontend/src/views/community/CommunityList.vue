<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import api from '@/api/community'
import { useRoute, useRouter } from 'vue-router'
import useAuthStore from '@/stores/useAuthStore'


//라우트 정보 객체
const route = useRoute();

//라우터 정보 객체
const router = useRouter()

const authStore = useAuthStore()

//게시판 카테고리 목록
const boardCategories = [
  { key: null, name: '전체글', icon: 'fa-solid fa-house' },
  { key: 'QUESTION', name: '질문/답변', icon: 'fa-solid fa-circle-question' },
  { key: 'FREE', name: '자유주제', icon: 'fa-solid fa-comments' },
  { key: 'NOTICE', name: '공지사항', icon: 'fa-solid fa-bullhorn' },
]

//현재 선택된 카테고리
const activeCategory = ref(route.query.postType || null)

//인기 게시글 랭킹 (명예의 전당)
const rankingList = ref([])
const rankingLoading = ref(true)

/**
 * 인기 게시글 랭킹 조회 (인기도: 스크랩+댓글+조회 상위)
 */
const getRanking = async () => {
  rankingLoading.value = true
  const data = await api.getRanking(5)
  if (data && data.success && Array.isArray(data.results)) {
    rankingList.value = data.results
  } else {
    rankingList.value = []
  }
  rankingLoading.value = false
}

/**
 * 글쓰기 버튼 클릭
 */
const goToWrite = () => {
  if (!authStore.checkLogin()) {
    alert('로그인 후 이용할 수 있습니다.')
    router.push({ name: 'login', query: { redirect: '/community/reg' } })
    return
  }
  router.push({ name: 'communityReg' })
}

// 로딩 상태
const isLoading = ref(true)

//검색 정보 객체
const searchInput = reactive({
  keyword: route.query.keyword || '', //검색 내용(제목, 내용 포함)
})

//질문 목록 리스트 정보 객체
const postPage = ref({
  page: 0,
  size: 0,
  hasNext: false,
  hasPrev: false,
  totalPages: 0,
  totalPosts: 0,
  posts: [],
})

const pageInfo = reactive({
  page: Number(route.query.page) || 1,
  size: Number(route.query.size) || 5,
  courseIdx: route.params.courseIdx,
  keyword: route.query.keyword,
  postType: route.query.postType || null
})

/**
 * 질문 목록 조회
 */
const getPostList = async () => {
  isLoading.value = true

  //API: 질문 목록 조회
  const data = await api.postList(pageInfo)

  if (data.success) {
    //질문 목록 추가
    if (data.results) {
      //조회 결과
      postPage.value = data.results
    }
  } else {
    //코스 목록 초기화
    postPage.value.posts.splice(0)
  }
  isLoading.value = false
}

/**
 * 페이지 이동
 */
const goToPage = (page) => {
  if (page < 1 || page > postPage.value.totalPages) return
  pageInfo.page = page

  // URL 쿼리 업데이트
  const query = { ...route.query, page: String(page) }
  router.replace({ query })

  getPostList()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 검색 실행
 *
 * 검색어로 게시글 목록을 조회한다(제목/내용). 백엔드 GET /community/list?keyword= 연동.
 * 첫 페이지로 초기화하고 URL 쿼리도 갱신해 새로고침/공유 시에도 유지되게 한다.
 */
const onSearch = () => {
  const keyword = (searchInput.keyword || '').trim()
  pageInfo.keyword = keyword
  pageInfo.page = 1

  // URL 쿼리 갱신 (postType 등 기존 조건은 유지)
  const query = { ...route.query, page: '1' }
  if (keyword) {
    query.keyword = keyword
  } else {
    delete query.keyword
  }
  router.replace({ query })

  getPostList()
}

/**
 * 검색 초기화
 */
const clearSearch = () => {
  searchInput.keyword = ''
  onSearch()
}


/**
 * 카테고리 선택
 */
const selectCategory = (key) => {
  activeCategory.value = key
  pageInfo.postType = key
  pageInfo.page = 1

  // URL 쿼리 업데이트
  const query = { ...route.query, page: '1' }
  if (key) {
    query.postType = key
  } else {
    delete query.postType
  }
  router.replace({ query })

  getPostList()
}

//현재 카테고리명
const activeCategoryName = computed(() => {
  const found = boardCategories.find(c => c.key === activeCategory.value)
  return found ? found.name : '전체글'
})

/**
 * 컴포넌트 마운트
 *
 */
onMounted(() => {
  document.title = "질문 게시판 | 따라학IT";
  getPostList()
  getRanking()
});
</script>

<template>
  <main class="pt-24 max-w-7xl mx-auto px-6 py-12 flex flex-col lg:flex-row gap-8">
    <!-- 왼쪽 사이드바 (게시판 카테고리) -->
    <aside class="w-full lg:w-64 flex-shrink-0">
      <div class="sticky top-28 space-y-6">
        <button @click="goToWrite"
          class="w-full py-4 bg-brand text-white rounded-2xl font-bold shadow-lg shadow-blue-100 flex items-center justify-center gap-2 hover:opacity-90 transition-all">
          <i class="fa-solid fa-pen-to-square"></i> 글쓰기
        </button>

        <div class="bg-white rounded-2xl border border-gray-100 p-2 shadow-sm">
          <ul class="space-y-1">
            <li v-for="cat in boardCategories" :key="cat.key">
              <button @click="selectCategory(cat.key)"
                class="w-full flex items-center gap-3 px-4 py-3 rounded-xl font-bold transition-all text-left"
                :class="activeCategory === cat.key ? 'bg-blue-50 text-brand' : 'text-gray-600 hover:bg-gray-50'">
                <i :class="cat.icon"></i> {{ cat.name }}
              </button>
            </li>
          </ul>
        </div>
      </div>
    </aside>

    <!-- 중앙 게시글 목록 -->
    <section class="flex-grow">
      <!-- 검색 영역 -->
      <div class="mb-6">
        <form @submit.prevent="onSearch" class="relative">
          <i class="fa-solid fa-magnifying-glass absolute left-5 top-1/2 -translate-y-1/2 text-gray-400 text-sm"></i>
          <input v-model="searchInput.keyword" type="search" placeholder="제목·내용으로 검색해보세요"
            class="w-full pl-12 pr-24 py-4 bg-white border border-gray-100 rounded-2xl shadow-sm text-sm text-gray-700 placeholder-gray-400 focus:border-brand focus:outline-none transition-all" />
          <button type="submit"
            class="absolute right-2 top-1/2 -translate-y-1/2 px-5 py-2.5 bg-brand text-white rounded-xl text-sm font-bold hover:opacity-90 transition-all">
            검색
          </button>
        </form>
        <!-- 검색 결과 안내 -->
        <div v-if="pageInfo.keyword" class="mt-3 flex items-center gap-3 px-1 text-sm text-gray-500">
          <span><b class="text-gray-800">'{{ pageInfo.keyword }}'</b> 검색 결과 {{ postPage.totalPosts }}건</span>
          <button @click="clearSearch" class="flex items-center gap-1 text-gray-400 hover:text-brand transition-colors">
            <i class="fa-solid fa-xmark"></i> 검색 초기화
          </button>
        </div>
      </div>

      <!-- 게시글 리스트 -->
      <div class="space-y-4">
        <!-- 스켈레톤 UI -->
        <template v-if="isLoading">
          <div v-for="n in 5" :key="n" class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
            <div class="flex items-center gap-2 mb-3">
              <div class="w-16 h-5 bg-gray-100 rounded skeleton"></div>
              <div class="w-3/4 h-6 bg-gray-100 rounded skeleton"></div>
            </div>
            <div class="space-y-2 mb-4">
              <div class="w-full h-4 bg-gray-100 rounded skeleton"></div>
              <div class="w-2/3 h-4 bg-gray-100 rounded skeleton"></div>
            </div>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-4">
                <div class="w-16 h-3 bg-gray-100 rounded skeleton"></div>
                <div class="w-20 h-3 bg-gray-100 rounded skeleton"></div>
                <div class="w-12 h-3 bg-gray-100 rounded skeleton"></div>
              </div>
              <div class="flex items-center gap-3">
                <div class="w-8 h-4 bg-gray-100 rounded skeleton"></div>
                <div class="w-8 h-4 bg-gray-100 rounded skeleton"></div>
              </div>
            </div>
          </div>
        </template>

        <!-- 빈 상태: 검색 결과 없음 -->
        <div v-else-if="postPage.posts.length === 0 && pageInfo.keyword"
          class="bg-white p-12 rounded-2xl border border-gray-100 shadow-sm flex flex-col items-center text-center">
          <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-5">
            <i class="fa-solid fa-magnifying-glass text-brand text-2xl"></i>
          </div>
          <h3 class="text-lg font-bold text-gray-900 mb-2">
            '{{ pageInfo.keyword }}' 검색 결과가 없어요
          </h3>
          <p class="text-sm text-gray-400 mb-6">
            다른 키워드로 검색하거나 새 글을 남겨보세요.
          </p>
          <button @click="clearSearch"
            class="px-6 py-3 bg-brand text-white rounded-2xl font-bold shadow-lg shadow-blue-100 flex items-center gap-2 hover:opacity-90 transition-all">
            <i class="fa-solid fa-rotate-left"></i> 검색 초기화
          </button>
        </div>

        <!-- 빈 상태: 글 없음 -->
        <div v-else-if="postPage.posts.length === 0"
          class="bg-white p-12 rounded-2xl border border-gray-100 shadow-sm flex flex-col items-center text-center">
          <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-5">
            <i class="fa-regular fa-comment-dots text-brand text-2xl"></i>
          </div>
          <h3 class="text-lg font-bold text-gray-900 mb-2">
            {{ activeCategoryName }}에 아직 글이 없어요
          </h3>
          <p class="text-sm text-gray-400 mb-6">
            첫 번째 글을 남겨 커뮤니티를 시작해 보세요.
          </p>
          <button @click="goToWrite"
            class="px-6 py-3 bg-brand text-white rounded-2xl font-bold shadow-lg shadow-blue-100 flex items-center gap-2 hover:opacity-90 transition-all">
            <i class="fa-solid fa-pen-to-square"></i> 글쓰기
          </button>
        </div>

        <!-- 실제 게시글 -->
        <template v-else>
          <div v-for="(post, index) in postPage.posts" :key="index"
            class="bg-white p-6 rounded-2xl border border-gray-100 post-card cursor-pointer shadow-sm">
            <RouterLink :to="`/community/${post.idx}`">

              <div class="flex justify-between items-start mb-3">
                <div class="flex items-center gap-2">
                  <span class="px-2 py-1 bg-gray-100 text-gray-500 text-[10px] font-bold rounded">{{ post.postTypeDescription }}</span>
                  <h3 class="text-lg font-bold text-gray-900 leading-tight">
                    {{ post.title }}
                  </h3>
                </div>
              </div>
              <p class="text-gray-500 text-sm line-clamp-2 mb-4 leading-relaxed">
                {{ post.text }}
              </p>
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-4 text-xs text-gray-400">
                  <span class="flex items-center gap-1.5"><i class="fa-solid fa-user"></i> {{ post.userName }}</span>
                  <span class="flex items-center gap-1.5"><i class="fa-solid fa-clock"></i> {{ post.createdAt }}</span>
                </div>
                <div class="flex items-center gap-3">
                  <span class="flex items-center gap-1 text-xs text-gray-400"><i class="fa-regular fa-eye"></i> {{ post.viewCount }}</span>
                  <span class="flex items-center gap-1 text-xs font-bold"
                    :class="post.commentCount > 0 ? 'text-brand' : 'text-gray-400'"><i
                      class="fa-regular fa-comment-dots"></i> {{ post.commentCount }}</span>
                </div>
              </div>

            </RouterLink>
          </div>
        </template>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="postPage.totalPages > 1" class="mt-12 flex justify-center items-center gap-2">
        <button
          @click="goToPage(postPage.page)"
          :disabled="!postPage.hasPrev"
          class="w-10 h-10 rounded-lg border border-gray-200 flex items-center justify-center transition-all"
          :class="postPage.hasPrev ? 'hover:border-brand hover:text-brand cursor-pointer' : 'text-gray-300 cursor-not-allowed'">
          <i class="fa-solid fa-chevron-left"></i>
        </button>
        <button v-for="page in postPage.totalPages" :key="page"
          @click="goToPage(page)"
          :class="page === postPage.page + 1
            ? 'w-10 h-10 rounded-lg bg-brand text-white font-bold'
            : 'w-10 h-10 rounded-lg border border-gray-200 text-gray-500 hover:border-brand hover:text-brand transition-all cursor-pointer'">
          {{ page }}
        </button>
        <button
          @click="goToPage(postPage.page + 2)"
          :disabled="!postPage.hasNext"
          class="w-10 h-10 rounded-lg border border-gray-200 flex items-center justify-center transition-all"
          :class="postPage.hasNext ? 'hover:border-brand hover:text-brand cursor-pointer' : 'text-gray-300 cursor-not-allowed'">
          <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>
    </section>

    <!-- 오른쪽 사이드바 (인기 게시글 랭킹) -->
    <aside class="w-full lg:w-72 flex-shrink-0">
      <div class="space-y-6">
        <!-- 인기 게시글 랭킹 (명예의 전당) -->
        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <h4 class="font-bold mb-4 flex items-center gap-2 text-gray-900">
            <i class="fa-solid fa-fire text-brand"></i> 인기 게시글
          </h4>

          <!-- 로딩 -->
          <ul v-if="rankingLoading" class="space-y-4">
            <li v-for="n in 3" :key="n" class="flex items-center gap-3 animate-pulse">
              <div class="w-8 h-8 rounded-full bg-gray-100 flex-shrink-0"></div>
              <div class="flex-1 space-y-2">
                <div class="h-3 w-3/4 bg-gray-100 rounded"></div>
                <div class="h-2 w-1/2 bg-gray-100 rounded"></div>
              </div>
            </li>
          </ul>

          <!-- 빈 상태 -->
          <p v-else-if="rankingList.length === 0" class="text-sm text-gray-400 py-4 text-center">
            아직 인기 게시글이 없습니다.
          </p>

          <!-- 실제 데이터 -->
          <ul v-else class="space-y-4">
            <li v-for="(post, idx) in rankingList" :key="post.idx">
              <RouterLink :to="`/community/${post.idx}`" class="flex items-center gap-3 group">
                <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs flex-shrink-0"
                  :class="idx === 0 ? 'bg-blue-100 text-brand' : 'bg-gray-100 text-gray-400'">
                  {{ idx + 1 }}
                </div>
                <div class="min-w-0">
                  <p class="text-sm font-bold text-gray-900 line-clamp-1 group-hover:text-brand transition-colors">
                    {{ post.title }}
                  </p>
                  <p class="text-[10px] text-gray-400">
                    <i class="fa-regular fa-eye"></i> {{ post.viewCount }}
                    <i class="fa-regular fa-comment-dots ml-2"></i> {{ post.commentCount }}
                    <i class="fa-regular fa-bookmark ml-2"></i> {{ post.scrapCount }}
                  </p>
                </div>
              </RouterLink>
            </li>
          </ul>
        </div>
      </div>
    </aside>
  </main>
</template>

<style scoped>
/* 게시글 카드 hover */
.post-card {
    transition: all 0.3s ease;
}

.post-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
    border-color: #14BCED;
}

/* 스켈레톤 애니메이션 */
.skeleton {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
}

@keyframes skeleton-loading {
    0% {
        background-position: 200% 0;
    }
    100% {
        background-position: -200% 0;
    }
}
</style>
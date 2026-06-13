<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import useAuthStore from '@/stores/useAuthStore'
import QuillEditor from '@/components/base/QuillEditor.vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/community'
import { userImageUrl } from '@/utils/image'

const backend = import.meta.env.VITE_IMG_BASE_URL

//라우트 정보 객체
const route = useRoute()
const router = useRouter()
const postIdx = route.params.postIdx

//저장소 정보 객체
const authStore = useAuthStore()
const isScrapped = ref(false)
const isScrapLoading = ref(false)

//관련(인기) 게시글
const relatedPosts = ref([])
const relatedLoading = ref(true)

/**
 * 관련 게시글 조회 (같은 코스/타입 기준 최신)
 */
const getRelatedPosts = async () => {
  relatedLoading.value = true
  const data = await api.getRelated(postIdx, 5)
  if (data && data.success && Array.isArray(data.results)) {
    // 현재 글 제외
    relatedPosts.value = data.results.filter(p => Number(p.idx) !== Number(postIdx))
  } else {
    relatedPosts.value = []
  }
  relatedLoading.value = false
}

const post = ref({
  idx: 0,
  title: '',
  text: '',
  content: '',
  userName: '',
  userIdx: 0,
  userProfileImageUrl: '',
  courseName: '',
  lectureName: '',
  comments: [],
  createdAt: '',
  updatedAt: ''
})

// 로딩 상태
const isLoading = ref(true)

/**
 * 답변 내용 유효성 룰
 *
 * 답변 내용 유효성 룰을 정의한다.
 */
const commentInputError = reactive({
  text: {
    isValid: false,
    errorMessage: null
  },
})

// 에디터 v-model용 객체
const commentEditorContent = reactive({
  text: '',
  content: ''
})

// 에디터 에러 상태
const commentEditorError = ref(false)

//답변 정보 객체
const commentInput = reactive({
  text: '', //답변 내용(평문)
  content: '', //답변 내용(에디터)
  postIdx: postIdx, //관련 질문 IDX
})

/**
 * 컴포넌트 마운트
 *
 */
onMounted(async () => {
  await getPostDetail();
  await syncScrapState();
  getRelatedPosts();
  document.title = `${post.value.title} | 따라학IT`;
});

/**
 * 질문 데이터 가져오기
 */
const getPostDetail = async () => {
  isLoading.value = true
  const data = await api.getPostDetail(postIdx);

  if (data.success) {
    if (data.results) {
      post.value = data.results;
    }
  }
  isLoading.value = false
};

const extractScrapPostIds = (results) => {
  if (!results) return []
  if (Array.isArray(results)) return results.map(item => Number(item.postIdx ?? item.idx))
  if (Array.isArray(results.list)) return results.list.map(item => Number(item.postIdx ?? item.idx))
  if (Array.isArray(results.posts)) return results.posts.map(item => Number(item.postIdx ?? item.idx))
  return []
}

const syncScrapState = async () => {
  if (!authStore.isLogin) {
    isScrapped.value = false
    return
  }

  const data = await api.scrapList()
  if (data?.success) {
    const postIds = extractScrapPostIds(data.results)
    isScrapped.value = postIds.includes(Number(postIdx))
  }
}

const toggleScrap = async () => {
  if (!authStore.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (isScrapLoading.value) return

  isScrapLoading.value = true
  const data = await api.scrapToggle(postIdx)
  if (data?.success) {
    if (typeof data.results?.scrapped === 'boolean') {
      isScrapped.value = data.results.scrapped
    } else {
      isScrapped.value = !isScrapped.value
    }
  }
  isScrapLoading.value = false
}

/**
 * 에디터 내용 변경 핸들러
 */
const onCommentEditorChange = (value) => {
  commentEditorContent.text = value.text
  commentEditorContent.content = value.content
  commentInput.text = value.text
  commentInput.content = value.content
  // 입력 시 바로 유효성 검사
  commentEditorRules()
}

/**
 * 답변 내용 유효성 룰
 *
 * 답변 내용 유효성 룰을 정의한다.
 */
const commentEditorRules = () => {
  if (commentInput.text !== '' && commentInput.text !== '\n') {
    commentEditorError.value = false
    commentInputError.text.errorMessage = null
    commentInputError.text.isValid = true
  } else {
    commentEditorError.value = true
    commentInputError.text.errorMessage = '답변 내용을 입력해주세요.'
    commentInputError.text.isValid = false
  }
}


/**
 * 폼 유효성 검사
 */
const isFormValid = computed(() =>
  commentInputError.text.isValid
);

/**
 * 폼 서브밋
 *
 * 화면 새로고침
 */
const submitForm = async () => {
  //API: 답변 등록
  const data = await api.commentCreate(commentInput)
  if (data.success) {
    window.location.reload();
  }
}

</script>

<template>
  <!-- 스켈레톤 UI -->
  <main v-if="isLoading" class="pt-24 max-w-7xl mx-auto px-6 py-12 flex flex-col lg:flex-row gap-8">
    <section class="flex-grow max-w-4xl">
      <!-- 브레드크럼 스켈레톤 -->
      <div class="mb-6 flex items-center gap-2">
        <div class="w-32 h-4 bg-gray-100 rounded skeleton"></div>
        <div class="w-4 h-4 bg-gray-100 rounded skeleton"></div>
        <div class="w-20 h-4 bg-gray-100 rounded skeleton"></div>
      </div>

      <!-- 메인 게시글 카드 스켈레톤 -->
      <article class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden mb-8">
        <div class="p-8 md:p-10">
          <!-- 헤더 스켈레톤 -->
          <header class="mb-8 border-b border-gray-50 pb-8">
            <div class="flex items-center gap-2 mb-4">
              <div class="w-24 h-6 bg-gray-100 rounded-full skeleton"></div>
              <div class="w-20 h-6 bg-gray-100 rounded-full skeleton"></div>
            </div>
            <div class="w-full h-8 bg-gray-100 rounded skeleton mb-2"></div>
            <div class="w-3/4 h-8 bg-gray-100 rounded skeleton mb-6"></div>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-gray-100 skeleton"></div>
                <div>
                  <div class="w-20 h-4 bg-gray-100 rounded skeleton mb-2"></div>
                  <div class="w-32 h-3 bg-gray-100 rounded skeleton"></div>
                </div>
              </div>
              <div class="flex gap-2">
                <div class="w-20 h-8 bg-gray-100 rounded-xl skeleton"></div>
              </div>
            </div>
          </header>
          <!-- 본문 스켈레톤 -->
          <div class="space-y-4">
            <div class="w-full h-5 bg-gray-100 rounded skeleton"></div>
            <div class="w-full h-5 bg-gray-100 rounded skeleton"></div>
            <div class="w-5/6 h-5 bg-gray-100 rounded skeleton"></div>
            <div class="w-full h-5 bg-gray-100 rounded skeleton"></div>
            <div class="w-2/3 h-5 bg-gray-100 rounded skeleton"></div>
          </div>
        </div>
      </article>

      <!-- 답변 섹션 스켈레톤 -->
      <section class="space-y-6">
        <div class="flex items-center justify-between mb-4">
          <div class="w-24 h-6 bg-gray-100 rounded skeleton"></div>
        </div>
        <div v-for="n in 2" :key="n" class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-9 h-9 rounded-full bg-gray-100 skeleton"></div>
            <div>
              <div class="w-20 h-4 bg-gray-100 rounded skeleton mb-2"></div>
              <div class="w-16 h-3 bg-gray-100 rounded skeleton"></div>
            </div>
          </div>
          <div class="space-y-2">
            <div class="w-full h-4 bg-gray-100 rounded skeleton"></div>
            <div class="w-full h-4 bg-gray-100 rounded skeleton"></div>
            <div class="w-3/4 h-4 bg-gray-100 rounded skeleton"></div>
          </div>
        </div>
      </section>
    </section>

    <!-- 오른쪽 사이드바 스켈레톤 -->
    <aside class="w-full lg:w-72 flex-shrink-0">
      <div class="sticky top-28 space-y-6">
        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm text-center">
          <div class="w-20 h-20 rounded-full bg-gray-100 mx-auto mb-4 skeleton"></div>
          <div class="w-24 h-5 bg-gray-100 rounded skeleton mx-auto mb-2"></div>
          <div class="w-16 h-3 bg-gray-100 rounded skeleton mx-auto mb-4"></div>
          <div class="w-full h-10 bg-gray-100 rounded-xl skeleton"></div>
        </div>
        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div class="p-4 bg-gray-50 border-b border-gray-100">
            <div class="w-20 h-4 bg-gray-200 rounded skeleton"></div>
          </div>
          <div class="p-4">
            <div class="w-32 h-4 bg-gray-100 rounded skeleton mb-2"></div>
            <div class="w-24 h-3 bg-gray-100 rounded skeleton"></div>
          </div>
        </div>
      </div>
    </aside>
  </main>

  <!-- 실제 콘텐츠 -->
  <main v-else class="pt-24 max-w-7xl mx-auto px-6 py-12 flex flex-col lg:flex-row gap-8">
    <!-- 왼쪽 게시글 상세 내용 -->
    <section class="flex-grow max-w-4xl">
      <!-- 브레드크럼 및 뒤로가기 -->
      <div class="mb-6 flex items-center gap-2 text-sm text-gray-400">
        <RouterLink :to="{ name: 'communityList' }" class="hover:text-brand transition-all flex items-center gap-1 cursor-pointer">
          <i class="fa-solid fa-arrow-left"></i> 목록으로 돌아가기
        </RouterLink>
        <span class="mx-1">/</span>
        <span class="text-gray-600 font-medium">질문/답변</span>
      </div>

      <!-- 메인 게시글 카드 -->
      <article class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden mb-8">
        <div class="p-8 md:p-10">
          <!-- 헤더 -->
          <header class="mb-8 border-b border-gray-50 pb-8">
            <div class="flex items-center gap-2 mb-4">
              <span class="px-3 py-1 bg-blue-50 text-brand text-xs font-bold rounded-full border border-blue-100">
                {{ post.courseName }}
              </span>
              <span class="px-3 py-1 bg-gray-100 text-gray-500 text-xs font-medium rounded-full">
                {{ post.lectureName }}
              </span>
            </div>
            <h1 class="text-2xl md:text-3xl font-bold text-gray-900 leading-snug mb-6">
              {{ post.title }}
            </h1>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-blue-400 to-cyan-300 overflow-hidden">
                  <img v-if="post.userProfileImageUrl" :src="userImageUrl(post.userProfileImageUrl)" alt="" class="w-full h-full object-cover">
                </div>
                <div>
                  <p class="text-sm font-bold text-gray-900">{{ post.userName }}</p>
                  <p class="text-[11px] text-gray-400">
                    <i class="fa-solid fa-clock mr-1"></i>{{ post.createdAt }}
                    <span class="mx-1">·</span>
                    <i class="fa-regular fa-comment mr-1"></i>답변 {{ post.commentCount }}
                  </p>
                </div>
              </div>
              <div class="flex gap-2">
                <button @click="toggleScrap" :disabled="isScrapLoading"
                        class="px-4 py-2 text-sm font-medium rounded-xl transition-all border"
                        :class="isScrapped
                          ? 'text-brand border-blue-100 bg-blue-50'
                          : 'text-gray-500 border-gray-100 hover:bg-gray-50'">
                  <i :class="[isScrapped ? 'fa-solid' : 'fa-regular', 'fa-bookmark', 'mr-1']"></i>
                  {{ isScrapped ? '스크랩됨' : '스크랩' }}
                </button>
                <button class="p-2 text-gray-400 hover:text-gray-600">
                  <i class="fa-solid fa-ellipsis-vertical"></i>
                </button>
              </div>
            </div>
          </header>

          <!-- 본문 -->
          <div class="content-area text-gray-700 leading-relaxed text-base md:text-lg">
            <QuillEditor
              v-if="post.content"
              :read-only="true"
              :initial-content="post.content"
            />
          </div>

          <!-- 추천 버튼 -->
          <div class="mt-10 pt-8 border-t border-gray-50 flex justify-center">
            <button class="flex items-center gap-2 px-8 py-3 rounded-full border border-gray-200 text-gray-500 hover:border-brand hover:text-brand transition-all font-bold">
              <i class="fa-regular fa-thumbs-up text-xl"></i>
              추천
            </button>
          </div>
        </div>
      </article>

      <!-- 댓글/답변 섹션 -->
      <section class="space-y-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-gray-900 flex items-center gap-2">
            답변 <span class="text-brand">{{ post.commentCount }}</span>
          </h3>
          <div class="flex gap-4 text-sm text-gray-400">
            <button class="text-gray-900 font-bold">최신순</button>
            <button>추천순</button>
          </div>
        </div>

        <!-- 답변 작성 폼 -->
        <div v-if="authStore.isLogin && (authStore.getUserIdx() == post.userIdx || authStore.getUserRole() == 'ROLE_ADMIN')"
             class="bg-white rounded-2xl border border-gray-100 shadow-sm mb-8 overflow-hidden">
          <form @submit.prevent="submitForm">
            <div class="comment-editor-wrapper">
              <QuillEditor
                v-model="commentEditorContent"
                :enable-image-upload="true"
                :image-base-url="backend"
                placeholder="지식 공유를 통해 함께 성장해봐요! 정중한 답변 부탁드립니다."
                min-height="8rem"
                :error="commentEditorError"
                @blur="commentEditorRules"
                @text-change="onCommentEditorChange"
              />
            </div>
            <p v-if="commentInputError.text.errorMessage" class="text-red-500 text-sm mt-2 px-6">
              {{ commentInputError.text.errorMessage }}
            </p>
            <div class="flex justify-end items-center gap-4 p-4 border-t border-gray-50">
              <button type="submit"
                      :disabled="!isFormValid"
                      class="px-6 py-2.5 bg-brand text-white rounded-xl text-sm font-bold shadow-lg shadow-blue-100 disabled:opacity-40 disabled:cursor-not-allowed hover:opacity-90 transition-all">
                답변 등록
              </button>
            </div>
          </form>
        </div>

        <!-- 로그인 안내 -->
        <div v-else-if="!authStore.isLogin" class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm mb-8">
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-500">답변을 작성하려면 로그인이 필요합니다.</span>
            <RouterLink to="/user/login" class="px-6 py-2.5 bg-brand text-white rounded-xl text-sm font-bold">
              로그인
            </RouterLink>
          </div>
        </div>

        <!-- 답변 리스트 -->
        <div class="space-y-4">
          <div v-for="(comment, index) in post.comments" :key="comment.idx"
               class="bg-white p-6 rounded-2xl shadow-sm"
               :class="index === 0 ? 'border-2 border-blue-100 relative' : 'border border-gray-100'">
            <!-- 베스트 답변 뱃지 (첫 번째 답변) -->
            <div v-if="index === 0 && post.commentCount > 1"
                 class="absolute -top-3 left-6 px-3 py-1 bg-brand text-white text-[10px] font-bold rounded-full shadow-md">
              BEST 답변
            </div>

            <div class="flex justify-between items-start mb-4">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500 text-xs overflow-hidden">
                  <img v-if="comment.userProfileImageUrl" :src="userImageUrl(comment.userProfileImageUrl)" alt="" class="w-full h-full object-cover">
                  <span v-else>{{ comment.userName?.charAt(0) }}</span>
                </div>
                <div>
                  <p class="text-sm font-bold text-gray-900">
                    {{ comment.userName }}
                    <i v-if="comment.userIdx === post.userIdx" class="fa-solid fa-circle-check text-brand ml-1" title="질문자"></i>
                  </p>
                  <p class="text-[11px] text-gray-400">{{ comment.createdAt }}</p>
                </div>
              </div>
              <button class="text-gray-300 hover:text-gray-500"><i class="fa-solid fa-ellipsis"></i></button>
            </div>

            <div class="text-gray-600 text-[15px] leading-relaxed mb-4">
              <QuillEditor
                :read-only="true"
                :initial-content="comment.content"
              />
            </div>

            <div class="flex items-center gap-4 text-xs font-bold">
              <button class="text-gray-400 flex items-center gap-1.5 hover:text-brand transition-all">
                <i class="fa-regular fa-thumbs-up"></i> 도움이 됐어요
              </button>
            </div>
          </div>

          <!-- 답변이 없을 때 -->
          <div v-if="post.commentCount === 0" class="bg-white p-12 rounded-2xl border border-gray-100 shadow-sm text-center">
            <i class="fa-regular fa-comment-dots text-4xl text-gray-300 mb-4"></i>
            <p class="text-gray-400">아직 답변이 없습니다.</p>
            <p class="text-sm text-gray-400 mt-1">첫 번째 답변을 남겨보세요!</p>
          </div>
        </div>
      </section>
    </section>

    <!-- 오른쪽 사이드바 -->
    <aside class="w-full lg:w-72 flex-shrink-0">
      <div class="sticky top-28 space-y-6">
        <!-- 작성자 정보 -->
        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm text-center">
          <div class="w-20 h-20 rounded-full bg-gradient-to-tr from-blue-400 to-cyan-300 mx-auto mb-4 overflow-hidden">
            <img v-if="post.userProfileImageUrl" :src="userImageUrl(post.userProfileImageUrl)" alt="" class="w-full h-full object-cover">
          </div>
          <h4 class="font-bold text-gray-900">{{ post.userName }}</h4>
          <p class="text-xs text-gray-400 mb-4">질문자</p>
          <button class="w-full py-2.5 border border-brand text-brand text-sm font-bold rounded-xl hover:bg-blue-50 transition-all">
            프로필 보기
          </button>
        </div>

        <!-- 관련 강의 -->
        <div v-if="post.courseName || post.lectureName" class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div class="p-4 bg-gray-50 border-b border-gray-100 font-bold text-sm text-gray-900">관련 강의</div>
          <div class="p-4">
            <div v-if="post.courseName" class="text-sm font-bold text-gray-900 mb-1">{{ post.courseName }}</div>
            <div v-if="post.lectureName" class="text-xs text-gray-500">{{ post.lectureName }}</div>
          </div>
        </div>

        <!-- 관련 게시글 -->
        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <h4 class="font-bold mb-4 flex items-center gap-2 text-gray-900">
            <i class="fa-solid fa-fire text-orange-400"></i> 관련 게시글
          </h4>

          <!-- 로딩 -->
          <ul v-if="relatedLoading" class="space-y-3">
            <li v-for="n in 3" :key="n" class="h-3 bg-gray-100 rounded animate-pulse"></li>
          </ul>

          <!-- 빈 상태 -->
          <p v-else-if="relatedPosts.length === 0" class="text-xs text-gray-400 py-2 text-center">
            관련 게시글이 없습니다.
          </p>

          <!-- 실제 데이터 -->
          <ul v-else class="space-y-4">
            <li v-for="rel in relatedPosts" :key="rel.idx">
              <RouterLink :to="`/community/${rel.idx}`"
                class="block text-xs text-gray-600 hover:text-brand transition-all line-clamp-1">
                {{ rel.title }}
              </RouterLink>
            </li>
          </ul>
        </div>
      </div>
    </aside>
  </main>
</template>

<style scoped>
/* 본문 콘텐츠 영역 스타일 */
.content-area :deep(pre) {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 1rem;
  overflow-x: auto;
}

.content-area :deep(code) {
  background: #f3f4f6;
  padding: 0.125rem 0.375rem;
  border-radius: 4px;
  font-size: 0.875em;
  color: #e11d48;
}

.content-area :deep(pre code) {
  background: transparent;
  padding: 0;
  color: #d4d4d4;
}

/* 텍스트 줄 수 제한 */
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Quill 읽기 전용 스타일 오버라이드 */
.content-area :deep(.ql-container.ql-snow) {
  border: none;
}

.content-area :deep(.ql-editor) {
  padding: 0;
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

/* 댓글 에디터 스타일 */
.comment-editor-wrapper :deep(.ql-toolbar.ql-snow) {
  border: none;
  border-bottom: 1px solid rgb(229, 231, 235);
  border-radius: 0;
  padding: 0.75rem 1.5rem;
  background-color: rgb(249, 250, 251);
}

.comment-editor-wrapper :deep(.ql-toolbar .ql-formats) {
  margin-right: 0.75rem;
}

.comment-editor-wrapper :deep(.ql-toolbar button) {
  color: rgb(156, 163, 175);
  transition: all 0.2s;
}

.comment-editor-wrapper :deep(.ql-toolbar button:hover) {
  color: rgb(20, 189, 238);
}

.comment-editor-wrapper :deep(.ql-toolbar button.ql-active) {
  color: rgb(20, 189, 238);
}

.comment-editor-wrapper :deep(.ql-toolbar .ql-stroke) {
  stroke: rgb(156, 163, 175);
}

.comment-editor-wrapper :deep(.ql-toolbar .ql-fill) {
  fill: rgb(156, 163, 175);
}

.comment-editor-wrapper :deep(.ql-toolbar button:hover .ql-stroke),
.comment-editor-wrapper :deep(.ql-toolbar button.ql-active .ql-stroke) {
  stroke: rgb(20, 189, 238);
}

.comment-editor-wrapper :deep(.ql-toolbar button:hover .ql-fill),
.comment-editor-wrapper :deep(.ql-toolbar button.ql-active .ql-fill) {
  fill: rgb(20, 189, 238);
}

.comment-editor-wrapper :deep(.ql-toolbar .ql-picker) {
  color: rgb(156, 163, 175);
}

.comment-editor-wrapper :deep(.ql-toolbar .ql-picker-label) {
  color: rgb(156, 163, 175);
}

.comment-editor-wrapper :deep(.ql-toolbar .ql-picker-label:hover),
.comment-editor-wrapper :deep(.ql-toolbar .ql-picker-label.ql-active) {
  color: rgb(20, 189, 238);
}

.comment-editor-wrapper :deep(.ql-toolbar .ql-picker-label .ql-stroke) {
  stroke: rgb(156, 163, 175);
}

.comment-editor-wrapper :deep(.ql-toolbar .ql-picker-label:hover .ql-stroke) {
  stroke: rgb(20, 189, 238);
}

.comment-editor-wrapper :deep(.ql-container.ql-snow) {
  border: none;
  border-radius: 0;
}

.comment-editor-wrapper :deep(.ql-editor) {
  padding: 1rem 1.5rem;
  font-size: 0.9375rem;
  line-height: 1.75;
  color: rgb(55, 65, 81);
  min-height: 8rem;
}

.comment-editor-wrapper :deep(.ql-editor.ql-blank::before) {
  color: rgb(209, 213, 219);
  font-style: normal;
  left: 1.5rem;
  right: 1.5rem;
}

/* 코드 블록 언어 선택 드롭다운 스타일 */
.comment-editor-wrapper :deep(.ql-code-block-container .ql-ui) {
  color: rgb(156, 163, 175);
}

.comment-editor-wrapper :deep(.ql-code-block-container select.ql-ui option) {
  background-color: white;
  color: rgb(55, 65, 81);
}
</style>

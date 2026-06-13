<script setup>
// 수업 진행 방식 (포트폴리오) — 수업 진행 방식·지도 노하우.

// 내가 지도한 기수별 파이널 프로젝트 결과물 (beyond-sw-camp GitHub Organization 저장소)
const portfolios = [
  { cohort: 'BE02', url: 'https://github.com/orgs/beyond-sw-camp/repositories?q=be02-fin' },
  { cohort: 'BE06', url: 'https://github.com/orgs/beyond-sw-camp/repositories?q=be06-fin' },
  { cohort: 'BE12', url: 'https://github.com/orgs/beyond-sw-camp/repositories?q=be12-fin' },
  { cohort: 'BE17', url: 'https://github.com/orgs/beyond-sw-camp/repositories?q=be17-fin' },
  { cohort: 'BE24', url: 'https://github.com/orgs/beyond-sw-camp/repositories?q=be24-fin' },
]
</script>

<template>
  <main class="max-w-6xl mx-auto px-6 pt-28 pb-20 min-h-[70vh]">
    <!-- 헤더 -->
    <section class="mb-12">
      <p class="text-brand font-semibold tracking-wide mb-2">PORTFOLIO</p>
      <h1 class="text-3xl md:text-4xl font-bold text-slate-900 tracking-tight">수업 진행 방식</h1>
      <p class="text-slate-500 mt-3 max-w-2xl">
        제가 수업을 어떻게 설계하고 이끌어가는지, 그 진행 방식과 지도 노하우를 소개합니다.
      </p>
    </section>

    <!-- 핵심 운영 방식 (세로로 차례대로) -->
    <section class="space-y-6">
      <!-- 01. 수업 자료 완비 -->
      <div class="flex flex-col md:flex-row items-start gap-6 md:gap-10 bg-white rounded-3xl border border-gray-100 p-8 md:p-12 shadow-sm">
        <div class="flex items-center gap-5 shrink-0">
          <span class="text-4xl md:text-5xl font-extrabold text-brand/20 tabular-nums">01</span>
          <div class="w-14 h-14 rounded-2xl bg-blue-50 flex items-center justify-center">
            <i class="fa-solid fa-folder-open text-brand text-2xl"></i>
          </div>
        </div>
        <div>
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 mb-3">수업 자료 완비</h2>
          <p class="text-slate-500 leading-relaxed md:text-lg max-w-3xl">
            모든 수업에 필요한 강의 자료가 미리 빠짐없이 준비되어 있습니다.
            학생들은 잘 정리된 자료를 따라 막힘없이 학습을 이어갈 수 있습니다.
          </p>
        </div>
      </div>

      <!-- 02. 학생 산출물 관리 -->
      <div class="flex flex-col md:flex-row items-start gap-6 md:gap-10 bg-white rounded-3xl border border-gray-100 p-8 md:p-12 shadow-sm">
        <div class="flex items-center gap-5 shrink-0">
          <span class="text-4xl md:text-5xl font-extrabold text-brand/20 tabular-nums">02</span>
          <div class="w-14 h-14 rounded-2xl bg-blue-50 flex items-center justify-center">
            <i class="fa-solid fa-clipboard-check text-brand text-2xl"></i>
          </div>
        </div>
        <div class="w-full">
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 mb-3">학생 산출물 관리</h2>
          <p class="text-slate-500 leading-relaxed md:text-lg max-w-3xl">
            학생 한 명 한 명의 산출물을 분야별·단계별로 수집해 체계적으로 보관·평가합니다.
            프로젝트 평가 문제 출제부터 채점, 팀 구성 평가, 발표 평가까지 모두 기록으로 남기고
            개별 피드백으로 완성도를 함께 끌어올립니다.
          </p>

          <!-- 실제 관리 문서/자료 카테고리 -->
          <div class="mt-6 grid sm:grid-cols-2 gap-4">
            <!-- 평가 · 채점 -->
            <div class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <h3 class="font-bold text-slate-800 mb-3 flex items-center gap-2">
                <i class="fa-solid fa-pen-ruler text-brand"></i> 평가 · 채점
              </h3>
              <ul class="space-y-1.5 text-sm text-slate-500 leading-relaxed list-disc pl-4 marker:text-slate-300">
                <li>DB 평가 문제 · 채점 결과표</li>
                <li>최종 프로젝트 종합 평가 문제 · 팀 구성 점수</li>
                <li>팀 편성 기준표 · 기술셋 정리</li>
              </ul>
            </div>

            <!-- 프로젝트 산출물 -->
            <div class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <h3 class="font-bold text-slate-800 mb-3 flex items-center gap-2">
                <i class="fa-solid fa-folder-tree text-brand"></i> 프로젝트 산출물
              </h3>
              <ul class="space-y-1.5 text-sm text-slate-500 leading-relaxed list-disc pl-4 marker:text-slate-300">
                <li>분야별 산출물 보관 — 기반기술 · 프론트엔드 · 백엔드 · 데브옵스</li>
                <li>팀별(5개 팀) 산출물 아카이브</li>
              </ul>
            </div>

            <!-- 발표 평가 -->
            <div class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <h3 class="font-bold text-slate-800 mb-3 flex items-center gap-2">
                <i class="fa-solid fa-chalkboard text-brand"></i> 발표 평가
              </h3>
              <ul class="space-y-1.5 text-sm text-slate-500 leading-relaxed list-disc pl-4 marker:text-slate-300">
                <li>팀별 프로젝트 발표 영상 · 발표 평가서</li>
                <li>중간 발표 자료 · 심사위원 평가서</li>
              </ul>
            </div>

            <!-- 훈련생 학습 관리 -->
            <div class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <h3 class="font-bold text-slate-800 mb-3 flex items-center gap-2">
                <i class="fa-solid fa-user-check text-brand"></i> 훈련생 학습 관리
              </h3>
              <ul class="space-y-1.5 text-sm text-slate-500 leading-relaxed list-disc pl-4 marker:text-slate-300">
                <li>사전학습(프리코스) 운영 · 인증</li>
                <li>실시간 수업자료 공유 · 추가 학습 콘텐츠(인프런) 제공</li>
                <li>블로그 작성 · 출결 학습 모니터링</li>
                <li>월별 코딩테스트 안내 · 결과</li>
                <li>만족도 조사 · 환류 회의록</li>
                <li>1:1 상담 일지</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- 03. 배포부터 운영까지 -->
      <div class="flex flex-col md:flex-row items-start gap-6 md:gap-10 bg-white rounded-3xl border border-gray-100 p-8 md:p-12 shadow-sm">
        <div class="flex items-center gap-5 shrink-0">
          <span class="text-4xl md:text-5xl font-extrabold text-brand/20 tabular-nums">03</span>
          <div class="w-14 h-14 rounded-2xl bg-blue-50 flex items-center justify-center">
            <i class="fa-solid fa-gauge-high text-brand text-2xl"></i>
          </div>
        </div>
        <div>
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 mb-3">배포부터 운영까지</h2>
          <p class="text-slate-500 leading-relaxed md:text-lg max-w-3xl">
            프로젝트에서는 학생들이 직접 만든 서비스를 배포해 올리고, 모니터링 환경에서 운영해보게 합니다.
            개발에서 끝나지 않고 실제 서비스를 운영하는 경험까지 제공합니다.
          </p>
        </div>
      </div>

      <!-- 04. 학생들의 포트폴리오 -->
      <div class="flex flex-col md:flex-row items-start gap-6 md:gap-10 bg-white rounded-3xl border border-gray-100 p-8 md:p-12 shadow-sm">
        <div class="flex items-center gap-5 shrink-0">
          <span class="text-4xl md:text-5xl font-extrabold text-brand/20 tabular-nums">04</span>
          <div class="w-14 h-14 rounded-2xl bg-blue-50 flex items-center justify-center">
            <i class="fa-brands fa-github text-brand text-2xl"></i>
          </div>
        </div>
        <div class="w-full">
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 mb-2">학생들의 포트폴리오</h2>
          <p class="text-slate-500 text-sm mb-5">제가 지도한 기수별 파이널 프로젝트 결과물(GitHub)입니다.</p>
          <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <a v-for="p in portfolios" :key="p.cohort" :href="p.url" target="_blank" rel="noopener noreferrer"
              class="group flex items-center justify-between gap-3 bg-slate-50 rounded-2xl p-5 border border-slate-100 hover:border-brand hover:bg-blue-50/40 transition-all">
              <span class="flex items-center gap-3">
                <i class="fa-brands fa-github text-slate-700 text-xl"></i>
                <span class="text-sm font-bold text-slate-800">{{ p.cohort }} 파이널 프로젝트</span>
              </span>
              <i class="fa-solid fa-arrow-up-right-from-square text-slate-300 group-hover:text-brand transition-colors text-xs"></i>
            </a>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped></style>

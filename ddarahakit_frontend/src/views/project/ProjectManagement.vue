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
      <h1 class="text-3xl md:text-4xl font-bold text-slate-900 tracking-tight">포트폴리오</h1>
      <p class="text-slate-500 mt-3 max-w-2xl">
        강의 자료 준비부터 산출물 관리, 배포·운영, 학생들이 만들어 낸 결과물까지 —
        강사로서 쌓아온 성과와 노하우를 한곳에 정리했습니다.
      </p>
    </section>

    <!-- 핵심 운영 방식 (세로로 차례대로) -->
    <section class="space-y-6">
      <!-- 01. 수업 자료 완비 -->
      <div class="flex flex-col md:flex-row items-start gap-6 md:gap-10 bg-white rounded-3xl border border-gray-100 p-6 md:p-12 shadow-sm">
        <div class="flex items-center gap-5 shrink-0">
          <span class="text-3xl md:text-5xl font-extrabold text-brand/20 tabular-nums">01</span>
        </div>
        <div>
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 mb-3">수업 자료 완비</h2>
          <p class="text-slate-500 leading-relaxed md:text-lg max-w-3xl">
            모든 수업에 필요한 강의 자료를 빠짐없이 직접 제작해 갖추고 있습니다.
            잘 정리된 자료로 학생들이 막힘없이 학습을 이어가도록 설계했습니다.
          </p>
        </div>
      </div>

      <!-- 02. 산출물 관리 -->
      <div class="flex flex-col md:flex-row items-start gap-6 md:gap-10 bg-white rounded-3xl border border-gray-100 p-6 md:p-12 shadow-sm">
        <div class="flex items-center gap-5 shrink-0">
          <span class="text-3xl md:text-5xl font-extrabold text-brand/20 tabular-nums">02</span>
        </div>
        <div class="w-full">
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 mb-3">산출물 관리</h2>
          <p class="text-slate-500 leading-relaxed md:text-lg max-w-3xl">
            훈련 과정에서 나오는 모든 산출물을 분야별·단계별로 수집해 체계적으로 보관·정리합니다.
            평가 문제 출제와 채점, 팀 구성·발표 평가까지 빠짐없이 기록으로 남겨,
            평가기관의 점검·심사에서 운영기관이 좋은 평가를 받을 수 있도록 근거 자료를 빈틈없이 갖춰 둡니다.
          </p>

          <!-- 실제 관리 문서/자료 카테고리 -->
          <div class="mt-6 grid sm:grid-cols-2 gap-4">
            <!-- 프로젝트 산출물 -->
            <div class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <h3 class="font-bold text-slate-800 mb-3 flex items-center gap-2">
                <i class="fa-solid fa-folder-tree text-brand"></i> 프로젝트 산출물
              </h3>
              <ul class="space-y-1.5 text-sm text-slate-500 leading-relaxed list-disc pl-4 marker:text-slate-300">
                <li>분야별 산출물 보관 — 기반기술 · 프론트엔드 · 백엔드 · 데브옵스</li>
                <li>팀별(5개 팀) 산출물 아카이브</li>
                <li>학생들이 집중하는 코드뿐 아니라, 소홀하기 쉬운 요구사항 정의서 · 시스템 아키텍처 · ERD · 테스트까지 꼼꼼히 챙겨 관리</li>
              </ul>
            </div>

            <!-- 훈련생 학습 관리 -->
            <div class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <h3 class="font-bold text-slate-800 mb-3 flex items-center gap-2">
                <i class="fa-solid fa-user-check text-brand"></i> 훈련생 학습 관리
              </h3>
              <ul class="space-y-1.5 text-sm text-slate-500 leading-relaxed list-disc pl-4 marker:text-slate-300">
                <li>실시간 수업자료 공유</li>
                <li>1:1 상담 일지</li>
                <li>평가 · 채점 (수업 이해도 평가, 팀 매칭 활용 자료)</li>
                <li>중간중간 평가를 수행해 학생들의 이해도를 정량적으로 파악 · 관리</li>
                <li>이런 평가 자료 · 상담 내용을 종합해 프로젝트 팀 구성에 활용</li>
              </ul>
            </div>
          </div>

          <!-- 실제 산출물 관리 자료 -->
          <div class="mt-5 grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <figure>
              <a href="/images/output-checklist.png" target="_blank" rel="noopener noreferrer"
                class="block rounded-2xl border border-slate-100 overflow-hidden hover:border-brand transition-colors">
                <img src="/images/output-checklist.png" alt="팀별 산출물 점검 시트"
                  class="w-full h-40 sm:h-44 object-cover object-top bg-white" loading="lazy" />
              </a>
              <figcaption class="mt-2 text-xs text-slate-400 leading-relaxed">
                팀별 산출물 체크리스트
              </figcaption>
            </figure>
            <figure>
              <a href="/images/submission-archive.png" target="_blank" rel="noopener noreferrer"
                class="block rounded-2xl border border-slate-100 overflow-hidden hover:border-brand transition-colors">
                <img src="/images/submission-archive.png" alt="평가 제출용 프로젝트 산출물 아카이브"
                  class="w-full h-40 sm:h-44 object-cover object-top bg-white" loading="lazy" />
              </a>
              <figcaption class="mt-2 text-xs text-slate-400 leading-relaxed">
                평가 제출용 프로젝트 산출물 아카이브
              </figcaption>
            </figure>
            <figure>
              <a href="/images/evaluation-exam.png" target="_blank" rel="noopener noreferrer"
                class="block rounded-2xl border border-slate-100 overflow-hidden hover:border-brand transition-colors">
                <img src="/images/evaluation-exam.png" alt="직접 출제한 실기 평가 문제"
                  class="w-full h-40 sm:h-44 object-cover object-top bg-white" loading="lazy" />
              </a>
              <figcaption class="mt-2 text-xs text-slate-400 leading-relaxed">
                직접 출제한 실기 평가 문제(일부)
              </figcaption>
            </figure>
          </div>
        </div>
      </div>

      <!-- 03. 배포부터 운영까지 -->
      <div class="flex flex-col md:flex-row items-start gap-6 md:gap-10 bg-white rounded-3xl border border-gray-100 p-6 md:p-12 shadow-sm">
        <div class="flex items-center gap-5 shrink-0">
          <span class="text-3xl md:text-5xl font-extrabold text-brand/20 tabular-nums">03</span>
        </div>
        <div class="w-full">
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 mb-3">배포부터 운영까지</h2>
          <p class="text-slate-500 leading-relaxed md:text-lg max-w-3xl">
            프로젝트에서는 학생들이 직접 만든 서비스를 배포해 실제로 올리게 합니다.
            이렇게 배포된 서비스들은 모니터링 환경에서 상태를 수집해
            응답 시간·SSL 인증서는 물론 서비스 가용률(uptime)까지 직접 측정하고 관리해 줍니다.
            개발에서 끝나지 않고 실제 서비스를 운영하는 경험까지 제공합니다.
          </p>

          <!-- 모니터링 대시보드 -->
          <figure class="mt-6">
            <img src="/images/monitoring.png"
              alt="학생들이 배포한 서비스의 가용률·응답 시간·SSL 상태 모니터링 대시보드"
              class="w-full rounded-2xl border border-slate-100 shadow-sm" loading="lazy" />
            <figcaption class="mt-3 text-sm text-slate-400">
              학생들이 배포한 서비스의 가용률·응답 시간·SSL 상태를 한눈에 모니터링하는 대시보드
            </figcaption>
          </figure>
        </div>
      </div>

      <!-- 04. 학생들의 포트폴리오 -->
      <div class="flex flex-col md:flex-row items-start gap-6 md:gap-10 bg-white rounded-3xl border border-gray-100 p-6 md:p-12 shadow-sm">
        <div class="flex items-center gap-5 shrink-0">
          <span class="text-3xl md:text-5xl font-extrabold text-brand/20 tabular-nums">04</span>
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

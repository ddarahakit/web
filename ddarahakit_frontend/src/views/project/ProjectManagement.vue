<script setup>
// 수업 진행 방식 (포트폴리오) — 수업 진행 방식·지도 노하우. 콘텐츠는 추후 추가.
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

      <!-- 04. 학생 후기 -->
      <div class="flex flex-col md:flex-row items-start gap-6 md:gap-10 bg-white rounded-3xl border border-gray-100 p-8 md:p-12 shadow-sm">
        <div class="flex items-center gap-5 shrink-0">
          <span class="text-4xl md:text-5xl font-extrabold text-brand/20 tabular-nums">04</span>
          <div class="w-14 h-14 rounded-2xl bg-blue-50 flex items-center justify-center">
            <i class="fa-solid fa-comment-dots text-brand text-2xl"></i>
          </div>
        </div>
        <div class="w-full">
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 mb-5">학생 후기</h2>
          <div class="grid sm:grid-cols-2 gap-4">
            <figure class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <div class="flex text-yellow-400 text-xs gap-0.5 mb-3">
                <i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i>
              </div>
              <blockquote class="text-slate-600 leading-relaxed text-sm">
                “수업 자료가 정말 잘 정리되어 있어서 막히는 부분 없이 끝까지 완주했어요.”
              </blockquote>
              <figcaption class="mt-3 text-xs font-semibold text-slate-400">— 백엔드 과정 수강생</figcaption>
            </figure>

            <figure class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <div class="flex text-yellow-400 text-xs gap-0.5 mb-3">
                <i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i>
              </div>
              <blockquote class="text-slate-600 leading-relaxed text-sm">
                “제가 만든 결과물을 꼼꼼히 피드백해 주셔서 완성도가 확 올라갔습니다.”
              </blockquote>
              <figcaption class="mt-3 text-xs font-semibold text-slate-400">— 프론트엔드 과정 수강생</figcaption>
            </figure>

            <figure class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <div class="flex text-yellow-400 text-xs gap-0.5 mb-3">
                <i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i>
              </div>
              <blockquote class="text-slate-600 leading-relaxed text-sm">
                “프로젝트를 직접 배포하고 모니터링까지 해본 경험이 면접에서 큰 도움이 됐어요.”
              </blockquote>
              <figcaption class="mt-3 text-xs font-semibold text-slate-400">— 데브옵스 과정 수강생</figcaption>
            </figure>

            <figure class="bg-slate-50 rounded-2xl p-5 border border-slate-100">
              <div class="flex text-yellow-400 text-xs gap-0.5 mb-3">
                <i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i>
              </div>
              <blockquote class="text-slate-600 leading-relaxed text-sm">
                “단순 강의가 아니라 실제 서비스 운영 경험까지 챙겨주는 점이 가장 좋았습니다.”
              </blockquote>
              <figcaption class="mt-3 text-xs font-semibold text-slate-400">— 풀스택 과정 수강생</figcaption>
            </figure>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped></style>

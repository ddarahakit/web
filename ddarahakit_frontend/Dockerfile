# ===== 빌드 스테이지 =====
FROM node:20-alpine AS build
WORKDIR /app

# 의존성 설치 (lockfile 기반 재현 가능 설치)
COPY package.json package-lock.json ./
RUN npm ci

# 소스 복사 후 프로덕션 빌드 (.env.production 사용 → dist 생성)
COPY . .
RUN npm run build

# ===== 런타임 스테이지 =====
FROM nginx:1.27-alpine
# 정적 빌드 결과물 서빙 + SPA(history 모드) 라우팅 fallback + 정적 자산 캐시.
COPY --from=build /app/dist /usr/share/nginx/html
RUN printf 'server {\n\
  listen 80;\n\
  server_name _;\n\
  root /usr/share/nginx/html;\n\
  index index.html;\n\
  location / {\n\
    try_files $uri $uri/ /index.html;\n\
  }\n\
  location /assets/ {\n\
    expires 1y;\n\
    add_header Cache-Control "public, immutable";\n\
  }\n\
}\n' > /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

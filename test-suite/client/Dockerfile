# Stage 0 - Building
FROM docker.io/node:alpine3.12 as build-stage
WORKDIR /app
COPY package.json yarn.lock ./
RUN yarn
COPY tsconfig.json ./
COPY public ./public
COPY src ./src
RUN yarn build

# Stage 1 - Production
FROM docker.io/nginxinc/nginx-unprivileged:1.20-alpine as prod-stage
COPY --from=build-stage /app/build/ /var/www/html
RUN rm -rf /etc/nginx/conf.d/*
COPY nginx.conf /etc/nginx/

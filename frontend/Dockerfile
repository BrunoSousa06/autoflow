FROM node:22-alpine AS build

WORKDIR /app

RUN chown node:node /app

USER node

COPY --chown=node:node package.json package-lock.json ./

RUN npm ci

COPY --chown=node:node . .

RUN npm run build -- --configuration=production


FROM nginxinc/nginx-unprivileged:1.27-alpine


USER root

RUN apk add --no-cache gettext


COPY --from=build \
     --chown=nginx:nginx \
     /app/dist/frontend/browser \
     /usr/share/nginx/html


COPY --chown=nginx:nginx \
     nginx.conf.template \
     /etc/nginx/templates/default.conf.template


COPY docker-entrypoint.sh \
     /docker-entrypoint.sh


RUN chmod +x /docker-entrypoint.sh \
    && chown nginx:nginx /docker-entrypoint.sh


USER nginx


EXPOSE 8080


ENTRYPOINT ["/docker-entrypoint.sh"]
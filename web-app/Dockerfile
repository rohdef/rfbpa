FROM stable-alpine3.20-slim
#FROM nginx:alpine

COPY ./nginx/nginx.conf /etc/nginx/templates/default.conf.template
COPY ./dist /usr/share/nginx/html

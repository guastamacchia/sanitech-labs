FROM nginx:alpine
WORKDIR /usr/share/nginx/html
COPY frontend/shell/src/ .
EXPOSE 80

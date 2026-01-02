FROM nginx:alpine
WORKDIR /usr/share/nginx/html
COPY frontend/mfe-admin/src/ .
EXPOSE 80

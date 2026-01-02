FROM nginx:alpine
WORKDIR /usr/share/nginx/html
COPY frontend/mfe-doctor/src/ .
EXPOSE 80

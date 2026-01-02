FROM nginx:alpine
WORKDIR /usr/share/nginx/html
COPY frontend/mfe-patient/src/ .
EXPOSE 80

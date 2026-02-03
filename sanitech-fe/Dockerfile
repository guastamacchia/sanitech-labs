FROM node:lts-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
ARG BUILD_ENV=local
RUN npm run build -- --configuration=${BUILD_ENV}

FROM nginx:alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/sanitech-spa /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

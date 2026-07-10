# ============================================================
# Frontend AutoMatch — build de Vite servido con nginx
# VITE_API_URL se deja vacio: el frontend usa rutas relativas
# y nginx hace de proxy hacia el backend (/api, /uploads, /images).
# ============================================================

FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
ENV VITE_API_URL=
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

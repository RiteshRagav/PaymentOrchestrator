# syntax=docker/dockerfile:1
# Multi-stage Dockerfile for Payment Orchestrator Frontend

# Stage 1: Build static assets
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies first for Docker layer caching
COPY package.json package-lock.json ./
RUN npm ci

# Copy sources and compile production bundle
COPY . .
RUN npm run build

# Stage 2: Serve static bundle via high-performance Nginx
FROM nginx:alpine AS runtime

# Copy custom Nginx configuration with API proxy support
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled SPA bundle from builder
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

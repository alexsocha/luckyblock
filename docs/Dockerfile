FROM node:10-alpine as build
WORKDIR /app

COPY package*.json .
RUN npm ci

COPY . .
RUN npm run build

# ================================================================
# STAGE 1: BUILD PHASE
# Uses Node.js to install dependencies and build static assets
# ================================================================
FROM node:20-alpine AS build

# Set working directory inside the build container
WORKDIR /app

# Copy dependency manifests first to leverage Docker layer caching
COPY package*.json ./

# Install exact dependencies defined in package-lock.json (faster/cleaner than npm install)
RUN npm ci

# Copy full application source code into the container
COPY . .

# Compile TypeScript and bundle React application into production static files (/app/dist)
# Transpiles TS/TSX to JavaScript, minifies assets, and removes unused code (tree-shaking)
RUN npm run build


# ================================================================
# STAGE 2: RUNTIME PHASE
# Uses a lightweight Nginx web server to serve compiled assets
# ================================================================
FROM nginx:alpine

# Copy compiled static output from STAGE 1 (/app/dist) into Nginx default web directory
# The Node.js environment, source code, and node_modules are discarded in the final image
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration to support client-side routing (React Router fallback)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Document that the container listens on HTTP port 80
EXPOSE 80

# Start Nginx in foreground mode ("daemon off;") to keep the Docker container running
CMD ["nginx", "-g", "daemon off;"]
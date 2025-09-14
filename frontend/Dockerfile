# Build stage
FROM node:18-alpine as build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Set environment variable for production build
ENV NODE_ENV=production

# Build the application
RUN npm run build

# List contents to verify build
RUN ls -la /app/build/
RUN ls -la /app/build/static/ || echo "No static directory"

# Production stage
FROM nginx:alpine

# Remove default nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy built app from build stage
COPY --from=build /app/build /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Verify files were copied
RUN ls -la /usr/share/nginx/html/
RUN ls -la /usr/share/nginx/html/static/ || echo "No static directory in nginx html"

# Create nginx user if it doesn't exist
RUN adduser -D -s /bin/sh -G www-data nginx || true

# Set correct permissions
RUN chown -R nginx:nginx /usr/share/nginx/html
RUN chmod -R 755 /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]

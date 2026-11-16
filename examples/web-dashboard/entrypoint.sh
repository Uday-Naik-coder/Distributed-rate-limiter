#!/bin/sh
# Entrypoint script to inject runtime config

# Create config.js with runtime API URL
cat > /usr/share/nginx/html/config.js <<EOF
window.__RUNTIME_CONFIG__ = {
  VITE_API_URL: "${VITE_API_URL:-https://distributed-rate-limiter-distributed.onrender.com}"
};
EOF

# Start nginx
exec nginx -g "daemon off;"
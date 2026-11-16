# Distributed Rate Limiter - Render Deployment Guide

## Architecture Overview

```
┌─────────────────┐     HTTPS      ┌─────────────────┐
│   Frontend      │ ─────────────► │   Backend       │
│   (Nginx)       │   API Calls    │   (Spring Boot) │
│   Port: 8080    │                │   Port: 8080    │
└─────────────────┘                └─────────────────┘
       │                                    │
       │                         ┌──────────┴──────────┐
       │                         │   In-Memory         │
       │                         │   Rate Limiter      │
       │                         │   (No Redis needed) │
       │                         └─────────────────────┘
       │
       ▼
   Users access
   via browser
```

## Docker Images

| Service | Dockerfile | Image Name | Port |
|---------|------------|------------|------|
| Backend | `distributed-rate-limiter/Dockerfile.backend` | `rate-limiter-backend` | 8080 |
| Frontend | `distributed-rate-limiter/examples/web-dashboard/Dockerfile.frontend` | `rate-limiter-frontend` | 8080 |

---

## Local Development

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local backend without Docker)
- Node 20+ (for local frontend without Docker)

### Option 1: Full Docker Compose (Recommended)
```bash
# From project root
docker-compose up --build
```
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Health: http://localhost:8080/actuator/health

### Option 2: Hybrid (Local Backend + Docker Frontend)
```bash
# Terminal 1: Backend
cd distributed-rate-limiter
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker

# Terminal 2: Frontend
cd distributed-rate-limiter/examples/web-dashboard
echo "VITE_API_URL=http://localhost:8080" > .env.local
npm run dev
```

---

## Render Deployment

### Method 1: Render Blueprint (Automated)

1. Push code to GitHub
2. In Render Dashboard: **New → Blueprint**
3. Connect your repository
4. Render will detect `render.yaml` and create both services
5. After first deploy, update `VITE_API_URL` in frontend service:
   - Go to Frontend service → Environment
   - Set `VITE_API_URL` to your backend URL (e.g., `https://rate-limiter-backend.onrender.com`)
   - Trigger manual deploy

### Method 2: Manual Service Creation

#### Backend Service (Web Service)
1. **New → Web Service**
2. Connect repository
3. Settings:
   - **Name**: `rate-limiter-backend`
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `distributed-rate-limiter/Dockerfile.backend`
   - **Docker Context**: `distributed-rate-limiter`
4. **Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=docker
   ratelimiter.redis.enabled=false
   ```
5. **Health Check Path**: `/actuator/health`
6. **Auto-Deploy**: Yes

#### Frontend Service (Web Service)
1. **New → Web Service** (or Static Site if preferred)
2. Connect repository
3. Settings:
   - **Name**: `rate-limiter-frontend`
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `distributed-rate-limiter/examples/web-dashboard/Dockerfile.frontend`
   - **Docker Context**: `distributed-rate-limiter/examples/web-dashboard`
4. **Environment Variables**:
   ```
   VITE_API_URL=https://rate-limiter-backend.onrender.com
   ```
   ⚠️ Replace with your actual backend URL after backend deploys
5. **Health Check Path**: `/health`
6. **Auto-Deploy**: Yes

### Post-Deploy Configuration

After both services deploy:

1. **Get Backend URL**: `https://rate-limiter-backend.onrender.com` (or your custom name)
2. **Update Frontend Env Var**:
   - Go to Frontend service → Environment
   - Set `VITE_API_URL=https://YOUR_BACKEND_URL.onrender.com`
   - Save and trigger **Manual Deploy → Clear build cache & deploy**
3. **Verify CORS**: Backend allows `https://*.onrender.com` by default

---

## Environment Variables Reference

### Backend (application-docker.properties)
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8080 | Set by Render automatically |
| `SPRING_PROFILES_ACTIVE` | docker | Activates docker profile |
| `ratelimiter.redis.enabled` | false | Uses in-memory rate limiter |
| `ratelimiter.cors.allowed-origin-patterns` | https://*.onrender.com,... | CORS origins |

### Frontend (Vite build-time)
| Variable | Description |
|----------|-------------|
| `VITE_API_URL` | Full backend URL (e.g., `https://rate-limiter-backend.onrender.com`) |

---

## API Communication Flow

```
User Browser
     │
     ▼
Frontend (Nginx) ──────► https://rate-limiter-frontend.onrender.com
     │
     ├─ Static assets (JS, CSS, images) → Served by Nginx
     │
     └─ API calls (/api/*, /actuator/*) → 
         Fetch with VITE_API_URL prefix →
         https://rate-limiter-backend.onrender.com/api/ratelimit/check
         (CORS allowed by backend)
```

---

## Troubleshooting

### Backend won't start
- Check logs for `SPRING_PROFILES_ACTIVE=docker`
- Verify `ratelimiter.redis.enabled=false`
- Check port binding: `server.port=${PORT:8080}`

### Frontend shows "Backend API unavailable"
- Verify `VITE_API_URL` is set correctly in Render frontend env vars
- Check browser console for CORS errors
- Verify backend health: `curl https://your-backend.onrender.com/actuator/health`

### CORS Errors
- Backend allows `https://*.onrender.com` by default
- For custom domains, add to `ratelimiter.cors.allowed-origin-patterns` in backend env vars
- Format: `https://your-frontend.com,https://*.onrender.com`

### Health Check Failures
- Backend: `/actuator/health` must return 200
- Frontend: `/health` returns 200 (nginx config)
- Increase `start_period` if slow startup

---

## Cost Optimization (Free Tier)

- Both services can run on Render Free tier
- Free tier spins down after 15 min inactivity
- First request after spin-down takes 30-60s
- Consider upgrading to Starter for production use

---

## Scaling Considerations

- **In-memory rate limiter**: Not shared across instances
- For horizontal scaling: Enable Redis (`ratelimiter.redis.enabled=true`) and provision Redis addon
- Frontend: Static files, scales automatically via CDN
- Backend: Single instance on free tier

---

## Useful Commands

```bash
# Build backend image locally
docker build -f distributed-rate-limiter/Dockerfile.backend -t rate-limiter-backend ./distributed-rate-limiter

# Build frontend image locally
docker build -f distributed-rate-limiter/examples/web-dashboard/Dockerfile.frontend \
  --build-arg VITE_API_URL=http://localhost:8080 \
  -t rate-limiter-frontend ./distributed-rate-limiter/examples/web-dashboard

# Run locally with docker-compose
docker-compose up --build

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Clean up
docker-compose down -v
```
import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import { createProxyMiddleware } from 'http-proxy-middleware';

const app = express();
const PORT = process.env.PORT ?? 3001;
const SPRING_BASE_URL = process.env.SPRING_BASE_URL ?? 'http://localhost:8080';

app.use(cors({ origin: 'http://localhost:5173', credentials: true }));

// Health check
app.get('/health', (_req, res) => res.json({ status: 'ok' }));

// Proxy all /api/* to Spring Boot
app.use(
  '/api',
  createProxyMiddleware({
    target: SPRING_BASE_URL,
    changeOrigin: true,
    // SSE: disable buffering for streaming endpoints
    on: {
      proxyReq: (proxyReq, req) => {
        if (req.url?.includes('/analyze/stream')) {
          proxyReq.setHeader('Accept', 'text/event-stream');
        }
      },
    },
  })
);

app.listen(PORT, () => console.log(`BFF running on http://localhost:${PORT}`));

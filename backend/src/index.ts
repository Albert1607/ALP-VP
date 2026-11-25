import express from 'express';
import cors from 'cors';
import { ENV } from './config/env.js';
import routes from './routes/index.js';
import { errorHandler } from './middlewares/errorHandler.js';
import { generalLimiter } from './middlewares/rateLimiter.js';

const app = express();

// Trust proxy for rate limiting behind reverse proxies
app.set('trust proxy', 1);

// Apply General Rate Limiter (600 requests per 15 minutes per IP)
app.use(generalLimiter);

app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Health Check
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'OK', timestamp: new Date().toISOString() });
});

// API Routes
app.use('/api', routes);

// Global Error Handler
app.use(errorHandler);

const PORT = parseInt(ENV.PORT, 10) || 3000;
app.listen(PORT, () => {
  console.log(`🚀 Nutrition App REST API server listening on http://localhost:${PORT}`);
});

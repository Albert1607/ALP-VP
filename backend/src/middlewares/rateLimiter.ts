import rateLimit from 'express-rate-limit';
import { AuthenticatedRequest } from '../types/index.js';

// General rate limit: 600 requests per 15 min per IP
export const generalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 600,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: 'Too many requests from this IP, please try again after 15 minutes',
  },
});

// Auth Login rate limit: 10 per 15 min per IP
export const authLoginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 10,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: 'Too many login attempts. Please try again after 15 minutes.',
  },
});

// AI Endpoints rate limit: 10 per 1 min, keyed by req.user.userId (or IP fallback)
export const aiEndpointLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 10,
  standardHeaders: true,
  legacyHeaders: false,
  keyGenerator: (req: AuthenticatedRequest) => {
    return req.user?.userId || req.ip || 'anonymous';
  },
  message: {
    error: 'Rate limit exceeded for AI features (10 requests per minute). Please try again shortly.',
  },
});

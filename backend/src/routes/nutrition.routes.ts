import { Router } from 'express';
import { NutritionController } from '../controllers/nutrition.controller.js';
import { authenticateToken } from '../middlewares/auth.js';
import { aiEndpointLimiter } from '../middlewares/rateLimiter.js';

const router = Router();

router.use(authenticateToken);

router.post('/analyze', aiEndpointLimiter, NutritionController.analyze);

export default router;

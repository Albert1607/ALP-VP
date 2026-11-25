import { Router } from 'express';
import { PlanController } from '../controllers/plan.controller.js';
import { authenticateToken } from '../middlewares/auth.js';
import { validate } from '../middlewares/validate.js';
import { generatePlanSchema } from '../validators/plan.validator.js';
import { aiEndpointLimiter } from '../middlewares/rateLimiter.js';

const router = Router();

router.use(authenticateToken);

router.post(
  '/generate',
  aiEndpointLimiter,
  validate(generatePlanSchema),
  PlanController.generatePlan
);
router.get('/', PlanController.getUserPlans);
router.patch('/:id/status', PlanController.updatePlanStatus);

export default router;

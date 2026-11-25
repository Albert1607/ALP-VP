import { Router } from 'express';
import { MealController } from '../controllers/meal.controller.js';
import { authenticateToken } from '../middlewares/auth.js';
import { validate } from '../middlewares/validate.js';
import { createMealLogSchema } from '../validators/meal.validator.js';

const router = Router();

router.use(authenticateToken);

router.post('/', validate(createMealLogSchema), MealController.createMealLog);
router.get('/', MealController.getMealLogs);
router.get('/:id', MealController.getMealLogById);
router.delete('/:id', MealController.deleteMealLog);

export default router;

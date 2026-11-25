import { Router } from 'express';
import authRoutes from './auth.routes.js';
import profileRoutes from './profile.routes.js';
import nutritionRoutes from './nutrition.routes.js';
import fridgeRoutes from './fridge.routes.js';
import budgetRoutes from './budget.routes.js';
import planRoutes from './plan.routes.js';
import mealRoutes from './meal.routes.js';
import reminderRoutes from './reminder.routes.js';

const router = Router();

router.use('/auth', authRoutes);
router.use('/profile', profileRoutes);
router.use('/nutrition', nutritionRoutes);
router.use('/fridge', fridgeRoutes);
router.use('/budget', budgetRoutes);
router.use('/plan', planRoutes);
router.use('/meals', mealRoutes);
router.use('/reminders', reminderRoutes);

export default router;

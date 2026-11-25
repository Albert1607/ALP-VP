import { Router } from 'express';
import { BudgetController } from '../controllers/budget.controller.js';
import { authenticateToken } from '../middlewares/auth.js';
import { validate } from '../middlewares/validate.js';
import { setBudgetSchema } from '../validators/budget.validator.js';

const router = Router();

router.use(authenticateToken);

router.get('/', BudgetController.getBudget);
router.post('/', validate(setBudgetSchema), BudgetController.setBudget);
router.put('/', validate(setBudgetSchema), BudgetController.setBudget);

export default router;

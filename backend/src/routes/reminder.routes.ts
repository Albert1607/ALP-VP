import { Router } from 'express';
import { ReminderController } from '../controllers/reminder.controller.js';
import { authenticateToken } from '../middlewares/auth.js';
import { validate } from '../middlewares/validate.js';
import { updateReminderSchema } from '../validators/reminder.validator.js';

const router = Router();

router.use(authenticateToken);

router.get('/status', ReminderController.getStatus);
router.put('/', validate(updateReminderSchema), ReminderController.updateSettings);

export default router;

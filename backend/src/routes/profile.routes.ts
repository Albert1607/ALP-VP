import { Router } from 'express';
import { ProfileController } from '../controllers/profile.controller.js';
import { authenticateToken } from '../middlewares/auth.js';
import { validate } from '../middlewares/validate.js';
import {
  updateProfileSchema,
  updateTimezoneSchema,
  createWeightLogSchema,
  calibrateSchema,
  confirmCalibrateSchema,
} from '../validators/profile.validator.js';

const router = Router();

router.use(authenticateToken);

router.get('/', ProfileController.getProfile);
router.put('/', validate(updateProfileSchema), ProfileController.updateProfile);
router.put('/timezone', validate(updateTimezoneSchema), ProfileController.updateTimezone);
router.post(
  '/weight-log',
  validate(createWeightLogSchema),
  ProfileController.addWeightLog
);
router.post(
  '/calibrate',
  validate(calibrateSchema),
  ProfileController.calibrate
);
router.post(
  '/calibrate/confirm',
  validate(confirmCalibrateSchema),
  ProfileController.confirmCalibration
);

export default router;

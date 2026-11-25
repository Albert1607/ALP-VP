import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller.js';
import { validate } from '../middlewares/validate.js';
import { registerSchema, loginSchema } from '../validators/auth.validator.js';
import { authLoginLimiter } from '../middlewares/rateLimiter.js';

const router = Router();

router.post('/register', validate(registerSchema), AuthController.register);
router.post('/login', authLoginLimiter, validate(loginSchema), AuthController.login);

export default router;

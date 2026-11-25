import { Router } from 'express';
import { FridgeController } from '../controllers/fridge.controller.js';
import { authenticateToken } from '../middlewares/auth.js';
import { validate } from '../middlewares/validate.js';
import {
  createFridgeItemSchema,
  updateFridgeItemSchema,
} from '../validators/fridge.validator.js';
import { aiEndpointLimiter } from '../middlewares/rateLimiter.js';

const router = Router();

router.use(authenticateToken);

router.post(
  '/',
  aiEndpointLimiter,
  validate(createFridgeItemSchema),
  FridgeController.createItem
);
router.get('/', FridgeController.getItems);
router.get('/:id', FridgeController.getItemById);
router.put('/:id', validate(updateFridgeItemSchema), FridgeController.updateItem);
router.delete('/:id', FridgeController.deleteItem);

export default router;

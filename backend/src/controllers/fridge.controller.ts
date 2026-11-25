import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../types/index.js';
import { FridgeService } from '../services/fridge.service.js';

export class FridgeController {
  static async createItem(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const item = await FridgeService.createItem(userId, req.body);
      res.status(201).json({
        message: 'Fridge item added successfully',
        item,
      });
    } catch (err) {
      next(err);
    }
  }

  static async getItems(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const items = await FridgeService.getItems(userId);
      res.status(200).json({ items });
    } catch (err) {
      next(err);
    }
  }

  static async getItemById(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { id } = req.params;
      const item = await FridgeService.getItemById(userId, id);
      res.status(200).json({ item });
    } catch (err) {
      next(err);
    }
  }

  static async updateItem(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { id } = req.params;
      const item = await FridgeService.updateItem(userId, id, req.body);
      res.status(200).json({
        message: 'Fridge item updated successfully',
        item,
      });
    } catch (err) {
      next(err);
    }
  }

  static async deleteItem(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { id } = req.params;
      await FridgeService.deleteItem(userId, id);
      res.status(200).json({ message: 'Fridge item deleted successfully' });
    } catch (err) {
      next(err);
    }
  }
}

import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../types/index.js';
import { MealService } from '../services/meal.service.js';

export class MealController {
  static async createMealLog(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const meal = await MealService.createMealLog(userId, req.body);
      res.status(201).json({
        message: 'Meal logged successfully',
        meal,
      });
    } catch (err) {
      next(err);
    }
  }

  static async getMealLogs(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const dateStr = req.query.date as string | undefined;
      const timezone = req.user?.timezone || 'Asia/Jakarta';
      const result = await MealService.getMealLogs(userId, dateStr, timezone);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  }

  static async getMealLogById(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { id } = req.params;
      const meal = await MealService.getMealLogById(userId, id);
      res.status(200).json({ meal });
    } catch (err) {
      next(err);
    }
  }

  static async deleteMealLog(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { id } = req.params;
      await MealService.deleteMealLog(userId, id);
      res.status(200).json({ message: 'Meal log deleted successfully' });
    } catch (err) {
      next(err);
    }
  }
}

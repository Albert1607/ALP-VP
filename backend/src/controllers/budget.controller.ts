import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../types/index.js';
import { BudgetService } from '../services/budget.service.js';

export class BudgetController {
  static async getBudget(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const result = await BudgetService.getBudget(userId);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  }

  static async setBudget(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { amountIdr, period } = req.body;
      const result = await BudgetService.setBudget(userId, amountIdr, period);
      res.status(200).json({
        message: 'Budget saved successfully',
        ...result,
      });
    } catch (err) {
      next(err);
    }
  }
}

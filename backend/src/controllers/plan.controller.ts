import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../types/index.js';
import { PlanService } from '../services/plan.service.js';

export class PlanController {
  static async generatePlan(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const result = await PlanService.generatePlan(userId, req.body);
      res.status(201).json({
        message: 'Food plan generated successfully',
        ...result,
      });
    } catch (err) {
      next(err);
    }
  }

  static async getUserPlans(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const plans = await PlanService.getUserPlans(userId);
      res.status(200).json({ plans });
    } catch (err) {
      next(err);
    }
  }

  static async updatePlanStatus(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { id } = req.params;
      const { status } = req.body;
      const updated = await PlanService.updatePlanStatus(userId, id, status);
      res.status(200).json({
        message: `Plan status updated to ${status}`,
        plan: updated,
      });
    } catch (err) {
      next(err);
    }
  }
}

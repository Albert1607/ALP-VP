import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../types/index.js';
import { ReminderService } from '../services/reminder.service.js';

export class ReminderController {
  static async getStatus(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const timezone = req.user?.timezone || 'Asia/Jakarta';
      const status = await ReminderService.getReminderStatus(userId, timezone);
      res.status(200).json(status);
    } catch (err) {
      next(err);
    }
  }

  static async updateSettings(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const settings = await ReminderService.updateReminderSettings(userId, req.body);
      res.status(200).json({
        message: 'Reminder settings updated successfully',
        settings,
      });
    } catch (err) {
      next(err);
    }
  }
}

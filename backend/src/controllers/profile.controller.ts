import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../types/index.js';
import { ProfileService } from '../services/profile.service.js';

export class ProfileController {
  static async getProfile(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const result = await ProfileService.getProfile(userId);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  }

  static async updateProfile(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const result = await ProfileService.updateProfile(userId, req.body);
      res.status(200).json({
        message: 'Body profile updated successfully (calibrated activity factor reset)',
        ...result,
      });
    } catch (err) {
      next(err);
    }
  }

  static async updateTimezone(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { timezone } = req.body;
      const result = await ProfileService.updateTimezone(userId, timezone);
      res.status(200).json({
        message: 'Timezone updated successfully',
        ...result,
      });
    } catch (err) {
      next(err);
    }
  }

  static async addWeightLog(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { weightKg, date } = req.body;
      const log = await ProfileService.addWeightLog(userId, weightKg, date);
      res.status(201).json({
        message: 'Weight log recorded',
        weightLog: log,
      });
    } catch (err) {
      next(err);
    }
  }

  static async calibrate(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const days = req.body.days || 14;
      const userTimezone = req.user?.timezone || 'Asia/Jakarta';
      const result = await ProfileService.calibrateActivityFactor(userId, days, userTimezone);
      res.status(200).json({
        message: 'Proposed calibration factor computed successfully',
        calibrationResult: result,
      });
    } catch (err) {
      next(err);
    }
  }

  static async confirmCalibration(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.user!.userId;
      const { proposedFactor } = req.body;
      const result = await ProfileService.confirmCalibration(userId, proposedFactor);
      res.status(200).json({
        message: 'Calibrated activity factor confirmed and applied to profile',
        ...result,
      });
    } catch (err) {
      next(err);
    }
  }
}

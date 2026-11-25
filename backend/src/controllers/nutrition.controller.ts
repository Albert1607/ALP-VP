import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../types/index.js';
import { GeminiService } from '../services/gemini.service.js';
import { AppError } from '../middlewares/errorHandler.js';

export class NutritionController {
  static async analyze(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
      const { textInput, imageBase64, imageMimeType } = req.body;

      if (!textInput && !imageBase64) {
        throw new AppError('Either textInput or imageBase64 must be provided', 400);
      }

      const result = await GeminiService.analyzeNutrition(
        textInput,
        imageBase64,
        imageMimeType || 'image/jpeg'
      );

      res.status(200).json({
        message: 'Nutrition analyzed successfully',
        analysis: result,
      });
    } catch (err) {
      next(err);
    }
  }
}

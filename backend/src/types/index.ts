import { Request } from 'express';

export interface JwtPayload {
  userId: string;
  email: string;
  timezone?: string;
}

export interface AuthenticatedRequest extends Request {
  user?: JwtPayload;
}

export interface StandardNutritionItem {
  name: string;
  grams: number;
  kcalPer100g: number;
  proteinPer100g: number;
  carbPer100g: number;
  fatPer100g: number;
  kcal: number;
  protein: number;
  carbs: number;
  fat: number;
  confidence?: number;
}

export interface NutritionAnalyzeResponse {
  items: StandardNutritionItem[];
  totalKcal: number;
  totalProtein: number;
  totalCarbs: number;
  totalFat: number;
}

export interface GeneratedMealPlanItem {
  slot: 'BREAKFAST' | 'LUNCH' | 'DINNER';
  foodName: string;
  grams: number;
  kcalPer100g: number;
  proteinPer100g: number;
  carbPer100g: number;
  fatPer100g: number;
  estimatedCostIdr: number;
}

export interface GeneratedPlanResult {
  meals: GeneratedMealPlanItem[];
  totalKcal: number;
  totalProtein: number;
  totalCostIdr: number;
}

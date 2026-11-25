import { prisma } from '../config/prisma.js';
import { AppError } from '../middlewares/errorHandler.js';
import { ProfileService } from './profile.service.js';
import { BudgetService } from './budget.service.js';
import { GeminiService } from './gemini.service.js';
import { MealSlot, PlanStatus } from '@prisma/client';
import { normalizeFoodName } from '../utils/food.js';

export interface GeneratePlanOptions {
  date?: string;
  customTargetKcal?: number;
  customTargetProtein?: number;
  customMaxBudget?: number;
}

export class PlanService {
  static async generatePlan(userId: string, options: GeneratePlanOptions = {}) {
    const profileInfo = await ProfileService.getProfile(userId);
    const targetKcal = options.customTargetKcal || profileInfo.targetKcal || 2000;
    const targetProtein = options.customTargetProtein || profileInfo.targetProtein || 100;

    const budgetInfo = await BudgetService.getBudget(userId);
    const maxBudgetDaily = options.customMaxBudget || budgetInfo.dailyLimitIdr || 100000;

    // Fetch candidate fridge ingredients sorted by nearest expiry date first
    const fridgeItems = await prisma.fridgeItem.findMany({
      where: { userId, quantityGramsOrMl: { gt: 0 } },
      orderBy: { expiryDate: 'asc' },
    });

    const candidates = fridgeItems.map((item) => ({
      name: item.name,
      quantityGrams: item.quantityGramsOrMl,
      expiryDate: item.expiryDate ? item.expiryDate.toISOString().split('T')[0] : null,
    }));

    const maxRetries = 3;
    let attempt = 0;
    let feedback = '';
    let isValid = false;

    let finalProcessedMeals: any[] = [];
    let finalComputedKcal = 0;
    let finalComputedProtein = 0;
    let finalComputedCost = 0;

    while (attempt < maxRetries && !isValid) {
      attempt++;

      const rawAiPlan = await GeminiService.generateMenuComposition(
        candidates,
        targetKcal,
        targetProtein,
        maxBudgetDaily,
        feedback
      );

      // Track remaining fridge stock across all meal rows generated in this plan
      const fridgeStockMap = new Map<string, number>();
      fridgeItems.forEach((item) => {
        fridgeStockMap.set(normalizeFoodName(item.name), item.quantityGramsOrMl);
      });

      let currentKcal = 0;
      let currentProtein = 0;
      let currentCost = 0;
      const currentProcessedMeals: any[] = [];

      for (const meal of rawAiPlan.meals || []) {
        const normName = normalizeFoodName(meal.foodName);

        const availableStock = fridgeStockMap.get(normName);
        let grams = meal.grams || 100;
        let estimatedCostIdr = 0;

        if (availableStock !== undefined && availableStock > 0) {
          // Cap ingredient grams to remaining available fridge stock!
          const usedGrams = Math.min(grams, availableStock);
          grams = usedGrams;
          fridgeStockMap.set(normName, availableStock - usedGrams);
          // Price fridge items at 0 IDR
          estimatedCostIdr = 0;
        } else {
          // Non-fridge item or stock exhausted: check PriceCache
          const cachedPrice = await prisma.priceCache.findUnique({
            where: { foodName: normName },
          });

          if (cachedPrice) {
            estimatedCostIdr = (grams / 1000) * cachedPrice.priceIdrPerKg;
          } else {
            estimatedCostIdr = meal.estimatedCostIdr || 0;
          }
        }

        // Read macros from existing Food row when present in DB
        const existingFood = await prisma.food.findFirst({
          where: { name: normName },
        });

        const kcalPer100g = existingFood ? existingFood.kcalPer100g : (meal.kcalPer100g || 0);
        const proteinPer100g = existingFood ? existingFood.proteinPer100g : (meal.proteinPer100g || 0);
        const carbPer100g = existingFood ? existingFood.carbPer100g : (meal.carbPer100g || 0);
        const fatPer100g = existingFood ? existingFood.fatPer100g : (meal.fatPer100g || 0);

        const kcal = (grams / 100) * kcalPer100g;
        const protein = (grams / 100) * proteinPer100g;

        currentKcal += kcal;
        currentProtein += protein;
        currentCost += estimatedCostIdr;

        currentProcessedMeals.push({
          slot: meal.slot as MealSlot,
          foodName: normName,
          grams: parseFloat(grams.toFixed(1)),
          kcalPer100g,
          proteinPer100g,
          carbPer100g,
          fatPer100g,
          estimatedCostIdr: parseFloat(estimatedCostIdr.toFixed(2)),
        });
      }

      // Validation
      const kcalDiffPercent = Math.abs(currentKcal - targetKcal) / targetKcal;
      const kcalValid = kcalDiffPercent <= 0.10;
      const proteinValid = currentProtein >= targetProtein * 0.9;
      const budgetValid = currentCost <= maxBudgetDaily * 1.05;

      finalProcessedMeals = currentProcessedMeals;
      finalComputedKcal = Math.round(currentKcal);
      finalComputedProtein = Math.round(currentProtein);
      finalComputedCost = Math.round(currentCost);

      if (kcalValid && proteinValid && budgetValid) {
        isValid = true;
      } else {
        const issues: string[] = [];
        if (!kcalValid)
          issues.push(
            `Computed calories (${Math.round(currentKcal)} kcal) outside +/- 10% target (${targetKcal} kcal)`
          );
        if (!proteinValid)
          issues.push(
            `Computed protein (${Math.round(currentProtein)}g) below target (${targetProtein}g)`
          );
        if (!budgetValid)
          issues.push(
            `Computed cost (IDR ${Math.round(currentCost)}) exceeded daily budget (IDR ${maxBudgetDaily})`
          );

        feedback = issues.join('; ');
      }
    }

    const planStatus: PlanStatus = isValid ? 'GENERATED' : 'UNVERIFIED';
    const planDate = options.date ? new Date(options.date) : new Date();

    const createdPlan = await prisma.$transaction(async (tx) => {
      const foodPlan = await tx.foodPlan.create({
        data: {
          userId,
          date: planDate,
          targetKcal,
          targetProtein,
          totalCostIdr: finalComputedCost,
          status: planStatus,
        },
      });

      for (const meal of finalProcessedMeals) {
        const normName = normalizeFoodName(meal.foodName);

        let food = await tx.food.findUnique({
          where: { name: normName },
        });

        if (!food) {
          food = await tx.food.create({
            data: {
              name: normName,
              kcalPer100g: meal.kcalPer100g,
              proteinPer100g: meal.proteinPer100g,
              carbPer100g: meal.carbPer100g,
              fatPer100g: meal.fatPer100g,
              source: 'AI',
            },
          });
        }

        await tx.foodPlanMeal.create({
          data: {
            foodPlanId: foodPlan.id,
            slot: meal.slot,
            foodId: food.id,
            grams: meal.grams,
            estimatedCostIdr: meal.estimatedCostIdr,
          },
        });

        // Only upsert PriceCache when no row exists OR existing row has source = 'ai_estimate' (Never overwrite 'user')
        if (meal.estimatedCostIdr > 0 && meal.grams > 0) {
          const pricePerKg = (meal.estimatedCostIdr / meal.grams) * 1000;
          const existingPrice = await tx.priceCache.findUnique({
            where: { foodName: normName },
          });

          if (!existingPrice) {
            await tx.priceCache.create({
              data: {
                foodName: normName,
                priceIdrPerKg: pricePerKg,
                source: 'ai_estimate',
              },
            });
          } else if (existingPrice.source === 'ai_estimate') {
            await tx.priceCache.update({
              where: { foodName: normName },
              data: {
                priceIdrPerKg: pricePerKg,
                source: 'ai_estimate',
              },
            });
          }
        }
      }

      return foodPlan;
    });

    const fullPlan = await prisma.foodPlan.findUnique({
      where: { id: createdPlan.id },
      include: {
        meals: {
          include: { food: true },
        },
      },
    });

    return {
      plan: fullPlan,
      validation: {
        attemptsUsed: attempt,
        validatedSuccessfully: isValid,
        status: planStatus,
        targetKcal,
        computedKcal: finalComputedKcal,
        targetProtein,
        computedProtein: finalComputedProtein,
        maxBudgetDaily,
        computedCost: finalComputedCost,
      },
    };
  }

  static async getUserPlans(userId: string) {
    return await prisma.foodPlan.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      include: {
        meals: {
          include: { food: true },
        },
      },
    });
  }

  static async updatePlanStatus(
    userId: string,
    planId: string,
    status: 'APPROVED' | 'REJECTED'
  ) {
    const plan = await prisma.foodPlan.findFirst({
      where: { id: planId, userId },
    });

    if (!plan) {
      throw new AppError('Food plan not found', 404);
    }

    return await prisma.foodPlan.update({
      where: { id: planId },
      data: { status },
    });
  }
}

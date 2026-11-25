import { prisma } from '../config/prisma.js';
import { AppError } from '../middlewares/errorHandler.js';
import { MealSlot } from '@prisma/client';
import { normalizeFoodName } from '../utils/food.js';
import { getStartAndEndOfDayInTimezone } from '../utils/date.js';

export interface CreateMealItemInput {
  foodName: string;
  grams: number;
  kcalPer100g: number;
  proteinPer100g: number;
  carbPer100g: number;
  fatPer100g: number;
}

export interface CreateMealLogInput {
  slot: MealSlot;
  date?: string;
  items: CreateMealItemInput[];
}

export class MealService {
  private static formatMealLog(log: any) {
    let mealKcal = 0;
    let mealProtein = 0;
    let mealCarbs = 0;
    let mealFat = 0;

    const items = log.items.map((item: any) => {
      const factor = item.grams / 100;
      const kcal = item.food.kcalPer100g * factor;
      const protein = item.food.proteinPer100g * factor;
      const carbs = item.food.carbPer100g * factor;
      const fat = item.food.fatPer100g * factor;

      mealKcal += kcal;
      mealProtein += protein;
      mealCarbs += carbs;
      mealFat += fat;

      return {
        id: item.id,
        foodId: item.foodId,
        name: item.food.name,
        grams: item.grams,
        kcalPer100g: item.food.kcalPer100g,
        proteinPer100g: item.food.proteinPer100g,
        carbPer100g: item.food.carbPer100g,
        fatPer100g: item.food.fatPer100g,
        kcal: parseFloat(kcal.toFixed(1)),
        protein: parseFloat(protein.toFixed(1)),
        carbs: parseFloat(carbs.toFixed(1)),
        fat: parseFloat(fat.toFixed(1)),
      };
    });

    return {
      id: log.id,
      slot: log.slot,
      date: log.date,
      items,
      summary: {
        kcal: parseFloat(mealKcal.toFixed(1)),
        protein: parseFloat(mealProtein.toFixed(1)),
        carbs: parseFloat(mealCarbs.toFixed(1)),
        fat: parseFloat(mealFat.toFixed(1)),
      },
    };
  }

  static async createMealLog(userId: string, input: CreateMealLogInput) {
    const mealDate = input.date ? new Date(input.date) : new Date();

    const mealLog = await prisma.$transaction(async (tx) => {
      const log = await tx.mealLog.create({
        data: {
          userId,
          slot: input.slot,
          date: mealDate,
        },
      });

      for (const item of input.items) {
        const normName = normalizeFoodName(item.foodName);

        let food = await tx.food.findFirst({
          where: { name: normName },
        });

        if (!food) {
          food = await tx.food.create({
            data: {
              name: normName,
              kcalPer100g: item.kcalPer100g,
              proteinPer100g: item.proteinPer100g,
              carbPer100g: item.carbPer100g,
              fatPer100g: item.fatPer100g,
              source: 'USER',
              createdByUserId: userId,
            },
          });
        }

        await tx.mealLogItem.create({
          data: {
            mealLogId: log.id,
            foodId: food.id,
            grams: item.grams,
          },
        });
      }

      return log;
    });

    return await this.getMealLogById(userId, mealLog.id);
  }

  static async getMealLogs(userId: string, dateStr?: string, userTimezone: string = 'Asia/Jakarta') {
    let dateFilter: any = {};
    if (dateStr) {
      const { start, end } = getStartAndEndOfDayInTimezone(dateStr, userTimezone);
      dateFilter = { gte: start, lte: end };
    }

    const logs = await prisma.mealLog.findMany({
      where: {
        userId,
        ...(dateStr ? { date: dateFilter } : {}),
      },
      orderBy: { date: 'desc' },
      include: {
        items: {
          include: { food: true },
        },
      },
    });

    let dayTotalKcal = 0;
    let dayTotalProtein = 0;
    let dayTotalCarbs = 0;
    let dayTotalFat = 0;

    const formattedLogs = logs.map((log) => {
      const formatted = this.formatMealLog(log);
      dayTotalKcal += formatted.summary.kcal;
      dayTotalProtein += formatted.summary.protein;
      dayTotalCarbs += formatted.summary.carbs;
      dayTotalFat += formatted.summary.fat;
      return formatted;
    });

    return {
      mealLogs: formattedLogs,
      daySummary: {
        totalKcal: parseFloat(dayTotalKcal.toFixed(1)),
        totalProtein: parseFloat(dayTotalProtein.toFixed(1)),
        totalCarbs: parseFloat(dayTotalCarbs.toFixed(1)),
        totalFat: parseFloat(dayTotalFat.toFixed(1)),
      },
    };
  }

  static async getMealLogById(userId: string, id: string) {
    const log = await prisma.mealLog.findFirst({
      where: { id, userId },
      include: {
        items: {
          include: { food: true },
        },
      },
    });

    if (!log) {
      throw new AppError('Meal log not found', 404);
    }

    return this.formatMealLog(log);
  }

  static async deleteMealLog(userId: string, id: string) {
    const log = await prisma.mealLog.findFirst({
      where: { id, userId },
    });
    if (!log) {
      throw new AppError('Meal log not found', 404);
    }
    return await prisma.mealLog.delete({
      where: { id: log.id },
    });
  }
}

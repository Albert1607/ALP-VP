import { prisma } from '../config/prisma.js';
import { BudgetPeriod } from '@prisma/client';

export class BudgetService {
  static getDailyBudgetLimit(amountIdr: number, period: BudgetPeriod): number {
    switch (period) {
      case 'DAILY':
        return amountIdr;
      case 'WEEKLY':
        return amountIdr / 7;
      case 'MONTHLY':
        return amountIdr / 30;
      default:
        return amountIdr;
    }
  }

  static async getBudget(userId: string) {
    const budget = await prisma.budget.findUnique({
      where: { userId },
    });

    if (!budget) {
      return {
        budget: null,
        dailyLimitIdr: 0,
      };
    }

    const dailyLimitIdr = Math.round(
      this.getDailyBudgetLimit(budget.amountIdr, budget.period)
    );

    return {
      budget,
      dailyLimitIdr,
    };
  }

  static async setBudget(
    userId: string,
    amountIdr: number,
    period: BudgetPeriod
  ) {
    const budget = await prisma.budget.upsert({
      where: { userId },
      update: {
        amountIdr,
        period,
      },
      create: {
        userId,
        amountIdr,
        period,
      },
    });

    const dailyLimitIdr = Math.round(
      this.getDailyBudgetLimit(budget.amountIdr, budget.period)
    );

    return {
      budget,
      dailyLimitIdr,
    };
  }
}

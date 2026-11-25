import jwt from 'jsonwebtoken';
import { prisma } from '../config/prisma.js';
import { ENV } from '../config/env.js';
import { AppError } from '../middlewares/errorHandler.js';
import { ActivityLevel, Gender, Goal } from '@prisma/client';
import { DateTime } from 'luxon';

export interface BodyProfileInput {
  gender: Gender;
  weightKg: number;
  heightCm: number;
  age: number;
  activityLevel: ActivityLevel;
  goal: Goal;
}

export const ACTIVITY_MULTIPLIERS: Record<ActivityLevel, number> = {
  SEDENTARY: 1.2,
  LIGHT: 1.375,
  MODERATE: 1.55,
  ACTIVE: 1.725,
  VERY_ACTIVE: 1.9,
};

export class ProfileService {
  static calculateBmr(
    gender: Gender,
    weightKg: number,
    heightCm: number,
    age: number
  ): number {
    if (gender === 'MALE') {
      return 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
    } else {
      return 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
    }
  }

  static calculateTdee(
    bmr: number,
    activityLevel: ActivityLevel,
    customMultiplier?: number | null
  ): number {
    const multiplier = customMultiplier ?? ACTIVITY_MULTIPLIERS[activityLevel];
    return bmr * multiplier;
  }

  static calculateTargetKcal(tdee: number, goal: Goal): number {
    switch (goal) {
      case 'LOSE_WEIGHT':
        return Math.max(1200, Math.round(tdee - 500));
      case 'GAIN_WEIGHT':
        return Math.round(tdee + 500);
      case 'MAINTAIN':
      default:
        return Math.round(tdee);
    }
  }

  static calculateTargetProtein(weightKg: number): number {
    return Math.round(weightKg * 2.0); // 2g per kg bodyweight
  }

  static async getProfile(userId: string) {
    const profile = await prisma.bodyProfile.findUnique({
      where: { userId },
    });

    const latestWeightLog = await prisma.weightLog.findFirst({
      where: { userId },
      orderBy: { date: 'desc' },
    });

    if (!profile) {
      return { profile: null, tdee: 0, targetKcal: 0, targetProtein: 0, latestWeightLog };
    }

    const bmr = this.calculateBmr(
      profile.gender,
      profile.weightKg,
      profile.heightCm,
      profile.age
    );
    const tdee = this.calculateTdee(
      bmr,
      profile.activityLevel,
      profile.calibratedActivityFactor
    );
    const targetKcal = this.calculateTargetKcal(tdee, profile.goal);
    const targetProtein = this.calculateTargetProtein(profile.weightKg);

    return {
      profile,
      bmr: Math.round(bmr),
      tdee: Math.round(tdee),
      targetKcal,
      targetProtein,
      latestWeightLog,
    };
  }

  static async updateProfile(userId: string, input: BodyProfileInput) {
    // When activity level or profile changes, reset calibratedActivityFactor to null
    const profile = await prisma.bodyProfile.upsert({
      where: { userId },
      update: {
        gender: input.gender,
        weightKg: input.weightKg,
        heightCm: input.heightCm,
        age: input.age,
        activityLevel: input.activityLevel,
        goal: input.goal,
        calibratedActivityFactor: null, // Reset calibrated factor on profile update
      },
      create: {
        userId,
        gender: input.gender,
        weightKg: input.weightKg,
        heightCm: input.heightCm,
        age: input.age,
        activityLevel: input.activityLevel,
        goal: input.goal,
      },
    });

    // Also log weight change
    await prisma.weightLog.create({
      data: {
        userId,
        weightKg: input.weightKg,
      },
    });

    const bmr = this.calculateBmr(
      profile.gender,
      profile.weightKg,
      profile.heightCm,
      profile.age
    );
    const tdee = this.calculateTdee(bmr, profile.activityLevel, null);
    const targetKcal = this.calculateTargetKcal(tdee, profile.goal);
    const targetProtein = this.calculateTargetProtein(profile.weightKg);

    return {
      profile,
      bmr: Math.round(bmr),
      tdee: Math.round(tdee),
      targetKcal,
      targetProtein,
    };
  }

  static async updateTimezone(userId: string, timezone: string) {
    const user = await prisma.user.update({
      where: { id: userId },
      data: { timezone },
    });

    // Re-issue JWT token with updated timezone
    const token = jwt.sign(
      { userId: user.id, email: user.email, timezone: user.timezone },
      ENV.JWT_SECRET,
      { expiresIn: '7d' }
    );

    return {
      user: {
        id: user.id,
        email: user.email,
        timezone: user.timezone,
      },
      token,
    };
  }

  static async addWeightLog(userId: string, weightKg: number, date?: string) {
    const log = await prisma.weightLog.create({
      data: {
        userId,
        weightKg,
        date: date ? new Date(date) : new Date(),
      },
    });

    await prisma.bodyProfile.updateMany({
      where: { userId },
      data: { weightKg },
    });

    return log;
  }

  static async calibrateActivityFactor(
    userId: string,
    days: number = 14,
    userTimezone: string = 'Asia/Jakarta'
  ) {
    if (days < 7) {
      throw new AppError('Calibration period must be at least 7 days.', 400);
    }

    const profile = await prisma.bodyProfile.findUnique({
      where: { userId },
    });

    if (!profile) {
      throw new AppError('Body profile not found. Please set profile first.', 404);
    }

    const startDate = new Date();
    startDate.setDate(startDate.getDate() - days);

    // 1. Fetch weight logs over the period
    const logs = await prisma.weightLog.findMany({
      where: {
        userId,
        date: { gte: startDate },
      },
      orderBy: { date: 'asc' },
    });

    if (logs.length < 2) {
      throw new AppError(
        `Insufficient weight logs. Need at least 2 weight logs over the past ${days} days.`,
        400
      );
    }

    const firstDate = logs[0].date;
    const lastDate = logs[logs.length - 1].date;
    const daysBetweenLogs = Math.max(
      1,
      Math.round((lastDate.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24))
    );

    if (daysBetweenLogs < 7) {
      throw new AppError(
        `Must have at least 7 days between the first (${firstDate.toISOString().substring(0, 10)}) and last (${lastDate.toISOString().substring(0, 10)}) weight logs.`,
        400
      );
    }

    // 2. Fetch logged meals over the period and count DISTINCT logged days
    const mealLogs = await prisma.mealLog.findMany({
      where: {
        userId,
        date: { gte: startDate },
      },
      include: {
        items: {
          include: { food: true },
        },
      },
    });

    const distinctLoggedDays = new Set(
      mealLogs.map((log) =>
        DateTime.fromJSDate(log.date).setZone(userTimezone).toISODate()
      )
    );

    if (distinctLoggedDays.size < 7) {
      throw new AppError(
        `Insufficient logged meal data. Requires at least 7 distinct days with logged meals (found ${distinctLoggedDays.size} days).`,
        400
      );
    }

    // Compute total logged kcal
    let totalLoggedKcal = 0;
    mealLogs.forEach((ml) => {
      ml.items.forEach((item) => {
        totalLoggedKcal += (item.grams / 100) * item.food.kcalPer100g;
      });
    });

    // Divide total logged kcal by the number of DISTINCT logged days (NO fallback to target kcal!)
    const averageDailyLoggedKcal = totalLoggedKcal / distinctLoggedDays.size;

    // Smooth weight with average of first 3 logs and last 3 logs
    const firstThreeLogs = logs.slice(0, Math.min(3, logs.length));
    const lastThreeLogs = logs.slice(Math.max(0, logs.length - 3));

    const initialSmoothedWeight =
      firstThreeLogs.reduce((acc, l) => acc + l.weightKg, 0) / firstThreeLogs.length;
    const finalSmoothedWeight =
      lastThreeLogs.reduce((acc, l) => acc + l.weightKg, 0) / lastThreeLogs.length;

    const actualWeightDiffKg = finalSmoothedWeight - initialSmoothedWeight;

    // Energy balance: 1 kg body fat ~ 7700 kcal
    const dailyEnergyBalanceKcal = (actualWeightDiffKg * 7700) / daysBetweenLogs;

    const bmr = this.calculateBmr(
      profile.gender,
      profile.weightKg,
      profile.heightCm,
      profile.age
    );
    const actualTdee = averageDailyLoggedKcal - dailyEnergyBalanceKcal;

    // Clamp calibrated factor between 1.2 and 2.0
    const rawCalibratedFactor = actualTdee / bmr;
    const clampedCalibratedFactor = parseFloat(
      Math.min(2.0, Math.max(1.2, rawCalibratedFactor)).toFixed(3)
    );

    const adjustedTargetKcal = this.calculateTargetKcal(bmr * clampedCalibratedFactor, profile.goal);

    // Return proposed factor WITHOUT saving it directly (requires separate confirmation)
    return {
      periodDays: daysBetweenLogs,
      distinctLoggedDaysCount: distinctLoggedDays.size,
      initialSmoothedWeightKg: parseFloat(initialSmoothedWeight.toFixed(2)),
      finalSmoothedWeightKg: parseFloat(finalSmoothedWeight.toFixed(2)),
      weightChangeKg: parseFloat(actualWeightDiffKg.toFixed(2)),
      averageDailyLoggedKcal: Math.round(averageDailyLoggedKcal),
      bmr: Math.round(bmr),
      previousActivityFactor:
        profile.calibratedActivityFactor || ACTIVITY_MULTIPLIERS[profile.activityLevel],
      proposedCalibratedActivityFactor: clampedCalibratedFactor,
      proposedTargetKcal: adjustedTargetKcal,
      status: 'PROPOSED_CALIBRATION_READY',
      note: 'Call POST /api/profile/calibrate/confirm to apply this proposed factor to your profile.',
    };
  }

  static async confirmCalibration(userId: string, proposedFactor: number) {
    if (proposedFactor < 1.2 || proposedFactor > 2.0) {
      throw new AppError('Proposed activity factor must be clamped between 1.2 and 2.0.', 400);
    }

    const updatedProfile = await prisma.bodyProfile.update({
      where: { userId },
      data: {
        calibratedActivityFactor: proposedFactor,
      },
    });

    const bmr = this.calculateBmr(
      updatedProfile.gender,
      updatedProfile.weightKg,
      updatedProfile.heightCm,
      updatedProfile.age
    );
    const tdee = bmr * proposedFactor;
    const targetKcal = this.calculateTargetKcal(tdee, updatedProfile.goal);

    return {
      profile: updatedProfile,
      calibratedActivityFactor: proposedFactor,
      tdee: Math.round(tdee),
      targetKcal,
    };
  }
}

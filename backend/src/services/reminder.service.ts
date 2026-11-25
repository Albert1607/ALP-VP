import { prisma } from '../config/prisma.js';
import { getStartAndEndOfDayInTimezone } from '../utils/date.js';

export interface UpdateReminderSettingInput {
  enabled?: boolean;
  breakfastTime?: string;
  lunchTime?: string;
  dinnerTime?: string;
}

export class ReminderService {
  static async getReminderStatus(userId: string, userTimezone: string = 'Asia/Jakarta') {
    let settings = await prisma.reminderSetting.findUnique({
      where: { userId },
    });

    if (!settings) {
      settings = await prisma.reminderSetting.create({
        data: {
          userId,
          enabled: true,
          breakfastTime: '08:00',
          lunchTime: '12:30',
          dinnerTime: '19:00',
        },
      });
    }

    const { start: todayStart, end: todayEnd } = getStartAndEndOfDayInTimezone(
      new Date(),
      userTimezone
    );

    const todayMealLogs = await prisma.mealLog.findMany({
      where: {
        userId,
        date: { gte: todayStart, lte: todayEnd },
      },
    });

    const loggedSlots = new Set(todayMealLogs.map((log) => log.slot));

    return {
      settings,
      todayLoggedStatus: {
        breakfastLogged: loggedSlots.has('BREAKFAST'),
        lunchLogged: loggedSlots.has('LUNCH'),
        dinnerLogged: loggedSlots.has('DINNER'),
        allLogged:
          loggedSlots.has('BREAKFAST') &&
          loggedSlots.has('LUNCH') &&
          loggedSlots.has('DINNER'),
      },
    };
  }

  static async updateReminderSettings(
    userId: string,
    input: UpdateReminderSettingInput
  ) {
    return await prisma.reminderSetting.upsert({
      where: { userId },
      update: {
        ...(input.enabled !== undefined && { enabled: input.enabled }),
        ...(input.breakfastTime && { breakfastTime: input.breakfastTime }),
        ...(input.lunchTime && { lunchTime: input.lunchTime }),
        ...(input.dinnerTime && { dinnerTime: input.dinnerTime }),
      },
      create: {
        userId,
        enabled: input.enabled ?? true,
        breakfastTime: input.breakfastTime || '08:00',
        lunchTime: input.lunchTime || '12:30',
        dinnerTime: input.dinnerTime || '19:00',
      },
    });
  }
}

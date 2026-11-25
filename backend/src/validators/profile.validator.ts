import { z } from 'zod';

export const updateProfileSchema = z.object({
  body: z.object({
    gender: z.enum(['MALE', 'FEMALE']),
    weightKg: z.number().positive(),
    heightCm: z.number().positive(),
    age: z.number().int().positive(),
    activityLevel: z.enum([
      'SEDENTARY',
      'LIGHT',
      'MODERATE',
      'ACTIVE',
      'VERY_ACTIVE',
    ]),
    goal: z.enum(['LOSE_WEIGHT', 'MAINTAIN', 'GAIN_WEIGHT']),
  }),
});

export const updateTimezoneSchema = z.object({
  body: z.object({
    timezone: z.string().min(1, 'Timezone is required'),
  }),
});

export const createWeightLogSchema = z.object({
  body: z.object({
    weightKg: z.number().positive(),
    date: z.string().datetime().optional(),
  }),
});

export const calibrateSchema = z.object({
  body: z.object({
    days: z.number().int().min(7).max(90).optional().default(14),
  }),
});

export const confirmCalibrateSchema = z.object({
  body: z.object({
    proposedFactor: z.number().min(1.2).max(2.0),
  }),
});

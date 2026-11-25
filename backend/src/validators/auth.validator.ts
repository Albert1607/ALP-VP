import { z } from 'zod';

export const registerSchema = z.object({
  body: z.object({
    email: z.string().email('Invalid email format'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    timezone: z.string().optional().default('Asia/Jakarta'),
    gender: z.enum(['MALE', 'FEMALE']).optional(),
    weightKg: z.number().positive().optional(),
    heightCm: z.number().positive().optional(),
    age: z.number().int().positive().optional(),
    activityLevel: z
      .enum(['SEDENTARY', 'LIGHT', 'MODERATE', 'ACTIVE', 'VERY_ACTIVE'])
      .optional(),
    goal: z.enum(['LOSE_WEIGHT', 'MAINTAIN', 'GAIN_WEIGHT']).optional(),
  }),
});

export const loginSchema = z.object({
  body: z.object({
    email: z.string().email(),
    password: z.string().min(1),
  }),
});

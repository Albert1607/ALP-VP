import { z } from 'zod';

export const createMealLogSchema = z.object({
  body: z.object({
    slot: z.enum(['BREAKFAST', 'LUNCH', 'DINNER']),
    date: z.string().optional(),
    items: z
      .array(
        z.object({
          foodName: z.string().min(1),
          grams: z.number().positive(),
          kcalPer100g: z.number().nonnegative(),
          proteinPer100g: z.number().nonnegative(),
          carbPer100g: z.number().nonnegative(),
          fatPer100g: z.number().nonnegative(),
        })
      )
      .min(1, 'At least one meal item is required'),
  }),
});

import { z } from 'zod';

export const updateReminderSchema = z.object({
  body: z.object({
    enabled: z.boolean().optional(),
    breakfastTime: z
      .string()
      .regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Time format must be HH:mm')
      .optional(),
    lunchTime: z
      .string()
      .regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Time format must be HH:mm')
      .optional(),
    dinnerTime: z
      .string()
      .regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Time format must be HH:mm')
      .optional(),
  }),
});

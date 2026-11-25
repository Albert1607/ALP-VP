import { z } from 'zod';

export const generatePlanSchema = z.object({
  body: z.object({
    date: z.string().optional(),
    customTargetKcal: z.number().positive().optional(),
    customTargetProtein: z.number().positive().optional(),
    customMaxBudget: z.number().positive().optional(),
  }),
});

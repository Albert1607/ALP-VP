import { z } from 'zod';

export const setBudgetSchema = z.object({
  body: z.object({
    amountIdr: z.number().positive(),
    period: z.enum(['DAILY', 'WEEKLY', 'MONTHLY']),
  }),
});

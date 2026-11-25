import { z } from 'zod';

export const createFridgeItemSchema = z.object({
  body: z.object({
    name: z.string().min(1).optional(),
    weightType: z.enum(['GROSS', 'NET', 'FILL_PERCENT', 'VOLUME']).optional().default('NET'),
    rawQuantity: z.number().min(0).optional().default(0),
    quantityGramsOrMl: z.number().min(0).optional(), // Explicit net override if provided
    tareWeight: z.number().min(0).optional().default(0),
    fillPercent: z.number().min(0).max(100).optional().default(100),
    containerCapacity: z.number().min(0).optional().default(0),
    expiryDate: z.string().datetime().nullable().optional(),
    textInput: z.string().optional(),
    imageBase64: z.string().optional(),
  }),
});

export const updateFridgeItemSchema = z.object({
  body: z.object({
    name: z.string().min(1).optional(),
    weightType: z.enum(['GROSS', 'NET', 'FILL_PERCENT', 'VOLUME']).optional(),
    rawQuantity: z.number().min(0).optional(),
    quantityGramsOrMl: z.number().min(0).optional(), // Direct net value update
    tareWeight: z.number().min(0).optional(),
    fillPercent: z.number().min(0).max(100).optional(),
    containerCapacity: z.number().min(0).optional(),
    expiryDate: z.string().datetime().nullable().optional(),
  }),
});

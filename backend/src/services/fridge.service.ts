/**
 * MIGRATION NOTE FOR EXISTING DATABASE ROWS:
 * Run the following SQL query to backfill rawQuantity = quantityGramsOrMl for existing records:
 *
 * UPDATE "FridgeItem"
 * SET "rawQuantity" = "quantityGramsOrMl"
 * WHERE "rawQuantity" = 0 OR "rawQuantity" IS NULL;
 */

import { prisma } from '../config/prisma.js';
import { AppError } from '../middlewares/errorHandler.js';
import { GeminiService } from './gemini.service.js';
import { WeightType } from '@prisma/client';

export interface CreateFridgeItemInput {
  name?: string;
  weightType?: WeightType;
  rawQuantity?: number;
  quantityGramsOrMl?: number; // Explicit net quantity override if provided
  tareWeight?: number;
  fillPercent?: number;
  containerCapacity?: number;
  densityGPerMl?: number;
  expiryDate?: string | null;
  textInput?: string;
  imageBase64?: string;
}

export class FridgeService {
  static computeNetQuantity(
    weightType: WeightType,
    rawQuantity: number,
    containerCapacity: number,
    fillPercent: number,
    tareWeight: number,
    densityGPerMl: number = 1.0
  ): number {
    let net = 0;
    const density = densityGPerMl > 0 ? densityGPerMl : 1.0;

    switch (weightType) {
      case 'GROSS':
        net = rawQuantity * density - tareWeight;
        break;
      case 'FILL_PERCENT':
        // FILL_PERCENT must NOT subtract tareWeight!
        net = ((containerCapacity * fillPercent) / 100) * density;
        break;
      case 'VOLUME':
        net = rawQuantity * density;
        break;
      case 'NET':
      default:
        net = rawQuantity;
        break;
    }

    return Math.max(0, parseFloat(net.toFixed(2)));
  }

  static async createItem(userId: string, input: CreateFridgeItemInput) {
    let itemName = input.name;
    let wType = input.weightType || 'NET';
    let rawQty = input.rawQuantity || 0;
    let tare = input.tareWeight || 0;
    let fill = input.fillPercent !== undefined ? input.fillPercent : 100;
    let cap = input.containerCapacity || 0;
    let density = input.densityGPerMl || 1.0;
    let expiry: Date | null = input.expiryDate ? new Date(input.expiryDate) : null;

    // Call Gemini AI parser if image or text provided
    if ((input.imageBase64 || input.textInput) && (!itemName || rawQty === 0)) {
      const parsed = await GeminiService.parseFridgeItemInput(
        input.textInput,
        input.imageBase64
      );
      if (!itemName) itemName = parsed.name;
      if (rawQty === 0 && parsed.quantityGramsOrMl) rawQty = parsed.quantityGramsOrMl;
      if (cap === 0 && parsed.containerCapacity) cap = parsed.containerCapacity;
      if (fill === 100 && parsed.fillPercent !== undefined) fill = parsed.fillPercent;
      if (tare === 0 && parsed.tareWeight) tare = parsed.tareWeight;
      if (!expiry && parsed.expiryDaysFromNow) {
        expiry = new Date();
        expiry.setDate(expiry.getDate() + parsed.expiryDaysFromNow);
      }
    }

    if (!itemName) {
      throw new AppError('Item name is required', 400);
    }

    // If explicit quantityGramsOrMl is provided on creation: set rawQuantity = that value and weightType NET
    if (input.quantityGramsOrMl !== undefined) {
      rawQty = input.quantityGramsOrMl;
      wType = 'NET';
    }

    const netQty = this.computeNetQuantity(wType, rawQty, cap, fill, tare, density);

    return await prisma.fridgeItem.create({
      data: {
        userId,
        name: itemName.trim(),
        weightType: wType,
        rawQuantity: rawQty,
        quantityGramsOrMl: netQty,
        tareWeight: tare,
        fillPercent: fill,
        containerCapacity: cap,
        densityGPerMl: density,
        expiryDate: expiry,
      },
    });
  }

  static async getItems(userId: string) {
    return await prisma.fridgeItem.findMany({
      where: { userId },
      orderBy: { expiryDate: 'asc' },
    });
  }

  static async getItemById(userId: string, id: string) {
    const item = await prisma.fridgeItem.findFirst({
      where: { id, userId },
    });
    if (!item) {
      throw new AppError('Fridge item not found', 404);
    }
    return item;
  }

  static async updateItem(userId: string, id: string, input: Partial<CreateFridgeItemInput>) {
    const item = await this.getItemById(userId, id);

    let netQty: number;

    const hasQuantityChange =
      input.rawQuantity !== undefined ||
      input.weightType !== undefined ||
      input.tareWeight !== undefined ||
      input.fillPercent !== undefined ||
      input.containerCapacity !== undefined ||
      input.densityGPerMl !== undefined;

    if (input.quantityGramsOrMl !== undefined) {
      netQty = input.quantityGramsOrMl;
    } else if (hasQuantityChange) {
      const wType = input.weightType ?? item.weightType;
      const rawQty = input.rawQuantity ?? item.rawQuantity;
      const tare = input.tareWeight ?? item.tareWeight;
      const fill = input.fillPercent ?? item.fillPercent;
      const cap = input.containerCapacity ?? item.containerCapacity;
      const density = input.densityGPerMl ?? item.densityGPerMl;

      netQty = this.computeNetQuantity(wType, rawQty, cap, fill, tare, density);
    } else {
      // Keep stored net quantity when no quantity-related field is updated
      netQty = item.quantityGramsOrMl;
    }

    return await prisma.fridgeItem.update({
      where: { id: item.id },
      data: {
        name: input.name ? input.name.trim() : item.name,
        weightType: input.weightType ?? item.weightType,
        rawQuantity: input.rawQuantity ?? item.rawQuantity,
        quantityGramsOrMl: netQty,
        tareWeight: input.tareWeight ?? item.tareWeight,
        fillPercent: input.fillPercent ?? item.fillPercent,
        containerCapacity: input.containerCapacity ?? item.containerCapacity,
        densityGPerMl: input.densityGPerMl ?? item.densityGPerMl,
        expiryDate:
          input.expiryDate !== undefined
            ? input.expiryDate
              ? new Date(input.expiryDate)
              : null
            : item.expiryDate,
      },
    });
  }

  static async deleteItem(userId: string, id: string) {
    const item = await this.getItemById(userId, id);
    return await prisma.fridgeItem.delete({
      where: { id: item.id },
    });
  }
}

import { ai, GEMINI_MODEL } from '../config/gemini.js';
import { NutritionAnalyzeResponse, StandardNutritionItem } from '../types/index.js';
import { AppError } from '../middlewares/errorHandler.js';
import { Type, Schema } from '@google/genai';

const nutritionAnalysisSchema: Schema = {
  type: Type.OBJECT,
  properties: {
    items: {
      type: Type.ARRAY,
      items: {
        type: Type.OBJECT,
        properties: {
          name: { type: Type.STRING },
          grams: { type: Type.NUMBER },
          kcalPer100g: { type: Type.NUMBER },
          proteinPer100g: { type: Type.NUMBER },
          carbPer100g: { type: Type.NUMBER },
          fatPer100g: { type: Type.NUMBER },
        },
        required: [
          'name',
          'grams',
          'kcalPer100g',
          'proteinPer100g',
          'carbPer100g',
          'fatPer100g',
        ],
      },
    },
  },
  required: ['items'],
};

const fridgeItemParsingSchema: Schema = {
  type: Type.OBJECT,
  properties: {
    name: { type: Type.STRING },
    quantityGramsOrMl: { type: Type.NUMBER },
    containerCapacity: { type: Type.NUMBER },
    fillPercent: { type: Type.NUMBER },
    tareWeight: { type: Type.NUMBER },
    expiryDaysFromNow: { type: Type.NUMBER },
  },
  required: ['name', 'quantityGramsOrMl'],
};

export class GeminiService {
  static async analyzeNutrition(
    textInput?: string,
    imageBase64?: string,
    imageMimeType: string = 'image/jpeg'
  ): Promise<NutritionAnalyzeResponse> {
    try {
      const contents: any[] = [];

      if (imageBase64) {
        contents.push({
          inlineData: {
            mimeType: imageMimeType,
            data: imageBase64,
          },
        });
      }

      const promptText = `Analyze the food in the input (${textInput || 'image'}). For each food item identified, estimate its weight in grams and per-100g nutrition values (kcalPer100g, proteinPer100g, carbPer100g, fatPer100g).`;
      contents.push(promptText);

      const response = await ai.models.generateContent({
        model: GEMINI_MODEL,
        contents,
        config: {
          responseMimeType: 'application/json',
          responseSchema: nutritionAnalysisSchema,
        },
      });

      const responseText = response.text;
      if (!responseText) {
        throw new AppError('Gemini API returned empty response', 500);
      }

      const parsed = JSON.parse(responseText);
      const rawItems = parsed.items || [];

      // Backend Recomputation of macros: (grams / 100) * per100g (Do NOT trust AI totals)
      let totalKcal = 0;
      let totalProtein = 0;
      let totalCarbs = 0;
      let totalFat = 0;

      const items: StandardNutritionItem[] = rawItems.map((item: any) => {
        const grams = item.grams || 100;
        const kcalPer100g = item.kcalPer100g || 0;
        const proteinPer100g = item.proteinPer100g || 0;
        const carbPer100g = item.carbPer100g || 0;
        const fatPer100g = item.fatPer100g || 0;

        const factor = grams / 100;
        const kcal = factor * kcalPer100g;
        const protein = factor * proteinPer100g;
        const carbs = factor * carbPer100g;
        const fat = factor * fatPer100g;

        totalKcal += kcal;
        totalProtein += protein;
        totalCarbs += carbs;
        totalFat += fat;

        return {
          name: item.name.trim(),
          grams,
          kcalPer100g,
          proteinPer100g,
          carbPer100g,
          fatPer100g,
          kcal: parseFloat(kcal.toFixed(1)),
          protein: parseFloat(protein.toFixed(1)),
          carbs: parseFloat(carbs.toFixed(1)),
          fat: parseFloat(fat.toFixed(1)),
        };
      });

      return {
        items,
        totalKcal: parseFloat(totalKcal.toFixed(1)),
        totalProtein: parseFloat(totalProtein.toFixed(1)),
        totalCarbs: parseFloat(totalCarbs.toFixed(1)),
        totalFat: parseFloat(totalFat.toFixed(1)),
      };
    } catch (err: any) {
      console.error('[Gemini Analyze Error]:', err);
      if (err?.status === 429 || err?.message?.includes('429') || err?.message?.includes('RESOURCE_EXHAUSTED')) {
        throw new AppError('AI service rate limit exceeded. Please try again in a moment.', 429);
      }
      throw new AppError('Failed to process AI nutrition analysis. Please try again later.', 500);
    }
  }

  static async parseFridgeItemInput(
    textInput?: string,
    imageBase64?: string,
    imageMimeType: string = 'image/jpeg'
  ) {
    try {
      const contents: any[] = [];
      if (imageBase64) {
        contents.push({
          inlineData: {
            mimeType: imageMimeType,
            data: imageBase64,
          },
        });
      }

      const prompt = `Identify the food item and estimate its net weight in grams/ml, container total capacity (if applicable in ml/g), fill percentage (0-100), container tare weight in grams (if any), and estimated shelf life/expiry in days from today. Text input: "${textInput || 'None'}"`;
      contents.push(prompt);

      const response = await ai.models.generateContent({
        model: GEMINI_MODEL,
        contents,
        config: {
          responseMimeType: 'application/json',
          responseSchema: fridgeItemParsingSchema,
        },
      });

      const text = response.text;
      if (!text) throw new AppError('Failed to parse fridge item with AI', 500);
      return JSON.parse(text);
    } catch (err: any) {
      console.error('[Gemini Fridge Parsing Error]:', err);
      if (err?.status === 429 || err?.message?.includes('429') || err?.message?.includes('RESOURCE_EXHAUSTED')) {
        throw new AppError('AI service rate limit exceeded. Please try again in a moment.', 429);
      }
      throw new AppError('Failed to parse fridge input with AI. Please try again later.', 500);
    }
  }

  static async generateMenuComposition(
    ingredientsWithExpiry: { name: string; quantityGrams: number; expiryDate?: string | null }[],
    targetKcal: number,
    targetProtein: number,
    maxBudgetDailyIdr: number,
    retryFeedback?: string
  ) {
    const menuPlanSchema: Schema = {
      type: Type.OBJECT,
      properties: {
        meals: {
          type: Type.ARRAY,
          items: {
            type: Type.OBJECT,
            properties: {
              slot: { type: Type.STRING },
              foodName: { type: Type.STRING },
              grams: { type: Type.NUMBER },
              kcalPer100g: { type: Type.NUMBER },
              proteinPer100g: { type: Type.NUMBER },
              carbPer100g: { type: Type.NUMBER },
              fatPer100g: { type: Type.NUMBER },
              estimatedCostIdr: { type: Type.NUMBER },
            },
            required: [
              'slot',
              'foodName',
              'grams',
              'kcalPer100g',
              'proteinPer100g',
              'carbPer100g',
              'fatPer100g',
              'estimatedCostIdr',
            ],
          },
        },
      },
      required: ['meals'],
    };

    const ingredientListStr = ingredientsWithExpiry.length > 0
      ? ingredientsWithExpiry
          .map(
            (ing) =>
              `- "${ing.name}": ${ing.quantityGrams}g available${
                ing.expiryDate ? ` (expiring: ${ing.expiryDate})` : ''
              }`
          )
          .join('\n')
      : 'Common healthy kitchen ingredients (rice, eggs, chicken, veggies, tofu, etc.)';

    const prompt = `You are a professional nutritionist AI. Compose a 1-day meal plan consisting of BREAKFAST, LUNCH, and DINNER.

Available candidate ingredients in user's fridge:
${ingredientListStr}

CRITICAL RULES:
1. When using ingredients from the fridge, REUSE THE EXACT ITEM NAMES VERBATIM as listed above.
2. Target Daily Calories: EXACTLY ${targetKcal} kcal (MUST BE WITHIN +/- 10% of target: ${Math.round(
      targetKcal * 0.9
    )} - ${Math.round(targetKcal * 1.1)} kcal).
3. Target Daily Protein: AT LEAST ${targetProtein} grams of protein.
4. Total Estimated Cost (IDR): MUST NOT EXCEED IDR ${maxBudgetDailyIdr}.
5. For ingredients from the fridge, set estimatedCostIdr = 0. For missing ingredients to buy, estimate market cost in IDR.
6. Provide per-100g macros (kcalPer100g, proteinPer100g, carbPer100g, fatPer100g) for every food item.

${
  retryFeedback
    ? `PREVIOUS ATTEMPT REJECTION REASON: ${retryFeedback}. ADJUST PORTIONS OR INGREDIENTS TO SATISFY ALL CONSTRAINTS STRICTLY.`
    : ''
}

Generate structured JSON response following the schema.`;

    try {
      const response = await ai.models.generateContent({
        model: GEMINI_MODEL,
        contents: [prompt],
        config: {
          responseMimeType: 'application/json',
          responseSchema: menuPlanSchema,
        },
      });

      const text = response.text;
      if (!text) throw new AppError('Gemini returned empty plan generation response', 500);

      return JSON.parse(text);
    } catch (err: any) {
      console.error('[Gemini Plan Generation Error]:', err);
      if (err?.status === 429 || err?.message?.includes('429') || err?.message?.includes('RESOURCE_EXHAUSTED')) {
        throw new AppError('AI service rate limit exceeded. Please try again in a moment.', 429);
      }
      throw new AppError('Failed to generate menu plan with AI. Please try again later.', 500);
    }
  }
}

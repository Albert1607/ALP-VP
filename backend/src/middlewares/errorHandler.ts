import { Request, Response, NextFunction } from 'express';

export class AppError extends Error {
  statusCode: number;

  constructor(message: string, statusCode: number = 400) {
    super(message);
    this.statusCode = statusCode;
  }
}

export const errorHandler = (
  err: any,
  req: Request,
  res: Response,
  next: NextFunction
): void => {
  console.error('[Server Error Log]:', err);

  if (err instanceof AppError) {
    res.status(err.statusCode).json({ error: err.message });
    return;
  }

  // Handle Gemini API Rate Limit / Resource Exhausted
  if (
    err?.status === 429 ||
    err?.message?.includes('429') ||
    err?.message?.includes('RESOURCE_EXHAUSTED') ||
    err?.message?.includes('Quota exceeded')
  ) {
    res.status(429).json({
      error: 'AI service rate limit exceeded. Please try again in a moment.',
    });
    return;
  }

  // Hide internal & external error details from client response
  res.status(500).json({
    error: 'An unexpected server error occurred. Please try again later.',
  });
};

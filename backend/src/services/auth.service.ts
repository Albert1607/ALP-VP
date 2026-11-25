import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { prisma } from '../config/prisma.js';
import { ENV } from '../config/env.js';
import { AppError } from '../middlewares/errorHandler.js';
import { ActivityLevel, Gender, Goal } from '@prisma/client';

export interface RegisterInput {
  email: string;
  password: string;
  timezone?: string;
  gender?: Gender;
  weightKg?: number;
  heightCm?: number;
  age?: number;
  activityLevel?: ActivityLevel;
  goal?: Goal;
}

export class AuthService {
  static async register(input: RegisterInput) {
    const existing = await prisma.user.findUnique({
      where: { email: input.email },
    });
    if (existing) {
      throw new AppError('Email is already registered', 400);
    }

    const passwordHash = await bcrypt.hash(input.password, 10);
    const timezone = input.timezone || 'Asia/Jakarta';

    const user = await prisma.user.create({
      data: {
        email: input.email,
        passwordHash,
        timezone,
        reminderSetting: {
          create: {
            enabled: true,
            breakfastTime: '08:00',
            lunchTime: '12:30',
            dinnerTime: '19:00',
          },
        },
      },
    });

    if (
      input.gender &&
      input.weightKg &&
      input.heightCm &&
      input.age &&
      input.activityLevel &&
      input.goal
    ) {
      await prisma.bodyProfile.create({
        data: {
          userId: user.id,
          gender: input.gender,
          weightKg: input.weightKg,
          heightCm: input.heightCm,
          age: input.age,
          activityLevel: input.activityLevel,
          goal: input.goal,
        },
      });

      await prisma.weightLog.create({
        data: {
          userId: user.id,
          weightKg: input.weightKg,
        },
      });
    }

    const token = jwt.sign(
      { userId: user.id, email: user.email, timezone: user.timezone },
      ENV.JWT_SECRET,
      { expiresIn: '7d' }
    );

    return {
      user: {
        id: user.id,
        email: user.email,
        timezone: user.timezone,
      },
      token,
    };
  }

  static async login(email: string, password: string) {
    const user = await prisma.user.findUnique({
      where: { email },
      include: { profile: true },
    });

    if (!user) {
      throw new AppError('Invalid email or password', 401);
    }

    const valid = await bcrypt.compare(password, user.passwordHash);
    if (!valid) {
      throw new AppError('Invalid email or password', 401);
    }

    const token = jwt.sign(
      { userId: user.id, email: user.email, timezone: user.timezone },
      ENV.JWT_SECRET,
      { expiresIn: '7d' }
    );

    return {
      user: {
        id: user.id,
        email: user.email,
        timezone: user.timezone,
        profile: user.profile,
      },
      token,
    };
  }
}

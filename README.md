# ALP-VP

Autonomous Learning Project (ALP) for Visual Programming course.

## Overview
A comprehensive nutrition and meal planning application consisting of:
- **Android App (`app/`)**: Built with Kotlin, Jetpack Compose, Hilt, Room, Retrofit, and WorkManager.
- **Backend API (`backend/`)**: Built with Node.js, Express, TypeScript, Prisma ORM, and Google Gemini AI integration.

## Features
- **Authentication**: JWT-based user authentication and profile management.
- **Meal & Nutrition Tracking**: Log meals, calculate nutritional intake, and view insights.
- **Fridge Inventory**: Manage ingredients and stock in your fridge.
- **Budget Tracking**: Monitor food and grocery spending.
- **Meal Planning**: AI-assisted meal suggestions and scheduling powered by Gemini.
- **Reminders**: Scheduled meal and nutrition notifications with WorkManager.

## Getting Started

### Android App
1. Open the project in Android Studio.
2. Sync Gradle dependencies.
3. Configure `network_security_config.xml` or base API URL in `Constants.kt` if running on emulator / physical device.
4. Run the app.

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Copy environment configuration:
   ```bash
   cp .env.example .env
   ```
4. Configure database connection and API keys in `.env`.
5. Run database migrations:
   ```bash
   npx prisma migrate dev
   ```
6. Start development server:
   ```bash
   npm run dev
   ```

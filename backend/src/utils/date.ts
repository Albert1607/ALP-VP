import { DateTime } from 'luxon';

/**
 * Returns the UTC start and end Date objects corresponding to a local date string (YYYY-MM-DD)
 * or Date object in the specified user timezone.
 */
export function getStartAndEndOfDayInTimezone(
  dateInput?: Date | string,
  timeZone: string = 'Asia/Jakarta'
): { start: Date; end: Date } {
  let dt: DateTime;

  if (typeof dateInput === 'string' && dateInput.length >= 10) {
    // Extract YYYY-MM-DD part and treat as local date in user's timezone
    const dateOnlyStr = dateInput.substring(0, 10);
    dt = DateTime.fromISO(dateOnlyStr, { zone: timeZone });
  } else if (dateInput instanceof Date) {
    dt = DateTime.fromJSDate(dateInput).setZone(timeZone);
  } else {
    dt = DateTime.now().setZone(timeZone);
  }

  if (!dt.isValid) {
    dt = DateTime.now().setZone(timeZone);
  }

  const start = dt.startOf('day').toJSDate();
  const end = dt.endOf('day').toJSDate();

  return { start, end };
}

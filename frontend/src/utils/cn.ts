import { clsx, type ClassValue } from 'clsx';

/**
 * Utility for conditional Tailwind class merging.
 *
 * Usage:
 *   cn('base-class', isActive && 'active-class', 'another-class')
 *   cn('px-4', size === 'lg' && 'py-3', size === 'sm' && 'py-1')
 */
export function cn(...inputs: ClassValue[]): string {
  return clsx(inputs);
}

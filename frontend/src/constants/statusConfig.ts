import { RequestStatus } from '@/types/request.types';

interface StatusConfigEntry {
  label: string;
  color: string;   // Tailwind CSS badge classes
  dot:   string;   // Tailwind CSS indicator dot class
}

/**
 * Maps each RequestStatus to its UI representation.
 *
 * Why Record<RequestStatus, ...>?
 * TypeScript enforces that EVERY status has an entry here.
 * If you add a new status to the type and forget to add it here,
 * TypeScript shows an error immediately — before you even run the app.
 */
export const STATUS_CONFIG: Record<RequestStatus, StatusConfigEntry> = {
  PENDING: {
    label: 'Pending',
    color: 'bg-yellow-100 text-yellow-800 border border-yellow-200',
    dot:   'bg-yellow-500',
  },
  ASSIGNED: {
    label: 'Assigned',
    color: 'bg-blue-100 text-blue-800 border border-blue-200',
    dot:   'bg-blue-500',
  },
  IN_PROGRESS: {
    label: 'In Progress',
    color: 'bg-orange-100 text-orange-800 border border-orange-200',
    dot:   'bg-orange-500',
  },
  COMPLETED: {
    label: 'Completed',
    color: 'bg-green-100 text-green-800 border border-green-200',
    dot:   'bg-green-500',
  },
  CANCELLED: {
    label: 'Cancelled',
    color: 'bg-gray-100 text-gray-500 border border-gray-200',
    dot:   'bg-gray-400',
  },
  FAILED: {
    label: 'Failed',
    color: 'bg-red-100 text-red-800 border border-red-200',
    dot:   'bg-red-500',
  },
};

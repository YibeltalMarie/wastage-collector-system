/**
 * Date formatting utilities.
 * Centralise all date formatting here so you change it in one place.
 */

export const formatDate = (isoString: string): string => {
  return new Date(isoString).toLocaleDateString('en-ET', {
    year:  'numeric',
    month: 'long',
    day:   'numeric',
  });
};

export const formatDateTime = (isoString: string): string => {
  return new Date(isoString).toLocaleString('en-ET', {
    year:   'numeric',
    month:  'short',
    day:    'numeric',
    hour:   '2-digit',
    minute: '2-digit',
  });
};

export const formatRelativeTime = (isoString: string): string => {
  const now  = new Date();
  const date = new Date(isoString);
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60_000);

  if (diffMins < 1)   return 'just now';
  if (diffMins < 60)  return `${diffMins}m ago`;
  if (diffMins < 1440) return `${Math.floor(diffMins / 60)}h ago`;
  return `${Math.floor(diffMins / 1440)}d ago`;
};

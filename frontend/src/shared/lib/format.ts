export function formatDistance(km: number | null | undefined): string {
  if (km == null || Number.isNaN(km)) return '—';
  return `${km.toFixed(1)} km`;
}

export function formatDuration(minutes: number | null | undefined): string {
  if (minutes == null || Number.isNaN(minutes)) return '—';
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hours === 0) return `${mins} min`;
  if (mins === 0) return `${hours} h`;
  return `${hours} h ${mins} min`;
}

export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return '—';
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

export function formatAddress(location: {
  street?: string;
  streetNumber?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
} | null | undefined): string {
  if (!location) return '—';
  const parts = [
    [location.street, location.streetNumber].filter(Boolean).join(' '),
    location.city,
    location.state,
    location.country,
  ].filter(Boolean);
  const postal = location.postalCode ? ` (${location.postalCode})` : '';
  return `${parts.join(', ')}${postal}` || '—';
}

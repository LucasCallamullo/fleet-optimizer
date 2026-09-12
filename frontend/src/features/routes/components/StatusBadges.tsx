import { cn } from '@shared/lib/utils';
import type { RouteStatus, LegStatus } from '../types/routeTypes';

type Status = RouteStatus | LegStatus;

const STATUS_STYLES: Record<Status, string> = {
  PLANNED: 'bg-[--color-info]/10 text-[--color-info] border-[--color-info]/30',
  IN_PROGRESS: 'bg-[--color-warning]/10 text-[--color-warning] border-[--color-warning]/30',
  COMPLETED: 'bg-[--color-success]/10 text-[--color-success] border-[--color-success]/30',
  CANCELLED: 'bg-[--color-error]/10 text-[--color-error] border-[--color-error]/30',
  PENDING: 'bg-muted text-muted-foreground border-border',
  IN_TRANSIT: 'bg-[--color-info]/10 text-[--color-info] border-[--color-info]/30',
  FAILED: 'bg-[--color-info]/10 text-[--color-info] border-[--color-info]/30',
  SKIPPED: 'bg-[--color-info]/10 text-[--color-info] border-[--color-info]/30',
};
 
const STATUS_LABELS: Record<Status, string> = {
  PLANNED: 'Planned',
  IN_PROGRESS: 'In progress',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  PENDING: 'Pending',
  IN_TRANSIT: 'In transit',
  FAILED: 'bg-[--color-info]/10 text-[--color-info] border-[--color-info]/30',
  SKIPPED: 'bg-[--color-info]/10 text-[--color-info] border-[--color-info]/30',
};

export function StatusBadge({ status }: { status: Status }) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium',
        STATUS_STYLES[status]
      )}
    >
      {STATUS_LABELS[status]}
    </span>
  );
}
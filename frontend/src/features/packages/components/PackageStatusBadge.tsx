import type { PackageStatus } from '../types/packageTypes';

// ================================================================
// TYPES & CONFIGURATION
// ================================================================

export interface PackageStatusBadgeProps {
  status: PackageStatus;
}

interface StatusConfig {
  label: string;
  color: string;
}

const statusConfig: Record<PackageStatus, StatusConfig> = {
  CREATED: {
    label: 'Created',
    color: 'bg-muted text-muted-foreground border-border',
  },
  PROCESSING: {
    label: 'Processing',
    color: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20',
  },
  READY_FOR_PICKUP: {
    label: 'Ready for Pickup',
    color: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20',
  },
  IN_TRANSIT: {
    label: 'In Transit',
    color: 'bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/20',
  },
  DELIVERED: {
    label: 'Delivered',
    color: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20',
  },
  ON_HOLD: {
    label: 'On Hold',
    color: 'bg-orange-500/10 text-orange-600 dark:text-orange-400 border-orange-500/20',
  },
  CANCELLED: {
    label: 'Cancelled',
    color: 'bg-destructive/10 text-destructive border-destructive/20',
  },
};

// ================================================================
// COMPONENT
// ================================================================

export default function PackageStatusBadge({ status }: PackageStatusBadgeProps) {
  const config = statusConfig[status] || statusConfig.CREATED;

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${config.color}`}
    >
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-75" />
      {config.label}
    </span>
  );
}
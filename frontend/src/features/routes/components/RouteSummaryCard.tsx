import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { StatusBadge } from './StatusBadges';
import { formatDistance, formatDuration } from '@shared/lib/format';
import type { RouteDetail } from '../types/routeCreateTypes';

export function RouteSummaryCard({ route }: { route: RouteDetail }) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between gap-4">
        <div>
          <CardTitle className="text-xl">{route.name}</CardTitle>
          <p className="text-sm text-muted-foreground">Route #{route.id}</p>
        </div>
        <StatusBadge status={route.status} />
      </CardHeader>
      <CardContent className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <Stat label="Distance" value={formatDistance(route.estimatedDistanceKm)} />
        <Stat label="Duration" value={formatDuration(route.estimatedDurationMinutes)} />
      </CardContent>
    </Card>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs uppercase tracking-wide text-muted-foreground">{label}</span>
      <span className="text-sm font-medium">{value}</span>
    </div>
  );
}
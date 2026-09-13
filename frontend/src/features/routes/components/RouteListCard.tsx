import { Link } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { StatusBadge } from './StatusBadges';
import { formatDateTime, formatDistance, formatDuration } from '@shared/lib/format';
import type { RouteDetail } from '../types/routeCreateTypes';

export function RouteListCard({ route }: { route: RouteDetail }) {
  const legCount = route.legs.length;

  return (
    <Link to={`/routes/${route.id}`} className="block focus:outline-none">
      <Card className="transition-colors hover:border-primary/40 hover:bg-muted/40">
        <CardHeader className="flex flex-row items-start justify-between gap-4">
          <div className="min-w-0">
            <CardTitle className="truncate text-base">{route.name}</CardTitle>
            <p className="mt-1 truncate text-sm text-muted-foreground">
              {route.description || `Route #${route.id}`}
            </p>
          </div>
          <StatusBadge status={route.status} />
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            <Stat label="Legs" value={String(legCount)} />
            <Stat label="Distance" value={formatDistance(route.estimatedDistanceKm)} />
            <Stat label="Duration" value={formatDuration(route.estimatedDurationMinutes)} />
            <Stat label="Created" value={formatDateTime(route.createdAt)} />
          </div>
        </CardContent>
      </Card>
    </Link>
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
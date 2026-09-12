import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { StatusBadge } from './StatusBadges';
import { formatAddress, formatDateTime, formatDistance, formatDuration } from '@shared/lib/format';
import type { RouteLeg } from '../types/routeCreateTypes';

export function RouteLegCard({ leg }: { leg: RouteLeg }) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between gap-4">
        <CardTitle className="text-base">
          Leg {leg.sequence} <span className="text-muted-foreground">#{leg.id}</span>
        </CardTitle>
        <StatusBadge status={leg.status} />
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
          <Stat label="Distance" value={formatDistance(leg.distanceKm)} />
          <Stat label="Duration" value={formatDuration(leg.durationMinutes)} />
          <Stat label="Vehicle" value={`#${leg.vehicleId}`} />
          {/* <Stat label="Weight" value={`${leg.weigth} kg`} />
          <Stat label="Volume" value={`${leg.volumeCbm} m³`} /> */}
        </div>

        <div className="grid gap-3 md:grid-cols-2">
          <AddressBlock label="Origin" location={leg.origin} />
          <AddressBlock label="Destination" location={leg.destination} />
        </div>

        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <span>
            Package <span className="font-medium text-foreground">#{leg.packageId}</span>
          </span>
          <span>
            Vehicle <span className="font-medium text-foreground">#{leg.vehicleId}</span>
          </span>
          <span>ETA {formatDateTime(leg?.startedAt || '')}</span>
        </div>
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

function AddressBlock({
  label,
  location,
}: {
  label: string;
  location: RouteLeg['origin'];
}) {
  return (
    <div className="rounded-md border border-border bg-muted/40 p-3">
      <p className="mb-1 text-xs uppercase tracking-wide text-muted-foreground">{label}</p>
      <p className="text-sm font-medium">{formatAddress(location)}</p>
      <p className="mt-1 text-xs text-muted-foreground">
        {location.latitude.toFixed(4)}, {location.longitude.toFixed(4)}
      </p>
    </div>
  );
}
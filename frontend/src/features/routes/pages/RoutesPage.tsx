import { useEffect, useMemo, useState } from 'react';
import { routesApi } from '../api/routesApi';
import { RouteListCard } from '../components/RouteListCard';
import { Input } from '@shared/components/ui/input';
import type { RouteDetail, RouteStatus } from '../types/routeCreateTypes';

const STATUS_FILTERS: Array<{ label: string; value: RouteStatus | 'ALL' }> = [
  { label: 'All', value: 'ALL' },
  { label: 'Planned', value: 'PLANNED' },
  { label: 'In progress', value: 'IN_PROGRESS' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' },
];

export default function RoutesPage() {
  const [routes, setRoutes] = useState<RouteDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<RouteStatus | 'ALL'>('ALL');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    routesApi
      .getAll()
      .then((data) => {
        if (!cancelled) setRoutes(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err?.message ?? 'Failed to load routes');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return routes.filter((route) => {
      const matchesStatus = statusFilter === 'ALL' || route.status === statusFilter;
      const matchesQuery =
        q.length === 0 ||
        route.name.toLowerCase().includes(q) ||
        (route.description ?? '').toLowerCase().includes(q);
      return matchesStatus && matchesQuery;
    });
  }, [routes, query, statusFilter]);

  return (
    <div className="mx-auto max-w-5xl space-y-6 p-6">
      <header className="flex flex-col gap-2">
        <h1 className="text-2xl font-semibold">Routes</h1>
        <p className="text-sm text-muted-foreground">
          Browse the routes you have access to. Click one to see its legs and details.
        </p>
      </header>

      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <Input
          placeholder="Search by name or description"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="md:max-w-sm"
        />

        <div className="flex flex-wrap gap-2">
          {STATUS_FILTERS.map((filter) => {
            const active = statusFilter === filter.value;
            return (
              <button
                key={filter.value}
                type="button"
                onClick={() => setStatusFilter(filter.value)}
                className={
                  'rounded-full border px-3 py-1 text-xs font-medium transition-colors ' +
                  (active
                    ? 'border-primary bg-primary text-primary-foreground'
                    : 'border-border bg-background text-muted-foreground hover:bg-muted')
                }
              >
                {filter.label}
              </button>
            );
          })}
        </div>
      </div>

      {loading && (
        <div className="space-y-4">
          <span>Is Loading...</span>
        </div>
      )}

      {!loading && error && (
        <div className="rounded-md border border-[--color-error]/30 bg-[--color-error]/10 p-4 text-sm text-[--color-error]">
          {error}
        </div>
      )}

      {!loading && !error && filtered.length === 0 && (
        <div className="rounded-md border border-border bg-muted/40 p-8 text-center text-sm text-muted-foreground">
          {routes.length === 0
            ? 'No routes yet.'
            : 'No routes match your filters.'}
        </div>
      )}

      {!loading && !error && filtered.length > 0 && (
        <div className="space-y-4">
          {filtered.map((route) => (
            <RouteListCard key={route.id} route={route} />
          ))}
        </div>
      )}
    </div>
  );
}
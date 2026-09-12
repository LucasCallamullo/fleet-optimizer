import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { routesApi } from '../api/routesApi';
import { RouteSummaryCard } from '../components/RouteSummaryCard';
import { RouteLegCard } from '../components/RouteLegCard';
import type { RouteDetail } from '../types/routeCreateTypes';

export default function RouteDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [route, setRoute] = useState<RouteDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    let cancelled = false;
    setLoading(true);
    setError(null);

    routesApi
      .getById(id)
      .then((data) => {
        if (!cancelled) setRoute(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err?.message ?? 'Failed to load route');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) {
    return (
      <div className="mx-auto max-w-5xl space-y-6 p-6">
        <span>Loading</span>
      </div>
    );
  }

  if (error || !route) {
    return (
      <div className="mx-auto max-w-5xl p-6">
        <div className="rounded-md border border-[--color-error]/30 bg-[--color-error]/10 p-4 text-sm text-[--color-error]">
          {error ?? 'Route not found'}
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6 p-6">
      <RouteSummaryCard route={route} />

      <section className="space-y-4">
        <h2 className="text-lg font-semibold">Legs</h2>
        <div className="space-y-4">
          {route.legs.map((leg) => (
            <RouteLegCard key={leg.id} leg={leg} />
          ))}
        </div>
      </section>
    </div>
  );
}
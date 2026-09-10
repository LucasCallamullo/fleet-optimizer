import { useNavigate } from 'react-router-dom';
import { usePackages } from '../hooks/usePackages';
import PackagesTable from '../components/PackagesTable';
import type { PackageDetailDTO } from '../types/packageTypes';
import { Package, RefreshCw } from 'lucide-react';

export default function PackagesPage() {
  const navigate = useNavigate();
  const { packages, loading, error, fetchPackages } = usePackages();

  const handleSelectPackage = (pkg: PackageDetailDTO): void => {
    navigate(`/packages/${pkg.id}`);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6 text-foreground">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <Package className="h-6 w-6 text-primary" />
            Packages
          </h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            Manage and track your packages
          </p>
        </div>

        <button
          type="button"
          onClick={() => fetchPackages()}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-foreground bg-card hover:bg-muted border border-border rounded-lg transition-colors disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 text-muted-foreground ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Table */}
      <PackagesTable
        packages={packages}
        loading={loading}
        error={error}
        onSelectPackage={handleSelectPackage}
      />
    </div>
  );
}
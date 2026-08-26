// src/features/packages/pages/PackagesPage.jsx
import { useNavigate } from 'react-router-dom';
import { usePackages } from '../hooks/usePackages';
import PackagesTable from '../components/PackagesTable';
import { Package, RefreshCw } from 'lucide-react';

export default function PackagesPage() {
  const navigate = useNavigate();
  const { packages, loading, error, fetchPackages } = usePackages();

  const handleSelectPackage = (pkg) => {
    navigate(`/packages/${pkg.id}`);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <Package className="h-6 w-6 text-blue-500" />
            Packages
          </h1>
          <p className="text-sm text-gray-500 mt-0.5">
            Manage and track your packages
          </p>
        </div>
        <button
          onClick={() => fetchPackages()}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-800 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
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
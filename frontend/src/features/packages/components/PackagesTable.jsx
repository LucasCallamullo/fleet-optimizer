// src/features/packages/components/PackagesTable.jsx
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/shared/components/ui/table';
import { Package, Weight, Box, Store, User, Calendar } from 'lucide-react';
import PackageStatusBadge from './PackageStatusBadge';

export default function PackagesTable({ packages, loading, error, onSelectPackage }) {
  if (loading) {
    return (
      <Card className="border-gray-200 shadow-sm">
        <CardHeader>
          <CardTitle className="text-gray-800">Packages</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-gray-500 text-center py-8 animate-pulse">
            Loading packages...
          </p>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="border-gray-200 shadow-sm">
        <CardHeader>
          <CardTitle className="text-gray-800">Packages</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-red-600 text-center py-8 font-medium">
            {error}
          </p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="border-gray-200 shadow-sm">
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-gray-800">Packages</CardTitle>
        <span className="text-sm text-gray-500">
          {packages.length} {packages.length === 1 ? 'package' : 'packages'}
        </span>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-gray-50">
                <TableHead className="font-semibold text-gray-600">ID</TableHead>
                <TableHead className="font-semibold text-gray-600">Tracking</TableHead>
                <TableHead className="font-semibold text-gray-600">Store</TableHead>
                <TableHead className="font-semibold text-gray-600">Weight</TableHead>
                <TableHead className="font-semibold text-gray-600">Volume</TableHead>
                <TableHead className="font-semibold text-gray-600">Owner</TableHead>
                <TableHead className="font-semibold text-gray-600">Status</TableHead>
                <TableHead className="text-right font-semibold text-gray-600">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {packages.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="text-center text-gray-400 py-8">
                    No packages found
                  </TableCell>
                </TableRow>
              ) : (
                packages.map((pkg) => (
                  <TableRow key={pkg.id} className="hover:bg-gray-50 transition-colors">
                    <TableCell className="font-mono text-xs text-gray-500">
                      #{pkg.id}
                    </TableCell>
                    <TableCell className="font-mono text-sm font-medium text-gray-800">
                      {pkg.trackingNumber}
                    </TableCell>
                    <TableCell className="text-gray-700">
                      <div className="flex items-center gap-1.5">
                        <Store className="h-3.5 w-3.5 text-gray-400" />
                        <span className="text-sm">{pkg.store?.name || '—'}</span>
                      </div>
                    </TableCell>
                    <TableCell className="text-gray-700">
                      <div className="flex items-center gap-1.5">
                        <Weight className="h-3.5 w-3.5 text-gray-400" />
                        <span>{pkg.totalWeightKg} kg</span>
                      </div>
                    </TableCell>
                    <TableCell className="text-gray-700">
                      <div className="flex items-center gap-1.5">
                        <Box className="h-3.5 w-3.5 text-gray-400" />
                        <span>{pkg.totalVolumeCbm} m³</span>
                      </div>
                    </TableCell>
                    <TableCell className="text-gray-700">
                      <div className="flex items-center gap-1.5">
                        <User className="h-3.5 w-3.5 text-gray-400" />
                        <span className="font-mono text-xs truncate max-w-[100px]">
                          {pkg.ownerId?.substring(0, 8)}...
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <PackageStatusBadge status={pkg.status} />
                    </TableCell>
                    <TableCell className="text-right">
                      <button
                        onClick={() => onSelectPackage?.(pkg)}
                        className="text-sm text-blue-600 hover:text-blue-800 font-medium transition-colors"
                      >
                        View
                      </button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
}
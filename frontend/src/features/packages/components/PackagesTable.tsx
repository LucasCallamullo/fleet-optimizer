import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/shared/components/ui/table';
import { Weight, Box, Store, User } from 'lucide-react';
import type { PackageDetailDTO } from '../types/packageTypes';
import PackageStatusBadge from './PackageStatusBadge';

// ================================================================
// COMPONENT PROPS INTERFACE
// ================================================================

export interface PackagesTableProps {
  packages: PackageDetailDTO[];
  loading: boolean;
  error: string | null;
  onSelectPackage?: (pkg: PackageDetailDTO) => void;
}

// ================================================================
// COMPONENT
// ================================================================

export default function PackagesTable({
  packages,
  loading,
  error,
  onSelectPackage,
}: PackagesTableProps) {
  /**
   * STATE 1: LOADING
   */
  if (loading) {
    return (
      <Card className="border-border bg-card shadow-sm">
        <CardHeader>
          <CardTitle className="text-foreground">Packages</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground text-center py-8 animate-pulse">
            Loading packages...
          </p>
        </CardContent>
      </Card>
    );
  }

  /**
   * STATE 2: ERROR
   */
  if (error) {
    return (
      <Card className="border-border bg-card shadow-sm">
        <CardHeader>
          <CardTitle className="text-foreground">Packages</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-destructive text-center py-8 font-medium">
            ⚠️ {error}
          </p>
        </CardContent>
      </Card>
    );
  }

  /**
   * STATE 3: SUCCESS - RENDER TABLE
   */
  return (
    <Card className="border-border bg-card shadow-sm">
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-foreground">Packages</CardTitle>
        <span className="text-sm text-muted-foreground">
          {packages.length} {packages.length === 1 ? 'package' : 'packages'}
        </span>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/50 border-border">
                <TableHead className="font-semibold text-muted-foreground">ID</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Tracking</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Store</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Weight</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Volume</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Owner</TableHead>
                <TableHead className="font-semibold text-muted-foreground">Status</TableHead>
                <TableHead className="text-right font-semibold text-muted-foreground">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {packages.length === 0 ? (
                <TableRow className="border-border">
                  <TableCell colSpan={8} className="text-center text-muted-foreground py-8">
                    No packages found
                  </TableCell>
                </TableRow>
              ) : (
                packages.map((pkg) => (
                  <TableRow key={pkg.id} className="hover:bg-muted/30 border-border transition-colors">
                    {/* ID */}
                    <TableCell className="font-mono text-xs text-muted-foreground">
                      #{pkg.id}
                    </TableCell>

                    {/* Tracking Number */}
                    <TableCell className="font-mono text-sm font-medium text-foreground">
                      {pkg.trackingNumber}
                    </TableCell>

                    {/* Store */}
                    <TableCell className="text-foreground">
                      <div className="flex items-center gap-1.5">
                        <Store className="h-3.5 w-3.5 text-muted-foreground" />
                        <span className="text-sm">{pkg.store?.name || '—'}</span>
                      </div>
                    </TableCell>

                    {/* Weight */}
                    <TableCell className="text-foreground">
                      <div className="flex items-center gap-1.5">
                        <Weight className="h-3.5 w-3.5 text-muted-foreground" />
                        <span>{pkg.totalWeightKg} kg</span>
                      </div>
                    </TableCell>

                    {/* Volume */}
                    <TableCell className="text-foreground">
                      <div className="flex items-center gap-1.5">
                        <Box className="h-3.5 w-3.5 text-muted-foreground" />
                        <span>{pkg.totalVolumeCbm} m³</span>
                      </div>
                    </TableCell>

                    {/* Owner */}
                    <TableCell className="text-foreground">
                      <div className="flex items-center gap-1.5">
                        <User className="h-3.5 w-3.5 text-muted-foreground" />
                        <span className="font-mono text-xs truncate max-w-25">
                          {pkg.ownerId ? `${pkg.ownerId.substring(0, 8)}...` : '—'}
                        </span>
                      </div>
                    </TableCell>

                    {/* Status Badge */}
                    <TableCell>
                      <PackageStatusBadge status={pkg.status} />
                    </TableCell>

                    {/* Actions */}
                    <TableCell className="text-right">
                      <button
                        type="button"
                        onClick={() => onSelectPackage?.(pkg)}
                        className="text-sm text-primary hover:text-primary/80 font-medium transition-colors"
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
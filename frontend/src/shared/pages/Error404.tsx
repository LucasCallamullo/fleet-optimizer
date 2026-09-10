// src/shared/pages/Error404.tsx
import { Link } from 'react-router-dom';
import { Button } from '@/shared/components/ui/button';
import { AlertTriangle, Home, ArrowLeft } from 'lucide-react';

export default function Error404() {
  return (
    <div className="min-h-[80vh] flex flex-col items-center justify-center text-center px-4 py-12">
      <div className="w-full max-w-md space-y-6">
        {/* Icon Badge */}
        <div className="inline-flex items-center justify-center p-4 bg-error/10 border border-error/20 rounded-full text-error mb-2 animate-bounce">
          <AlertTriangle className="h-12 w-12" />
        </div>

        {/* Header Text */}
        <div className="space-y-2">
          <h1 className="text-6xl font-extrabold tracking-tight text-foreground">
            404
          </h1>
          <h2 className="text-2xl font-bold text-foreground">
            Page Not Found
          </h2>
          <p className="text-sm text-muted-foreground max-w-sm mx-auto">
            The section or resource you are looking for does not exist or has been moved within the system.
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-4">
          <Button asChild variant="default" className="w-full sm:w-auto">
            <Link to="/">
              <Home className="mr-2 h-4 w-4" />
              Back to Home
            </Link>
          </Button>

          <Button
            variant="outline"
            className="w-full sm:w-auto"
            onClick={() => window.history.back()}
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Go Back
          </Button>
        </div>
      </div>
    </div>
  );
}
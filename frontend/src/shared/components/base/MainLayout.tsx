import type { PropsWithChildren } from 'react';
import Navbar from '@/shared/components/base/NavBar';

export default function MainLayout({ children }: PropsWithChildren) {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <Navbar />
      <main className="container mx-auto px-4 py-6">
        {children}
      </main>
    </div>
  );
}
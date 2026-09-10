import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Menu, Sun, Moon, Laptop, Palette } from 'lucide-react';
import {
  NavigationMenu,
  NavigationMenuItem,
  NavigationMenuList,
  navigationMenuTriggerStyle,
} from '@/shared/components/ui/navigation-menu';
import { Sheet, SheetContent, SheetTrigger } from '@/shared/components/ui/sheet';
import { Button } from '@/shared/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import { useTheme, type Mode, type BrandTheme } from '@/shared/theme/ThemeProvider';

interface NavItem {
  path: string;
  label: string;
}

export default function Navbar() {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const { mode, setMode, theme, setTheme } = useTheme();

  const navItems: NavItem[] = [
    { path: '/', label: 'Inicio' },
    { path: '/vehicles', label: 'Gestión de Vehículos' },
  ];

  return (
    <nav className="border-b border-border bg-card px-4 py-3 shadow-sm text-card-foreground">
      <div className="container mx-auto flex items-center justify-between">
        {/* Brand Logo */}
        <Link to="/" className="text-xl font-bold text-primary transition-colors hover:opacity-90">
          FleetOptimizer
        </Link>

        {/* Desktop Navigation */}
        <div className="hidden md:flex md:items-center md:gap-6">
          <NavigationMenu>
            <NavigationMenuList>
              {navItems.map((item) => (
                <NavigationMenuItem key={item.path}>
                  <Link to={item.path} className={navigationMenuTriggerStyle()}>
                    {item.label}
                  </Link>
                </NavigationMenuItem>
              ))}
            </NavigationMenuList>
          </NavigationMenu>

          {/* Theme & Mode Controls (Desktop) */}
          <ThemeSelectorDropdown mode={mode} setMode={setMode} theme={theme} setTheme={setTheme} />
        </div>

        {/* Mobile Controls */}
        <div className="flex items-center gap-2 md:hidden">
          <ThemeSelectorDropdown mode={mode} setMode={setMode} theme={theme} setTheme={setTheme} />

          <Sheet open={isOpen} onOpenChange={setIsOpen}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon">
                <Menu className="h-6 w-6" />
                <span className="sr-only">Abrir menú</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="right" className="w-62.5 bg-card text-card-foreground">
              <div className="flex flex-col gap-4 mt-8">
                {navItems.map((item) => (
                  <Link
                    key={item.path}
                    to={item.path}
                    className="text-lg font-medium hover:text-primary transition-colors"
                    onClick={() => setIsOpen(false)}
                  >
                    {item.label}
                  </Link>
                ))}
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </div>
    </nav>
  );
}

// ================================================================
// THEME SELECTOR DROPDOWN COMPONENT
// ================================================================

interface ThemeSelectorProps {
  mode: Mode;
  setMode: (mode: Mode) => void;
  theme: BrandTheme;
  setTheme: (theme: BrandTheme) => void;
}

function ThemeSelectorDropdown({ mode, setMode, theme, setTheme }: ThemeSelectorProps) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="relative">
          <Sun className="h-[1.2rem] w-[1.2rem] rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
          <Moon className="absolute h-[1.2rem] w-[1.2rem] rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
          <span className="sr-only">Cambiar tema</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-48 bg-popover text-popover-foreground">
        {/* Mode Section */}
        <DropdownMenuLabel>Modo de Apariencia</DropdownMenuLabel>
        <DropdownMenuItem onClick={() => setMode('light')} className={mode === 'light' ? 'bg-accent font-semibold' : ''}>
          <Sun className="mr-2 h-4 w-4" />
          <span>Claro</span>
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => setMode('dark')} className={mode === 'dark' ? 'bg-accent font-semibold' : ''}>
          <Moon className="mr-2 h-4 w-4" />
          <span>Oscuro</span>
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => setMode('system')} className={mode === 'system' ? 'bg-accent font-semibold' : ''}>
          <Laptop className="mr-2 h-4 w-4" />
          <span>Sistema</span>
        </DropdownMenuItem>

        <DropdownMenuSeparator />

        {/* Brand Accent Section */}
        <DropdownMenuLabel>Color de Marca</DropdownMenuLabel>
        <DropdownMenuItem onClick={() => setTheme('violet')} className={theme === 'violet' ? 'bg-accent font-semibold' : ''}>
          <Palette className="mr-2 h-4 w-4 text-purple-500" />
          <span>Violeta</span>
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => setTheme('blue')} className={theme === 'blue' ? 'bg-accent font-semibold' : ''}>
          <Palette className="mr-2 h-4 w-4 text-blue-500" />
          <span>Azul</span>
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => setTheme('orange')} className={theme === 'orange' ? 'bg-accent font-semibold' : ''}>
          <Palette className="mr-2 h-4 w-4 text-orange-500" />
          <span>Naranja</span>
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => setTheme('default')} className={theme === 'default' ? 'bg-accent font-semibold' : ''}>
          <Palette className="mr-2 h-4 w-4 text-gray-500" />
          <span>Gris Neutro</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
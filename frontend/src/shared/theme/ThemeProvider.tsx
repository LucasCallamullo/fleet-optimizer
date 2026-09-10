import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'

export type Mode = 'light' | 'dark' | 'system'
export type BrandTheme = 'default' | 'violet' | 'blue' | 'orange'

interface ThemeContextType {
  mode: Mode
  setMode: (mode: Mode) => void
  theme: BrandTheme
  setTheme: (theme: BrandTheme) => void
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined)

const STORAGE_KEY_MODE = 'app-theme-mode'
const STORAGE_KEY_BRAND = 'app-brand-theme'

interface ThemeProviderProps {
  children: ReactNode
  defaultMode?: Mode
  defaultTheme?: BrandTheme
}

export function ThemeProvider({
  children,
  defaultMode = 'system',
  defaultTheme = 'violet',
}: ThemeProviderProps) {
  const [mode, setMode] = useState<Mode>(
    () => (localStorage.getItem(STORAGE_KEY_MODE) as Mode) || defaultMode
  )

  const [theme, setTheme] = useState<BrandTheme>(
    () => (localStorage.getItem(STORAGE_KEY_BRAND) as BrandTheme) || defaultTheme
  )

  useEffect(() => {
    const root = document.documentElement

    // 1. Manejo del Modo (Light / Dark / System)
    const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    const isDark = mode === 'dark' || (mode === 'system' && systemPrefersDark)

    if (isDark) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }

    localStorage.setItem(STORAGE_KEY_MODE, mode)
  }, [mode])

  useEffect(() => {
    const root = document.documentElement

    // 2. Manejo del Tema de Marca (Atributo data-theme)
    root.setAttribute('data-theme', theme)
    localStorage.setItem(STORAGE_KEY_BRAND, theme)
  }, [theme])

  return (
    <ThemeContext.Provider value={{ mode, setMode, theme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  )
}

// Custom Hook seguro
export function useTheme() {
  const context = useContext(ThemeContext)
  if (!context) {
    throw new Error('useTheme debe ser usado dentro de un <ThemeProvider />')
  }
  return context
}
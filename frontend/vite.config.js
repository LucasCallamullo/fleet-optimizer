import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from "path"

/**
 * Vite Configuration
 * 
 * Configures React fast refresh, Tailwind CSS v4, local dev server settings,
 * and modular alias resolutions matching tsconfig paths.
 * 
 * @see https://vite.dev/config/
 */
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(), // Integrated Tailwind CSS v4 engine
  ],
  server: {
    open: true, // Automatically open application in browser on dev server start
  },
  resolve: {
    alias: {
      /**
       * Root Application Alias
       */
      "@": path.resolve(__dirname, "./src"),
      
      /**
       * Domain-driven Modules (Features)
       */
      "@features": path.resolve(__dirname, "./src/features"),

      /**
       * Cross-cutting Utilities and Shared Resources
       */
      "@shared": path.resolve(__dirname, "./src/shared"),
      
      /**
       * Specific Design System & Component Aliases (matching components.json)
       */
      "@components": path.resolve(__dirname, "./src/shared/components"),
      "@ui": path.resolve(__dirname, "./src/shared/components/ui"),
      "@lib": path.resolve(__dirname, "./src/shared/lib"),
      "@hooks": path.resolve(__dirname, "./src/shared/hooks"),
      "@api": path.resolve(__dirname, "./src/shared/api"),
      "@types": path.resolve(__dirname, "./src/shared/types"),
    },
  },
});
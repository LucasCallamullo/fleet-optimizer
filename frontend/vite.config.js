import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from "path"


// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(), // Tailwind v4 plugin
  ],
  server: {
    // port: 3000,          // cambiar el puerto (default 5173)
    open: true, // Abre el navegador automáticamente
  },
  resolve: {
    alias: {
      // Alias principal
      "@": path.resolve(__dirname, "./src"),
      
      // Alias para features (módulos de negocio)
      "@features": path.resolve(__dirname, "./src/features"),
      "@auth": path.resolve(__dirname, "./src/features/auth"),
      "@vehicles": path.resolve(__dirname, "./src/features/vehicles"),
      "@reservations": path.resolve(__dirname, "./src/features/reservations"),
      "@admin": path.resolve(__dirname, "./src/features/admin"),
      
      // Alias para shared (recursos compartidos)
      "@shared": path.resolve(__dirname, "./src/shared"),
      
      // Alias específicos para shadcn/ui (basados en components.json)
      "@components": path.resolve(__dirname, "./src/shared/components"),
      "@ui": path.resolve(__dirname, "./src/shared/components/ui"),
      "@lib": path.resolve(__dirname, "./src/shared/lib"),  // ← Usas lib, no utils
      "@hooks": path.resolve(__dirname, "./src/shared/hooks"),
      "@api": path.resolve(__dirname, "./src/shared/api"),
      "@types": path.resolve(__dirname, "./src/shared/types"),
    },
  },
});

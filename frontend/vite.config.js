import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from "path"


// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(), // <-- Agregamos el plugin de Tailwind v4 acá
  ],
  server: {
    // port: 3000,          // cambiar el puerto (default 5173)
    open: true,          // abre el navegador automáticamente
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  /* / Para resolver imports sin tener que poner ../../../../
  resolve: {
    alias: {
      '@': '/src',       // ahora podés importar desde '@/components/...'
      '@components': '/src/components',
    }
  } */
})

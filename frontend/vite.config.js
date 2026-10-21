import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Serwer deweloperski Vite (http://localhost:5173) przekazuje zapytania /api/... do właściwego serwisu.
// Dzięki temu przeglądarka rozmawia tylko z jednym adresem i nie ma problemu z CORS.
// Na produkcji to samo zrobi Caddy (Krok 13).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // host: true = serwer widoczny też w sieci domowej, więc otworzysz apkę na telefonie (12.8).
    host: true,
    proxy: {
      '/api/auth': 'http://localhost:8081',
      '/api/households': 'http://localhost:8081',
      '/api/planner': 'http://localhost:8082',
      '/api/notifications': 'http://localhost:8083',
    },
  },
})

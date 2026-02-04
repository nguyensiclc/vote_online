import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// Vite configuration for Vue 3 frontend.
// Dev server proxies /api calls to the Spring Boot backend at http://localhost:8080.
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});


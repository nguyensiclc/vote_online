import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// Vite configuration for Vue 3 frontend.
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081/vote-online',
        changeOrigin: true
      }
    }
  }
});


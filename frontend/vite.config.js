import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8087',
        changeOrigin: true
      },
      '/api/shop': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/menu': {
        target: 'http://localhost:8181',
        changeOrigin: true
      },
      '/api/queue': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      '/api/order': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/payment': {
        target: 'http://localhost:8084',
        changeOrigin: true
      },
      '/api/notification': {
        target: 'http://localhost:8086',
        changeOrigin: true
      }
    }
  }
})

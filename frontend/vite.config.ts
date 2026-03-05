import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    // 禁用缓存解决304问题
    headers: {
      'Cache-Control': 'no-cache, no-store, must-revalidate',
      'Pragma': 'no-cache',
      'Expires': '0'
    },
    proxy: {
      '/api': {
        target: process.env.VITE_BACKEND_URL || 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path,
      }
    },
  }
  ,
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/vue') || id.includes('node_modules/@vue') || id.includes('node_modules/vue-router') || id.includes('node_modules/pinia') || id.includes('node_modules/vue-i18n')) return 'vendor-vue'
          if (id.includes('node_modules/@heroicons')) return 'vendor-icons'
          if (id.includes('node_modules/zrender')) return 'vendor-echarts-zrender'
          if (id.includes('node_modules/echarts')) return 'vendor-echarts-core'
          if (id.includes('node_modules/rive')) return 'vendor-rive'
          if (id.includes('node_modules/jspdf')) return 'vendor-jspdf'
          if (id.includes('node_modules/html2canvas')) return 'vendor-html2canvas'
          if (id.includes('node_modules/jszip')) return 'vendor-jszip'
          if (id.includes('node_modules/gsap') || id.includes('node_modules/lenis')) return 'vendor-motion'
          if (id.includes('node_modules/markdown-it') || id.includes('node_modules/dompurify') || id.includes('node_modules/katex')) return 'vendor-content'
        }
      }
    }
  }
})

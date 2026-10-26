import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // All /api requests from the React dev server are forwarded to Spring Boot.
      // This means CORS is handled by the proxy in development — you won't see
      // CORS errors in the browser console even if the backend CORS config is wrong.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // Uncomment to debug proxied requests:
        // configure: (proxy) => { proxy.on('proxyReq', (req) => console.log(req.path)) }
      },
    },
  },
})

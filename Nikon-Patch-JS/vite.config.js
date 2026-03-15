import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const renameIndexPlugin = (newFilename) => {
  if (!newFilename) return;
  return {
    name: 'renameIndex',
    enforce: 'post', // 'post' ensures it runs after other build steps
    generateBundle(options, bundle) {
      const indexHtml = bundle['index.html'];
      if (indexHtml) {
        indexHtml.fileName = newFilename;
      }
    },
  };
};

export default defineConfig({
  base: '/nikon-patch/',
  test: {
    environment: 'jsdom',
  },
  server: {
    host: '0.0.0.0',
  },  
  plugins: [
    react(),
    renameIndexPlugin('nikon-patch.html'), // Replace with your desired filename
  ],
})

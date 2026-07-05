/// <reference types="vite/client" />

// Type your environment variables here
// If you add a new VITE_ variable, add it here too
// TypeScript will catch typos at compile time
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

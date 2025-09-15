/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_ORDER_SERVICE_URL: string
  readonly VITE_INVENTORY_SERVICE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

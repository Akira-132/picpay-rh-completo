import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// O endereço da API é gravado no bundle durante o build. Sem VITE_API_URL, o site
// publicado tentaria falar com http://localhost:8080; com http://, o navegador
// bloquearia as chamadas feitas a partir da página https. No Render (que define
// RENDER=true no build), esses casos falham o build em vez de publicar um site quebrado.
function validarApiUrlNoRender(mode: string) {
  if (!process.env.RENDER) return

  const apiUrl = loadEnv(mode, process.cwd(), 'VITE_').VITE_API_URL?.trim()
  if (!apiUrl) {
    throw new Error(
      'VITE_API_URL não está definida. Cadastre-a em Environment no Static Site do Render ' +
        '(ex.: https://seu-backend.onrender.com) e faça o deploy de novo.',
    )
  }

  let url: URL
  try {
    url = new URL(apiUrl)
  } catch {
    throw new Error(`VITE_API_URL não é uma URL válida: "${apiUrl}".`)
  }
  if (url.protocol !== 'https:') {
    throw new Error(
      `VITE_API_URL precisa usar https:// (valor atual: "${apiUrl}"). ` +
        'O navegador bloqueia chamadas http feitas a partir de uma página https.',
    )
  }
}

export default defineConfig(({ command, mode }) => {
  if (command === 'build') validarApiUrlNoRender(mode)

  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
  }
})

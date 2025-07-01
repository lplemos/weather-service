# Weather App

Uma aplicação React moderna para consultar o tempo em qualquer cidade do mundo.

## Características

- 🌤️ Tempo atual detalhado
- 📅 Previsão para 5 dias
- 🎨 Interface moderna com gradiente verde
- 📱 Design responsivo
- ⚡ Carregamento rápido com Vite
- 🔍 Pesquisa de cidades

## Tecnologias

- React 18
- TypeScript
- Vite
- Tailwind CSS
- Lucide React (ícones)
- Axios (HTTP client)

## Desenvolvimento

```bash
# Instalar dependências
npm install

# Iniciar servidor de desenvolvimento
npm run dev

# Build para produção
npm run build

# Preview do build
npm run preview
```

## Variáveis de Ambiente

Criar um arquivo `.env.local`:

```env
# API URL - para produção, usar o subdomain da API
VITE_API_URL=https://api.lplemos.com

# Para desenvolvimento local
# VITE_API_URL=http://localhost:8080
```

## Deploy

A app está configurada para ser deployada no Vercel no subpath `/weather`.

### Configuração do Vercel

- Framework: Vite
- Build Command: `npm run build`
- Output Directory: `dist`
- Base Path: `/weather`

### Configuração do Domínio

A app será acessível em `lplemos.com/weather` após o deploy.

## API Endpoints

A app consome os seguintes endpoints do Weather Service:

- `GET /api/v1/weather/current/{city}` - Tempo atual
- `GET /api/v1/weather/forecast/{city}` - Previsão 5 dias
- `GET /api/v1/weather/summary/{city}` - Resumo do tempo

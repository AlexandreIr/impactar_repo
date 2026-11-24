import type { NextConfig } from "next";

// O front faz proxy de /api/* para o backend: mesma origem para o navegador,
// cookie httpOnly funciona em dev e produção e não há CORS para configurar.
// API_URL é lida no build — em produção, defina-a antes de rodar o build.
const apiUrl = process.env.API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${apiUrl}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;

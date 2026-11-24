# Deploy — impactar (Render + Vercel + Neon)

Ordem: **banco → API (Render) → front (Vercel) → ajustar FRONTEND_URL → seed do admin**.

## 1. Banco de produção (Neon)

Escolha uma opção:

- **A (recomendada):** novo projeto Neon na região **AWS US East (N. Virginia)**, a mesma do Render.
  Cada consulta fica ~1 ms em vez de ~120 ms (Render não tem região no Brasil).
- **B:** branch `main` no projeto atual (São Paulo). Funciona, mas as telas ficam mais lentas.

Use a conexão **direta** (sem `-pooler`). O Flyway cria as tabelas no primeiro start da API.

## 2. API no Render

New → **Web Service** → conecte o repositório.

| Campo | Valor |
|---|---|
| Root Directory | `backend` |
| Runtime | Docker |
| Region | Virginia (US East) |
| Health Check Path | `/api/actuator/health` |

Variáveis de ambiente:

```properties
DB_URL=jdbc:postgresql://ep-xxxx.us-east-1.aws.neon.tech/impactar?sslmode=require
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=...            # NOVO segredo: openssl rand -base64 48
COOKIE_SECURE=true
FRONTEND_URL=https://SEU-APP.vercel.app    # ajuste após o passo 3
BREVO_SMTP_LOGIN=...
BREVO_SMTP_KEY=...
BREVO_FROM=...
GMAIL_USERNAME=...
GMAIL_APP_PASSWORD=...
```

Teste: `https://SEU-SERVICO.onrender.com/api/actuator/health` → `{"status":"UP"}`

## 3. Front na Vercel

New Project → importe o repositório.

| Campo | Valor |
|---|---|
| Root Directory | `frontend` |
| Framework | Next.js (automático) |

Variável de ambiente (Production): `API_URL=https://SEU-SERVICO.onrender.com`

> `API_URL` é lida no **build** (rewrites). Se mudar, faça **Redeploy**.

Depois do deploy, volte ao Render, ajuste `FRONTEND_URL` com a URL da Vercel e salve (reinicia a API).

## 4. Primeiro administrador

```bash
read -rs ADMIN_PASSWORD
psql "postgresql://USUARIO:SENHA@ep-xxxx.us-east-1.aws.neon.tech/impactar?sslmode=require" \
  -v ON_ERROR_STOP=1 -v admin_name="Administrador" -v admin_email="admin@impactar.com.br" \
  -v admin_password="$ADMIN_PASSWORD" -f db/seed/seed_admin.sql
```

## 5. Antes da apresentação (plano free do Render)

A API "dorme" após ~15 min sem uso e leva ~1 min para acordar.
**10 minutos antes**, abra no navegador `https://SEU-SERVICO.onrender.com/api/actuator/health`
e espere o `UP`. Só então abra o front (a Vercel desiste da requisição antes de a API acordar).
Mantenha uma aba da plataforma em uso até o início da reunião.

## Checklist de verificação

- [ ] Login do admin → troca de senha → `/admin/usuarios`
- [ ] Criar usuário → e-mail chega com link da Vercel
- [ ] Curso publicado aparece no catálogo do aluno
- [ ] Assistir aula, sair e voltar → vídeo retoma do ponto

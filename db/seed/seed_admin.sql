-- =====================================================================
-- seed_admin.sql — cria o primeiro Administrador (idempotente)
-- Destino: db/seed/seed_admin.sql
-- NÃO é migration: roda uma única vez, manualmente, após o Flyway aplicar a V1.
--
-- Uso (a senha não fica no arquivo nem no git):
--   read -rs ADMIN_PASSWORD
--   psql "$DATABASE_URL" -v ON_ERROR_STOP=1 \
--     -v admin_name="Administrador" \
--     -v admin_email="admin@seudominio.sp.gov.br" \
--     -v admin_password="$ADMIN_PASSWORD" \
--     -f db/seed/seed_admin.sql
--
-- Hash: bcrypt ($2a$, custo 10) via pgcrypto — compatível com
--   `new BCryptPasswordEncoder()` do Spring Security.
--   NÃO usar PasswordEncoderFactories.createDelegatingPasswordEncoder():
--   ele exige o prefixo {bcrypt} no hash e o login falharia.
--
-- must_change_password = TRUE força a troca de senha no primeiro login.
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (name, email, password_hash, role, must_change_password, active)
VALUES (
    'Alexandre',
    lower(trim('afsilva.tech@gmail.com')),
    crypt('123456', gen_salt('bf', 10)),
    'ADMIN',
    TRUE,
    TRUE
)
ON CONFLICT (email) DO NOTHING;

SELECT id, name, email, role, must_change_password, active, created_at
FROM users
WHERE email = lower(trim('afsilva.tech@gmail.com'));

#!/usr/bin/env bash
# =====================================================================
# migrar-pacote.sh — troca o domínio do projeto de .gov.br para .com.br
#
#   Pacote Java / groupId:  br.gov.sp.impactar  ->  br.com.impactar
#   E-mails de exemplo:     *@...gov.br         ->  *@impactar.com.br
#
# Uso (na raiz do repositório, ANTES de descompactar o pacote do instrutor):
#   bash migrar-pacote.sh
#
# Idempotente: rodar de novo não altera nada.
# =====================================================================
set -euo pipefail

if [ ! -d backend ]; then
  echo "Rode este script na raiz do repositório (pasta que contém backend/)." >&2
  exit 1
fi

OLD_DIR="br/gov/sp/impactar"
NEW_DIR="br/com/impactar"

# 1. Move os fontes para a nova estrutura de pastas
for base in backend/src/main/java backend/src/test/java; do
  if [ -d "$base/$OLD_DIR" ]; then
    mkdir -p "$base/$NEW_DIR"
    cp -R "$base/$OLD_DIR/." "$base/$NEW_DIR/"
    rm -rf "$base/$OLD_DIR"
    find "$base/br/gov" -type d -empty -delete 2>/dev/null || true
    echo "Movido: $base/$OLD_DIR -> $base/$NEW_DIR"
  fi
done

# 2. Troca as referências no código, no pom.xml e nos scripts SQL
FILES=$(grep -rlI \
  --exclude-dir=target --exclude-dir=.git --exclude-dir=node_modules --exclude=.env \
  -e 'br\.gov\.sp\.impactar' -e 'gov\.br' \
  backend db 2>/dev/null || true)

for file in $FILES; do
  perl -pi -e 's/br\.gov\.sp\.impactar/br.com.impactar/g; s/\@[A-Za-z0-9.-]*gov\.br/\@impactar.com.br/gi' "$file"
  echo "Atualizado: $file"
done

# 3. Confere se sobrou alguma referência
LEFT=$(grep -rnI --exclude-dir=target --exclude-dir=.git --exclude=.env -e 'gov\.sp' -e 'gov\.br' backend db 2>/dev/null || true)
if [ -n "$LEFT" ]; then
  echo "Atenção, referências restantes:" >&2
  echo "$LEFT" >&2
  exit 1
fi

echo "OK: pacote migrado para br.com.impactar. Rode: cd backend && ./mvnw clean test"

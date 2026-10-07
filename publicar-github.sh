#!/usr/bin/env bash
# ---------------------------------------------------------------------
# Publica este repositório (todas as branches Gitflow + tags) no GitHub.
#
# Uso:
#   ./publicar-github.sh <seu-usuario-github> [nome-do-repo]
#
# Com o GitHub CLI (gh) instalado e logado (gh auth login), o repositório
# público é criado automaticamente. Sem o gh, crie antes um repositório
# VAZIO e público em https://github.com/new com o mesmo nome.
# ---------------------------------------------------------------------
set -euo pipefail

USUARIO="${1:?Informe seu usuário do GitHub: ./publicar-github.sh <usuario> [repo]}"
REPO="${2:-login-seguro}"
URL="https://github.com/${USUARIO}/${REPO}.git"

if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
  if ! gh repo view "${USUARIO}/${REPO}" >/dev/null 2>&1; then
    gh repo create "${USUARIO}/${REPO}" --public \
      --description "Login Seguro com Spring Boot, Spring Security, Thymeleaf e MongoDB Atlas"
  fi
fi

if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "$URL"
else
  git remote add origin "$URL"
fi

# Envia todas as branches (main, develop, feature/*, release/*) e as tags
git push -u origin --all
git push origin --tags

echo
echo "Publicado em https://github.com/${USUARIO}/${REPO}"
echo "Dica: em Settings > General, defina 'main' como branch padrão se necessário."

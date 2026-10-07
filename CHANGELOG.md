# Changelog

## [1.0.0] - 2026-10-07

### Adicionado
- Cadastro, login (usuário ou e-mail) e logout com Spring Security.
- Senhas com hash BCrypt (custo 12) e política de senha forte.
- Três perfis (ADMIN, MODERADOR, USUARIO) com controle de acesso por rota e por método.
- Bloqueio temporário após tentativas de login malsucedidas.
- Usuários e sessões HTTP armazenados no MongoDB Atlas (Spring Session).
- Interface Thymeleaf com temas plugáveis (`padrao` e `escuro`) escolhidos por usuário.
- Áreas de moderação e administração de usuários.
- Criação do administrador inicial via variáveis de ambiente.
- Testes de controle de acesso, regras de negócio e bloqueio de login; CI no GitHub Actions.

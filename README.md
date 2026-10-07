# Login Seguro — Spring Boot + Spring Security + Thymeleaf + MongoDB Atlas

Sistema web de autenticação e autorização com controle completo de usuários, três perfis de acesso,
senhas criptografadas com BCrypt e usuários **e sessões** armazenados no MongoDB Atlas.
A interface usa Thymeleaf com **temas plugáveis**, separados da lógica de negócio.

> Projeto individual — Sistemas de Informação · UMC
> Autor: Felipe Lima Torres

---

## Sumário

1. [Funcionalidades](#funcionalidades)
2. [Tecnologias](#tecnologias)
3. [Arquitetura e organização](#arquitetura-e-organização)
4. [Perfis e controle de acesso](#perfis-e-controle-de-acesso)
5. [Segurança implementada](#segurança-implementada)
6. [Configurando o MongoDB Atlas](#configurando-o-mongodb-atlas)
7. [Executando localmente](#executando-localmente)
8. [Integração com o MongoDB Atlas](#integração-com-o-mongodb-atlas)
9. [Temas visuais](#temas-visuais)
10. [Testes](#testes)
11. [Gitflow](#gitflow)

---

## Funcionalidades

- Cadastro de usuários com validação (nome, usuário, e-mail, senha forte e confirmação).
- Login com **usuário ou e-mail**, logout seguro (POST + CSRF) e expiração de sessão.
- Bloqueio temporário da conta após 5 tentativas de login erradas (configurável).
- Três perfis: **ADMIN**, **MODERADOR** e **USUARIO**, cada um com suas áreas.
- Área do usuário: dados da conta, troca de senha e escolha de tema.
- Moderação: lista de contas, ativar/desativar e desbloquear usuários comuns.
- Administração: alterar perfil, ativar/desativar, desbloquear e excluir qualquer usuário
  (com travas para não excluir a si mesmo nem deixar o sistema sem administrador).
- Criação automática do primeiro administrador a partir de variáveis de ambiente.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Segurança | Spring Security 6 (form login, BCrypt, CSRF, method security) |
| Banco de dados | MongoDB Atlas via Spring Data MongoDB |
| Sessões | Spring Session MongoDB (coleção `sessions`) |
| Interface | Thymeleaf + Thymeleaf Layout Dialect + Thymeleaf Extras Spring Security |
| Validação | Jakarta Bean Validation (Hibernate Validator) |
| Testes | JUnit 5, Mockito, MockMvc, Spring Security Test |
| Build | Maven |

## Arquitetura e organização

O código é organizado **por módulo de negócio** (pacote por funcionalidade), e cada módulo é
dividido em camadas. Isso deixa o núcleo genérico: adicionar um perfil, um tema ou uma tela
não exige mexer na lógica central.

```
src/main/java/br/edu/umc/loginseguro
├── LoginSeguroApplication.java
├── bootstrap/            AdminBootstrap — cria o 1º ADMIN a partir do .env
├── config/               SecurityConfig, SessionConfig, MongoConfig
├── security/             UserDetailsService, bloqueio por tentativas, eventos de login
├── theme/                ThemeProperties, ThemeService, ThemeModelAdvice
├── user/
│   ├── domain/           User (documento) e Role (enum de perfis)
│   ├── dto/              Formulários (RegistrationForm, ChangePasswordForm) e UserView
│   ├── repository/       UserRepository (Spring Data MongoDB)
│   ├── service/          UserService (regras de negócio) e BusinessException
│   └── validation/       @FieldsMatch e política de senha
└── web/                  Controllers (Home, Auth, UserArea, Moderator, Admin)

src/main/resources
├── application.yml       Configuração (sem segredos; lê variáveis de ambiente / .env)
├── templates/
│   ├── themes/<tema>/layout.html   Layout de cada tema
│   ├── fragments/common.html       Fragmentos compartilhados (head, menu, alertas, rodapé)
│   ├── auth/  usuario/  gestao/    Páginas (só conteúdo; o layout vem do tema)
│   └── error.html
└── static/
    ├── css/base.css                Estrutura e componentes (usa variáveis CSS)
    └── themes/<tema>/theme.css     Cores, tipografia e ajustes de layout do tema
```

Fluxo de uma requisição:

```
Navegador → Spring Security (filtros: CSRF, sessão, autorização por rota)
         → Controller (web/)  → Service (@PreAuthorize)  → Repository → MongoDB Atlas
         → View Thymeleaf (página + layout do tema ativo)
```

Boas práticas aplicadas: injeção por construtor, DTOs separados das entidades (a view nunca recebe
o hash da senha), validação declarativa, mensagens de erro de negócio centralizadas, configuração
externa por variáveis de ambiente, nenhum segredo no repositório e testes automatizados.

## Perfis e controle de acesso

| Rota | Público | USUARIO | MODERADOR | ADMIN |
|---|:-:|:-:|:-:|:-:|
| `/`, `/login`, `/registro` | ✅ | ✅ | ✅ | ✅ |
| `/painel`, `/usuario/**` | ❌ | ✅ | ✅ | ✅ |
| `/moderador/**` | ❌ | ❌ | ✅ | ✅ |
| `/admin/**` | ❌ | ❌ | ❌ | ✅ |

O controle acontece em **três níveis**:

1. **Rotas** — `SecurityConfig.authorizeHttpRequests` (bloqueia a URL; resposta 403 + página “Acesso negado”).
2. **Serviço** — `@PreAuthorize` nos métodos administrativos do `UserService` (defesa em profundidade).
3. **Interface** — `sec:authorize` no Thymeleaf mostra só os menus permitidos (apenas visual; a regra real está nos níveis 1 e 2).

Regras extras: o cadastro público sempre cria `USUARIO` (não há como escolher o perfil pelo formulário);
moderador só altera contas `USUARIO`; ninguém altera/exclui a própria conta pela gestão; o último ADMIN não pode ser removido.

**Como adicionar um novo perfil:** inclua o valor no enum `Role` e libere as rotas em `SecurityConfig`.
Cadastro, listagem e troca de perfil passam a reconhecê-lo automaticamente.

## Segurança implementada

| Item | Como |
|---|---|
| Hash de senha | `BCryptPasswordEncoder` (custo 12, salt aleatório por senha) |
| Política de senha | 8–64 caracteres, maiúscula, minúscula, número e símbolo |
| Validação de dados | Bean Validation nos DTOs + verificação de duplicidade + índices únicos no MongoDB |
| CSRF | Ativado; token inserido automaticamente pelo Thymeleaf em todos os formulários |
| Session fixation | Novo ID de sessão após o login |
| Cookie de sessão | `HttpOnly`, `SameSite=Lax`, `Secure` configurável (`COOKIE_SECURE=true` em HTTPS) |
| Logout | Somente POST; invalida a sessão no MongoDB e apaga o cookie |
| Força bruta | Bloqueio temporário após N falhas (`app.security.*`) |
| Cabeçalhos | Content-Security-Policy, X-Frame-Options DENY, Referrer-Policy, X-Content-Type-Options |
| Erros | Sem stack trace na resposta; mensagem de login genérica para usuário/senha inválidos |
| Segredos | Somente em variáveis de ambiente / `.env` (ignorado pelo Git) |

## Configurando o MongoDB Atlas

1. Crie uma conta em <https://www.mongodb.com/cloud/atlas> e um cluster (o plano gratuito **M0** serve).
2. **Database Access** → *Add New Database User*: crie um usuário com senha e o papel
   *Read and write to any database* (ou restrito ao banco `login_seguro`).
3. **Network Access** → *Add IP Address*: adicione o seu IP atual (*Add Current IP Address*).
   Evite `0.0.0.0/0` fora de testes rápidos.
4. **Database** → *Connect* → *Drivers* → *Java*: copie a string `mongodb+srv://...`.
5. Cole a string no `.env` (próxima seção), trocando `<password>` pela senha do usuário.
   Caracteres especiais na senha precisam de URL encoding (`@` → `%40`, `:` → `%3A`, `/` → `%2F`).

Não é preciso criar banco nem coleções: a aplicação cria `users` e `sessions` (e seus índices) na primeira execução.

## Executando localmente

### Pré-requisitos

- JDK 21 (`java -version`)
- Maven 3.9+ (`mvn -v`) — ou abra o projeto no IntelliJ IDEA / VS Code, que já trazem o Maven
- Um cluster no MongoDB Atlas (seção anterior)

### Passo a passo

```bash
# 1. Clonar
git clone https://github.com/<seu-usuario>/login-seguro.git
cd login-seguro

# 2. Criar o arquivo de configuração a partir do modelo
cp .env.example .env          # Windows (PowerShell): Copy-Item .env.example .env
# edite o .env com a sua string do Atlas e os dados do administrador inicial

# 3. Rodar
mvn spring-boot:run
```

Acesse <http://localhost:8080>.

- Entre com o administrador definido em `ADMIN_USERNAME` / `ADMIN_PASSWORD`.
- Crie contas em **Criar conta** (elas nascem com perfil USUARIO).
- Na área **Administração**, promova uma conta a **MODERADOR** para testar os três perfis.

Para gerar o JAR executável:

```bash
mvn clean package
java -jar target/login-seguro-1.0.0.jar
```

Em vez do `.env` você pode exportar as variáveis no terminal (`export MONGODB_URI=...`) ou
configurá-las no *Run Configuration* da IDE — variáveis de ambiente têm prioridade.

### Variáveis de configuração

| Variável | Obrigatória | Descrição |
|---|:-:|---|
| `MONGODB_URI` | ✅ | String de conexão `mongodb+srv://` do Atlas |
| `MONGODB_DATABASE` | | Nome do banco (padrão `login_seguro`) |
| `ADMIN_USERNAME` / `ADMIN_EMAIL` / `ADMIN_PASSWORD` | | Cria o 1º ADMIN se ainda não houver nenhum |
| `APP_DEFAULT_THEME` | | `padrao` ou `escuro` |
| `COOKIE_SECURE` | | `true` quando estiver atrás de HTTPS |
| `PORT` | | Porta HTTP (padrão `8080`) |

### Problemas comuns

| Sintoma | Causa provável |
|---|---|
| `Could not resolve placeholder 'MONGODB_URI'` | `.env` ausente ou fora da raiz do projeto |
| `MongoTimeoutException` / timeout ao iniciar | Seu IP não está liberado em *Network Access* |
| `Authentication failed` (Mongo) | Usuário/senha do banco errados ou senha sem URL encoding |
| Log “Nenhum administrador cadastrado” | Variáveis `ADMIN_*` vazias — preencha e reinicie |

## Integração com o MongoDB Atlas

**Conexão segura.** O `application.yml` lê `spring.data.mongodb.uri` da variável `MONGODB_URI`.
O esquema `mongodb+srv://` descobre os nós do cluster via DNS e **habilita TLS automaticamente**;
o acesso ainda exige usuário/senha do banco e IP liberado na *Network Access List*. Nenhuma credencial fica no código ou no Git.

**Coleção `users`** — um documento por usuário (classe `User`):

```json
{
  "_id": "6703f1...",
  "name": "Maria Silva",
  "username": "maria",
  "email": "maria@exemplo.com",
  "passwordHash": "$2a$12$Q9...",
  "role": "USUARIO",
  "enabled": true,
  "failedLoginAttempts": 0,
  "lockedUntil": null,
  "lastLoginAt": "2026-10-07T13:00:00Z",
  "theme": "escuro",
  "createdAt": "2026-10-01T10:00:00Z",
  "updatedAt": "2026-10-07T13:00:00Z"
}
```

`username` e `email` têm **índices únicos** (`@Indexed(unique = true)` + `auto-index-creation: true`),
garantindo a regra mesmo com cadastros simultâneos.

**Coleção `sessions`** — gerenciada pelo Spring Session (`@EnableMongoHttpSession`). Cada login gera um
documento com o contexto de segurança serializado e a data de expiração; um índice TTL faz o MongoDB
apagar sessões vencidas sozinho. Como a sessão fica no banco, o usuário continua logado após reiniciar
a aplicação, e várias instâncias podem compartilhar as mesmas sessões. O logout remove o documento.

O acesso ao banco passa sempre por `UserRepository` (Spring Data), então trocar de banco ou de
coleção não afeta controllers nem telas.

## Temas visuais

Cada tema é formado por dois arquivos:

```
templates/themes/<nome>/layout.html    estrutura da página (menu no topo, lateral, etc.)
static/themes/<nome>/theme.css         variáveis de cor/tipografia e ajustes de layout
```

As páginas não conhecem o tema: todas declaram

```html
<html layout:decorate="~{__${theme.layout}__}">
```

e o `ThemeModelAdvice` injeta em `${theme}` o tema do usuário logado (ou o padrão). Já vêm dois temas:
**padrao** (claro, menu no topo) e **escuro** (menu lateral). O usuário troca em *Meu perfil*.

**Criando um tema novo** (ex.: `institucional`):

1. Copie `templates/themes/padrao/layout.html` para `templates/themes/institucional/layout.html` e ajuste a estrutura.
2. Copie `static/themes/padrao/theme.css` para `static/themes/institucional/theme.css` e troque as variáveis
   (`--primary`, `--bg`, `--font-display`...).
3. Adicione `institucional` em `app.theme.available` no `application.yml`.

Nenhum controller, serviço ou regra de segurança precisa mudar.
Temas desconhecidos caem no padrão, então um valor inválido nunca é usado para montar caminho de template.

## Testes

```bash
mvn test
```

Os testes não precisam do Atlas:

- `AccessControlTest` (MockMvc): rotas públicas, redirecionamento para login, 403 por perfil,
  CSRF obrigatório, validação do cadastro e logout.
- `UserServiceTest`: hash BCrypt, normalização, duplicidade, troca de senha e travas administrativas.
- `LoginAttemptServiceTest`: bloqueio após tentativas e reset no login bem-sucedido.

O workflow `.github/workflows/ci.yml` roda os testes a cada push e pull request.

## Gitflow

O repositório segue o modelo **Gitflow**:

| Branch | Uso |
|---|---|
| `main` | Código em produção; cada versão recebe uma tag (`v1.0.0`) |
| `develop` | Integração das funcionalidades |
| `feature/*` | Uma branch por funcionalidade, criada a partir de `develop` e integrada com `--no-ff` |
| `release/*` | Preparação de versão, integrada em `main` e `develop` |
| `hotfix/*` | Correções urgentes a partir de `main` |

Histórico da versão 1.0.0:

```
feature/estrutura-base      → develop
feature/modelo-usuario      → develop
feature/autenticacao        → develop
feature/sessoes-mongodb     → develop
feature/temas-interface     → develop
feature/controle-perfis     → develop
feature/testes              → develop
feature/documentacao        → develop
release/1.0.0               → main (tag v1.0.0) e develop
```

Para uma nova funcionalidade:

```bash
git checkout develop && git pull
git checkout -b feature/minha-funcionalidade
# ... commits ...
git checkout develop && git merge --no-ff feature/minha-funcionalidade
git push origin develop
```

# Wormless API

API REST para análise de arquivos e triagem de repositórios, com autenticação JWT,
histórico de relatórios e integração com MySQL e Gemini.

## Tecnologias

- Java 21 e Spring Boot
- Spring Data JPA e MySQL 8
- Spring Security com JWT
- Spring Cloud OpenFeign
- Maven, Docker Compose e Testcontainers

## Requisitos

- JDK 21
- Docker Desktop com Docker Compose, para executar a aplicação e os testes de integração
- Chave da API Gemini para gerar relatórios com IA

## Configuração local

Crie um arquivo `.env` na raiz do projeto:

```dotenv
JWT_SECRET=CHAVE_BASE64_COM_PELO_MENOS_32_BYTES
GEMINI_API_KEY=SUA_CHAVE_GEMINI
```

Gere um segredo JWT Base64 com OpenSSL, por exemplo:

```bash
openssl rand -base64 32
```

Suba o MySQL e a API:

```bash
docker compose up --build
```

## Build e verificações

No PowerShell, compile e empacote com:

```powershell
.\mvnw.cmd clean package
```

No Linux ou macOS:

```bash
./mvnw clean package
```

Os testes usam Testcontainers para iniciar MySQL. Se Docker não estiver disponível,
os testes de integração são pulados; com Docker ativo, eles são executados.

Para executar as verificações da esteira GitHub Actions localmente:

```bash
./mvnw validate
./mvnw compile com.github.spotbugs:spotbugs-maven-plugin:4.8.6.6:check
./mvnw test jacoco:report -Dspring.profiles.active=test
./mvnw package -DskipTests
```

No PowerShell, substitua `./mvnw` por `./mvnw.cmd`.
O relatório XML do SpotBugs é gerado em `target/spotbugsXml.xml`.


## Usar como GitHub Action em outro repositório

O workflow consumidor deve fazer checkout do código e permitir que o job solicite um token OIDC:

```yaml
name: Wormless
on: [push, pull_request]

permissions:
  contents: read
  id-token: write

jobs:
  scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v6
        with:
          persist-credentials: false
      - uses: wormless-framework/wormless-api@v1
        with:
          api-url: ${{ vars.WORMLESS_API_URL }}
          oidc-audience: api://wormless
```

Configure `WORMLESS_API_URL` como variável no repositório consumidor. O checkout usa o `GITHUB_TOKEN` de curta duração do próprio job e o token não é enviado à API Wormless. O servidor valida o token OIDC e só aceita repositórios listados em `WORMLESS_GITHUB_OIDC_ALLOWED_REPOSITORIES` (separe vários nomes `owner/repo` por vírgula). O audience configurado em `oidc-audience` deve corresponder a `WORMLESS_GITHUB_OIDC_AUDIENCE` no servidor. O endpoint aceita eventos `push`, `pull_request` e `workflow_dispatch`.

**Análise:** a Action executa uma versão fixada do OSV-Scanner no runner e envia à API apenas o SBOM CycloneDX e os metadados do commit. O scanner resolve o grafo Maven, incluindo dependências transitivas; a API não clona o repositório nem consulta o OSV novamente. A validação OIDC exige que o repositório conste em `WORMLESS_GITHUB_OIDC_ALLOWED_REPOSITORIES`. SBOM inválido, erro de scanner ou resultado com vulnerabilidades bloqueia a build.

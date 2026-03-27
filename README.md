# Azure Key Vault Reader

Aplicação desktop Java/Swing para consultar e visualizar segredos armazenados no Azure Key Vault de forma simples e segura, sem necessidade de CLI ou acesso direto ao portal Azure.

---

## Sumário

- [Finalidade](#finalidade)
- [Funcionalidades](#funcionalidades)
- [Pré-requisitos](#pré-requisitos)
- [Como baixar e buildar](#como-baixar-e-buildar)
- [Como usar](#como-usar)
- [Arquitetura](#arquitetura)
- [Histórico de versões](#histórico-de-versões)

---

## Finalidade

O **Azure Key Vault Reader** é uma ferramenta desktop voltada para desenvolvedores e times de operações que precisam consultar segredos (variáveis de ambiente, credenciais, tokens) armazenados no Azure Key Vault sem precisar acessar o portal Azure ou usar a CLI do Azure.

Com a aplicação é possível:

- Conectar-se a qualquer Key Vault informando as credenciais de acesso.
- Visualizar todos os segredos em uma tabela pesquisável.
- Copiar valores individuais ou no formato `VAR=valor` diretamente para a área de transferência.
- Manter um histórico local das conexões realizadas para acesso rápido posterior.

---

## Funcionalidades

| Funcionalidade | Descrição |
|---|---|
| Conexão via Service Principal | Autenticação com `Client ID`, `Client Secret` e `Tenant ID` |
| Listagem de segredos | Exibe todos os segredos do vault em tabela com colunas `Variável` e `Valor` |
| Filtro em tempo real | Campo de busca que filtra as variáveis enquanto você digita |
| Copiar para clipboard | Menu de contexto (botão direito) para copiar valor ou linha no formato `VAR=valor` |
| Histórico de conexões | Conexões anteriores são salvas localmente e podem ser recarregadas |
| Barra de progresso | Indicador visual durante o carregamento dos segredos |

---

## Pré-requisitos

- **Java 17+** instalado e no `PATH`
- **Maven 3.8+** (apenas para build a partir do código-fonte)
- Uma **Service Principal** no Azure com permissão de leitura no Key Vault desejado
- Os seguintes dados de acesso:
  - `Key Vault URI` — ex: `https://meu-keyvault.vault.azure.net`
  - `Client ID` — ID da aplicação registrada no Azure AD
  - `Client Secret` — segredo da aplicação
  - `Tenant ID` — ID do tenant/diretório do Azure AD

---

## Como baixar e buildar

### Opção 1 — Clonar e buildar com Maven

```bash
# Clone o repositório
git clone https://github.com/crqcastro/ReadAzureKeyVault.git
cd ReadAzureKeyVault

# Compile e empacote (gera um fat JAR com todas as dependências)
mvn clean package

# Execute a aplicação
java -jar target/ReadAzureKeyVault-1.0.0.jar
```

### Opção 2 — Baixar o JAR pré-compilado

Se o repositório disponibilizar releases, baixe o arquivo `ReadAzureKeyVault-<versão>.jar` na página de [Releases](../../releases) e execute:

```bash
java -jar ReadAzureKeyVault-1.0.0.jar
```

### Executar os testes

```bash
mvn test
```

---

## Como usar

### 1. Preencha os dados de conexão

Ao abrir a aplicação, preencha os campos do formulário com as credenciais da sua Service Principal:

| Campo | Exemplo |
|---|---|
| Key Vault URI | `https://meu-keyvault.vault.azure.net` |
| Client ID | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| Client Secret | `seu-client-secret` |
| Tenant ID | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |

> O botão **Conectar** só é habilitado após o preenchimento de todos os campos.

<!-- Screenshot: tela principal com formulário preenchido -->
<!-- Adicione a imagem em docs/screenshots/tela-conexao.png e descomente a linha abaixo -->
![Tela de conexão](assets/tela-conexao.png)

---

### 2. Clique em "Conectar e Listar Segredos"

A aplicação exibe uma barra de progresso enquanto busca os segredos. Ao concluir, a tabela é preenchida com todas as variáveis e seus valores.

---

### 3. Filtre variáveis

Use o campo **Filtrar** acima da tabela para buscar variáveis pelo nome ou valor em tempo real.

---

### 4. Copie valores para a área de transferência

Clique com o botão direito em qualquer linha da tabela para acessar o menu de contexto:

- **Copiar valor** — copia apenas o valor do segredo.
- **Copiar linha (VAR=valor)** — copia no formato pronto para uso em arquivos `.env`.

---

### 5. Use o histórico de conexões

As conexões realizadas com sucesso são salvas automaticamente. Para reutilizá-las:

1. Selecione uma entrada no campo **Histórico**.
2. Clique em **Carregar** para preencher os campos e reexibir os segredos salvos.
3. Clique em **Conectar** novamente para buscar dados atualizados do vault.

---

## Arquitetura

A aplicação foi organizada em camadas simples e explícitas seguindo princípios de arquitetura limpa:

```
br.com.cesarcastro
├── bootstrap          → Ponto de entrada e composição das dependências (Main.java)
├── presentation.ui    → Interface Swing e interação com o usuário (KeyVaultGui)
├── application
│   ├── service        → Casos de uso (KeyVaultSecretsService, SecretsEnvExporter)
│   └── port.out       → Contratos/interfaces para a camada de infraestrutura
├── domain
│   ├── model          → Modelos de domínio (KeyVaultConnectionSettings, SecretEntry, HistoryEntry)
│   └── exception      → Exceções de domínio
└── infrastructure
    ├── azure          → Integração com o SDK do Azure Key Vault
    └── persistence    → Persistência local do histórico de conexões
```

## Histórico de versões

### v1.0.1
(README)
- 
- Adicionado arquivo do github actions para geracao da tag automaticamente
- Adicionado modelo de comentario do PR
- Adicionado workflow para atualizar no READEME as alterações feitas
---
## Checklist de Qualidade
- [x] Cobertura de testes unitários validada.
- [x] Logs e monitoramento configurados.
- [x] Ajuste na versão da aplicação no pom usando o SEMVER [https://semver.org/](https://semver.org/)

<!-- RELEASE_NOTES --> 

### v1.0.0
**Versão inicial da aplicação.**

- Interface gráfica desktop com Java Swing.
- Conexão ao Azure Key Vault via Service Principal (Client ID + Client Secret + Tenant ID).
- Listagem de todos os segredos do vault em tabela.
- Filtro em tempo real por nome ou valor da variável.
- Menu de contexto para copiar valor ou linha no formato `VAR=valor`.
- Histórico local de conexões com recarregamento dos dados.
- Barra de progresso durante o carregamento dos segredos.
- Retry automático com backoff configurável (padrão: 3 tentativas).
- Arquitetura em camadas desacopladas (apresentação, serviço, domínio, infraestrutura).
- Testes automatizados com JUnit 5 para os serviços principais.
- Fat JAR gerado com maven-shade-plugin (todas as dependências incluídas).

---

## Tecnologias utilizadas

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem principal |
| Swing | (JDK built-in) | Interface gráfica |
| Azure SDK — Key Vault Secrets | 4.8.2 | Integração com Azure Key Vault |
| Azure SDK — Identity | 1.13.0 | Autenticação via Service Principal |
| Maven | 3.8+ | Build e gerenciamento de dependências |
| JUnit Jupiter | 5.10.2 | Testes automatizados |

---
# Sistema de Controle de Gastos Pessoais
**Documento de Planejamento — v2.3**

---

## 1. Visão Geral

Sistema web para gerenciamento de finanças pessoais de uso doméstico, rodando localmente. Permite que os membros da família registrem receitas, despesas e contas fixas, acompanhem o orçamento mensal e visualizem o saldo disponível após reserva de emergência e despesas do mês.

**Público-alvo:** Uso familiar interno, sem acesso externo ou integração com serviços de terceiros.

**Stack definida:**
- Backend: Spring Boot (Java) — API REST
- Frontend: Angular 21+
- Banco de dados: PostgreSQL (relacional, local)
- Migrations: Flyway
- Ambiente: execução local, sem deploy em nuvem

---

## 2. Contexto e Premissas

- Cada usuário gerencia **apenas suas próprias finanças** — dados isolados por conta, sem perfil administrador, sem visibilidade cruzada entre usuários.
- O período padrão de referência é o **mês calendário** (ex: junho/2025).
- O usuário pode configurar um **salário padrão** no perfil, somado automaticamente ao cálculo de sobra de todo mês.
- O usuário define um **valor fixo de reserva de emergência** mensal, deduzido antes do saldo livre.
- **Contas fixas** (água, luz, internet, streamings etc.) são cadastradas como entidade própria com valor base mensal, editável mês a mês sem alterar o valor base.
- Receitas e despesas eventuais são registradas como lançamentos avulsos.
- O sistema **não integra com bancos nem APIs externas** — todos os lançamentos são manuais.
- Autenticação obrigatória em todos os endpoints.

---

## 3. Funcionalidades

### 3.1 Autenticação e Usuário
- Cadastro com nome, e-mail e senha (hash bcrypt)
- Login com retorno de JWT (access token + refresh token)
- Logout com invalidação do refresh token
- Renovação automática do access token via refresh token (interceptor no Angular detecta 401 e renova sem redirecionar para login)
- Atualização de dados cadastrais, incluindo:
  - **Salário padrão** (decimal, opcional)
  - **Reserva de emergência mensal** (decimal, opcional)

### 3.2 Categorias
- CRUD de categorias de receita e despesa
- Cada categoria possui:
  - `nome`
  - `tipo`: `RECEITA` ou `DESPESA`
  - `essencialidade`: `ESSENCIAL`, `LAZER`, `EDUCACAO`, `SAUDE`, `TRANSPORTE`, `OUTROS`
  - `padrao` (boolean) — categorias padrão criadas automaticamente no cadastro
- Categorias padrão sugeridas: Alimentação (ESSENCIAL), Transporte (TRANSPORTE), Saúde (SAUDE), Lazer (LAZER), Educação (EDUCACAO), Freelance (RECEITA)

### 3.3 Contas Fixas
- Cadastro de contas fixas com os campos:
  - `nome` (ex: "Conta de Luz", "Netflix")
  - `valor_base` (decimal) — valor de referência mensal
  - `tipo_gasto`: `FIXO` ou `VARIAVEL`
    - `FIXO`: valor não costuma mudar (ex: internet, streaming)
    - `VARIAVEL`: valor oscila todo mês (ex: água, luz, telefone)
  - `essencialidade`: mesmos valores da categoria (`ESSENCIAL`, `LAZER` etc.)
  - `ativa` (boolean) — permite desativar sem excluir
- Todo mês, as contas fixas ativas geram automaticamente um **registro mensal** com o `valor_base`
- O usuário pode editar o valor do registro mensal de uma conta (ex: luz veio R$ 180 em vez de R$ 150) sem alterar o `valor_base` cadastrado
- O histórico de valores reais por mês fica salvo na tabela `ContaFixaRegistro`

### 3.4 Lançamentos (Receitas e Despesas Avulsas)
- CRUD de lançamentos com os campos:
  - `valor`, `data`, `descricao`
  - `categoria_id` (FK → Categoria)
  - `tipo`: `RECEITA` ou `DESPESA`
  - `recorrente` (boolean)
  - `meses_recorrencia`: `1` a `48`, ou `null` para recorrência infinita (somente se `recorrente = true`)
- Ao criar um lançamento recorrente, o sistema replica nos meses seguintes conforme `meses_recorrencia`
- Edição ou exclusão de lançamento recorrente afeta **somente o mês atual** — os demais permanecem
- Filtros: `?mes=6&ano=2025&tipo=DESPESA&categoria_id=...`

### 3.5 Resumo Financeiro
- Cálculo exibido na seguinte ordem:
  1. `total_receitas` = `salario_padrao` + `Σ receitas avulsas`
  2. `total_contas_fixas` = `Σ valores reais das contas fixas ativas no mês`
  3. `total_despesas_avulsas` = `Σ despesas lançadas manualmente`
  4. `total_despesas` = `total_contas_fixas` + `total_despesas_avulsas`
  5. `reserva_emergencia` = valor fixo configurado no perfil
  6. `saldo_livre` = `total_receitas` - `reserva_emergencia` - `total_despesas`
- Breakdown por essencialidade e por categoria
- Comparativo com meses anteriores

### 3.6 Orçamentos
- Definição de limite de gasto por categoria por mês
- Quando não definido para uma categoria, o sistema ignora o controle de orçamento para ela
- Alerta ao atingir 80% do limite da categoria (flag `alerta: true` no resumo)
- Comparativo realizado vs orçado

### 3.7 Relatórios
- Extrato do período com todos os lançamentos e contas fixas
- Resumo mensal com totais por tipo, categoria e essencialidade
- Filtros por: intervalo de datas, categoria, tipo, essencialidade
- Exportação em CSV (colunas: data, descricao, categoria, essencialidade, tipo, valor; encoding UTF-8, separador vírgula)

---

## 4. Modelo de Dados

```
Usuário
├── id (UUID)
├── nome
├── email (único)
├── senha (hash)
├── salario_mensal (decimal, nullable)
├── reserva_emergencia_mensal (decimal, nullable)
└── criado_em

Categoria
├── id (UUID)
├── usuario_id (FK → Usuário)
├── nome
├── tipo (RECEITA | DESPESA)
├── essencialidade (ESSENCIAL | LAZER | EDUCACAO | SAUDE | TRANSPORTE | OUTROS)
└── padrao (boolean)

Lancamento
├── id (UUID)
├── usuario_id (FK → Usuário)
├── categoria_id (FK → Categoria)
├── tipo (RECEITA | DESPESA)
├── valor (decimal)
├── data (date)
├── descricao
├── recorrente (boolean)
├── meses_recorrencia (int 1–48, nullable = infinito)
└── criado_em

ContaFixa
├── id (UUID)
├── usuario_id (FK → Usuário)
├── nome
├── valor_base (decimal)
├── tipo_gasto (FIXO | VARIAVEL)
├── essencialidade (ESSENCIAL | LAZER | EDUCACAO | SAUDE | TRANSPORTE | OUTROS)
└── ativa (boolean)

ContaFixaRegistro
├── id (UUID)
├── conta_fixa_id (FK → ContaFixa)
├── usuario_id (FK → Usuário)
├── mes (int 1–12)
├── ano (int)
├── valor_real (decimal)   ← editável, começa igual ao valor_base
└── observacao (nullable)

Orcamento
├── id (UUID)
├── usuario_id (FK → Usuário)
├── categoria_id (FK → Categoria)
├── mes (int 1–12)
├── ano (int)
└── valor_limite (decimal)
```

---

## 5. Endpoints da API

Todos os endpoints (exceto `/auth`) exigem header `Authorization: Bearer <token>`.

### 5.1 Autenticação

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/auth/cadastro` | Registra novo usuário |
| POST | `/api/auth/login` | Autentica e retorna JWT |
| POST | `/api/auth/logout` | Invalida o refresh token |
| POST | `/api/auth/refresh` | Renova o access token usando o refresh token |

### 5.2 Usuário

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/usuario/perfil` | Retorna dados do usuário autenticado |
| PUT | `/api/usuario/perfil` | Atualiza nome, salário padrão e reserva de emergência |

### 5.3 Categorias

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/categorias` | Lista categorias (`?tipo=DESPESA&essencialidade=LAZER`) |
| POST | `/api/categorias` | Cria nova categoria |
| PUT | `/api/categorias/{id}` | Atualiza categoria |
| DELETE | `/api/categorias/{id}` | Remove (valida se há lançamentos vinculados — retorna 409) |

### 5.4 Contas Fixas

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/contas-fixas` | Lista contas fixas (`?ativa=true`) |
| POST | `/api/contas-fixas` | Cadastra nova conta fixa |
| PUT | `/api/contas-fixas/{id}` | Atualiza dados da conta (nome, valor_base, tipo, essencialidade) |
| DELETE | `/api/contas-fixas/{id}` | Remove conta fixa |
| PATCH | `/api/contas-fixas/{id}/ativar` | Ativa ou desativa a conta |
| GET | `/api/contas-fixas/{id}/registros` | Lista registros mensais da conta |
| PATCH | `/api/contas-fixas/registros/{id}` | Edita valor real de um registro mensal específico |

### 5.5 Lançamentos

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/lancamentos` | Lista lançamentos (`?mes=6&ano=2025&tipo=DESPESA&categoria_id=...`) |
| POST | `/api/lancamentos` | Registra novo lançamento |
| PUT | `/api/lancamentos/{id}` | Atualiza lançamento (somente o mês atual se recorrente) |
| DELETE | `/api/lancamentos/{id}` | Remove lançamento (somente o mês atual se recorrente) |

### 5.6 Orçamentos

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/orcamentos` | Lista orçamentos (`?mes=6&ano=2025`) |
| POST | `/api/orcamentos` | Define orçamento para categoria/mês |
| PUT | `/api/orcamentos/{id}` | Atualiza limite |
| DELETE | `/api/orcamentos/{id}` | Remove orçamento |

### 5.7 Resumo (read-only, calculado)

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/resumo` | Resumo do mês (`?mes=6&ano=2025`) com todos os totais e breakdown |
| GET | `/api/resumo/historico` | Comparativo dos últimos N meses (`?meses=6`) |

### 5.8 Relatórios

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/relatorios/extrato` | Extrato detalhado (`?de=2025-06-01&ate=2025-06-30`) |
| GET | `/api/relatorios/mensal` | Resumo mensal consolidado |
| GET | `/api/relatorios/exportar` | Exporta extrato em CSV (`?de=...&ate=...`) |

---

## 6. Regras de Negócio

| # | Regra |
|---|-------|
| RN01 | Somente o próprio usuário acessa seus dados (validação por `usuario_id` do JWT) |
| RN02 | Ao cadastrar, o sistema cria automaticamente as categorias padrão |
| RN03 | Lançamento recorrente replica nos meses seguintes conforme `meses_recorrencia` (1–48 ou infinito se nulo) |
| RN04 | Edição ou exclusão de lançamento recorrente afeta somente o registro do mês atual |
| RN05 | Todo mês, contas fixas ativas geram automaticamente um `ContaFixaRegistro` com o `valor_base` |
| RN06 | Editar o `valor_real` de um `ContaFixaRegistro` não altera o `valor_base` da conta |
| RN07 | Ao atingir 80% do orçamento de uma categoria, a API sinaliza `alerta: true` no resumo |
| RN08 | Categoria sem orçamento definido não gera alertas |
| RN09 | Categoria não pode ser removida se houver lançamentos vinculados (retorna 409) |
| RN10 | Valor de lançamento deve ser positivo |
| RN11 | Data do lançamento não pode ser futura |
| RN12 | `salario_mensal` nulo ou zero é ignorado no cálculo |
| RN13 | `reserva_emergencia_mensal` é sempre deduzida antes do `saldo_livre`, independente das despesas |
| RN14 | `reserva_emergencia_mensal` nula ou zero é ignorada no cálculo |
| RN15 | `saldo_livre` pode ser negativo — o resumo deve sinalizá-lo visualmente |

---

## 7. Stack e Arquitetura Local

```
[ Angular 21+ ]  →  HTTP/REST  →  [ Spring Boot ]  →  [ PostgreSQL ]
   porta 4200                        porta 8080           porta 5432
```

- Angular consome a API via `HttpClient` com interceptor JWT que:
  - Injeta o `Authorization: Bearer <token>` em todas as requisições
  - Detecta resposta `401`, chama `/api/auth/refresh` automaticamente e reenvia a requisição original
  - Redireciona para login apenas se o refresh token também estiver expirado
- Spring Boot expõe a API REST com Spring Security configurado para JWT stateless
- Flyway gerencia as migrations do banco (versionadas em `/resources/db/migration`)
- CORS configurado no backend para aceitar requisições de `localhost:4200`
- Sem proxy reverso, sem Docker Compose obrigatório — cada serviço sobe individualmente

---

## 8. Segurança

- Senhas armazenadas com **bcrypt** (custo mínimo 12)
- JWT com expiração curta (access: 15 min, refresh: 7 dias)
- Refresh token armazenado no banco e invalidado no logout
- Todos os endpoints protegidos por autenticação
- Rate limiting nas rotas de autenticação (previne brute force)
- Dados validados e sanitizados antes de persistir

---

## 9. O que fica fora do escopo (v1)

- Integração com Open Finance / APIs bancárias
- Aplicativo mobile nativo
- Compartilhamento de orçamento entre usuários
- Metas de curto/longo prazo
- Notificações por e-mail ou push
- Deploy em nuvem ou acesso remoto

---

## 10. Próximos Passos

1. Criar repositório e configurar projetos Angular + Spring Boot
2. Configurar PostgreSQL local e criar primeira migration com Flyway
3. Implementar autenticação com JWT (base de tudo)
4. Implementar interceptor de refresh token no Angular
5. Implementar CRUD de categorias e lançamentos
6. Implementar contas fixas e geração automática de registros mensais
7. Implementar resumo financeiro com todas as regras de negócio
8. Implementar orçamentos
9. Implementar relatórios e exportação CSV
10. Desenvolver interface Angular consumindo todos os endpoints

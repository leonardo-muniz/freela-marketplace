<div align="center">

# Freela Marketplace

Projeto de referência para um marketplace de contratação de freelancers construído com arquitetura de microsserviços em Java e Spring.

![Java 27](https://img.shields.io/badge/Java-27-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 4.1.1](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Spring Cloud 2025.1.3](https://img.shields.io/badge/Spring%20Cloud-2025.1.3-6DB33F?logo=spring&logoColor=white)
![PostgreSQL 18](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql&logoColor=white)
![Apache Kafka 4](https://img.shields.io/badge/Apache%20Kafka-4-231F20?logo=apachekafka&logoColor=white)

</div>

---

A aplicação representa um cenário em que clientes contratam freelancers para a execução de trabalhos. O núcleo do sistema é o gerenciamento dos contratos firmados entre as partes. A partir desse domínio, outros serviços mantêm informações relacionadas a notificações, reputação e auditoria.

## Navegação

- [Visão geral](#visão-geral)
- [Domínio](#domínio)
- [Serviços](#serviços)
- [API Gateway](#api-gateway)
- [Eureka Server](#eureka-server)
- [PostgreSQL](#postgresql)
- [Apache Kafka](#apache-kafka)
- [Logs](#logs)
- [Infraestrutura local](#infraestrutura-local)
- [Execução das aplicações](#execução-das-aplicações)
- [Portas](#portas)
- [Teste básico](#teste-básico)
- [Tecnologias](#tecnologias)
- [Arquitetura orientada a eventos](#arquitetura-orientada-a-eventos)
- [Observabilidade](#observabilidade)
- [Resiliência e cache](#resiliência-e-cache)
- [Limites arquiteturais](#limites-arquiteturais)
- [Migrations e isolamento](#migrations-e-isolamento)
- [Testes e evidências](#testes-e-evidências)
- [Eventos, confiabilidade e observabilidade (refatoração)](#eventos-confiabilidade-e-observabilidade-refatoração)

---

## Visão geral

O sistema é composto por seis aplicações Spring Boot:

- `eureka-server`: registro e descoberta dos serviços.
- `api-gateway`: ponto de entrada HTTP da aplicação.
- `contrato-service`: gerenciamento dos contratos entre clientes e freelancers.
- `notificacao-service`: armazenamento das notificações relacionadas aos contratos.
- `reputacao-service`: manutenção de informações agregadas sobre os freelancers.
- `auditoria-service`: registro de eventos relevantes do sistema.

A infraestrutura local utiliza PostgreSQL e Apache Kafka.

```text
                         +-------------------+
                         |      Cliente      |
                         +---------+---------+
                                   |
                                   | HTTP
                                   v
                         +-------------------+
                         |    API Gateway    |
                         |       :8080       |
                         +---------+---------+
                                   |
                     Service Discovery / Eureka
                                   |
                +------------------+------------------+
                |                  |                  |
                v                  v                  v
       +----------------+  +----------------+  +----------------+
       | contrato       |  | notificacao    |  | reputacao      |
       | service :8081  |  | service :8082  |  | service :8083  |
       +----------------+  +----------------+  +----------------+
                |
                |                       +----------------+
                +---------------------->| auditoria      |
                                        | service :8084  |
                                        +----------------+

                          +-------------------+
                          |       Kafka       |
                          |       :9092       |
                          +-------------------+

                          +-------------------+
                          |    PostgreSQL     |
                          |       :5432       |
                          +-------------------+
```

---

## Domínio

O domínio principal está no `contrato-service`.

Um contrato representa o vínculo entre um cliente e um freelancer para a execução de um trabalho. Cada contrato possui:

- identificador;
- cliente;
- freelancer;
- título do trabalho;
- valor;
- status;
- data de criação.

Os estados disponíveis são:

```text
ATIVO
ENTREGA_REGISTRADA
CONCLUIDO
CANCELADO
```

O fluxo de negócio previsto pelo modelo é:

```text
ATIVO
  |
  v
ENTREGA_REGISTRADA
  |
  v
CONCLUIDO
```

Um contrato ativo também pode ser cancelado.

O `contrato-service` utiliza uma organização inspirada em Domain-Driven Design, separando domínio, aplicação e infraestrutura. Os serviços de notificação, reputação e auditoria seguem a mesma convenção (`domain/model`, `domain/repository`, `application`, `infrastructure/persistence`, `infrastructure/messaging` e `infrastructure/web`). O `api-gateway` mantém a classe de aplicação no pacote raiz e separa filtros em `infrastructure/filter` e controllers em `infrastructure/web`.

```text
contrato-service
└── src/main/java/br/com/freela/contrato
    ├── application
    ├── domain
    │   ├── event
    │   ├── model
    │   ├── repository
    │   └── shared
    └── infrastructure
        ├── persistence
        └── web
```

O Aggregate `Contrato` concentra as regras relacionadas às mudanças de estado e produz eventos de domínio. Atualmente existe o evento `ContratoCriado`, que contém as principais informações do contrato no momento da criação.

---

## Serviços

### contrato-service

Responsável pelo ciclo de vida dos contratos.

Porta:

```text
8081
```

Banco:

```text
contrato_db
```

Principais recursos HTTP:

```text
POST /api/contratos
GET  /api/contratos
GET  /api/contratos/{id}
```

Exemplo de criação de contrato:

```json
{
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "freelancerId": "22222222-2222-2222-2222-222222222222",
  "titulo": "Construção de API de pagamentos",
  "valor": 3500.00
}
```

### notificacao-service

Mantém notificações relacionadas aos acontecimentos do marketplace.

Porta:

```text
8082
```

Banco:

```text
notificacao_db
```

As notificações armazenam informações como contrato, destinatário, tipo, mensagem e momento de criação.

### reputacao-service

Mantém informações agregadas sobre a atividade dos freelancers.

Porta:

```text
8083
```

Banco:

```text
reputacao_db
```

Para cada freelancer são mantidos dados como quantidade de contratos concluídos e valor total dos contratos registrados.

Endpoint disponível para consulta:

```text
GET /api/reputacoes
```

### auditoria-service

Responsável pelo armazenamento de registros associados aos eventos do sistema.

Porta:

```text
8084
```

Banco:

```text
auditoria_db
```

Cada registro de auditoria pode armazenar:

- `eventId`;
- `aggregateId`;
- tipo do evento;
- `correlationId`;
- payload original;
- horário de recebimento.

Endpoint disponível para consulta:

```text
GET /api/auditoria
```

---

## API Gateway

O `api-gateway` é o ponto de entrada HTTP para os microsserviços.

Porta:

```text
8080
```

As rotas configuradas são:

| Caminho | Serviço |
|---|---|
| `/api/contratos/**` | `contrato-service` |
| `/api/notificacoes/**` | `notificacao-service` |
| `/api/reputacoes/**` | `reputacao-service` |
| `/api/auditoria/**` | `auditoria-service` |

O Gateway utiliza Eureka para localizar as instâncias dos serviços.

Também existe suporte ao header:

```text
X-Correlation-Id
```

Quando o header não é enviado pelo cliente, o Gateway gera automaticamente um UUID e o encaminha para o serviço de destino.

---

## Eureka Server

O Eureka Server mantém o registro das aplicações disponíveis no ambiente.

Porta:

```text
8761
```

Interface web:

```text
http://localhost:8761
```

Os microsserviços utilizam, por padrão:

```text
http://localhost:8761/eureka/
```

como endereço do service registry.

---

## PostgreSQL

O ambiente local utiliza uma instância PostgreSQL 18 independente para cada
microsserviço, com volume, usuário, database e porta próprios:

| Serviço | Host/porta | Database |
|---|---|---|
| contrato | `localhost:5433` | `contrato_db` |
| notificação | `localhost:5434` | `notificacao_db` |
| reputação | `localhost:5435` | `reputacao_db` |
| auditoria | `localhost:5436` | `auditoria_db` |

Bancos criados durante a inicialização:

```text
contrato_db
notificacao_db
reputacao_db
auditoria_db
```

Os containers que provisionam os bancos estão definidos em:

```text
infra/docker-compose.yml
```

Os serviços utilizam Flyway para migrations e Hibernate com `ddl-auto: validate`.

---

## Apache Kafka

O Apache Kafka é executado em modo KRaft, sem ZooKeeper.

Para aplicações executadas diretamente na máquina:

```text
localhost:9092
```

Para aplicações executadas dentro da rede Docker:

```text
kafka:19092
```

O broker possui listeners separados para comunicação interna e externa.

O ambiente também inclui o Kafka UI.

```text
http://localhost:8090
```

---

## Logs

Todos os serviços utilizam logs em nível `INFO` com um formato comum contendo data, nível, nome da aplicação, thread, logger e mensagem.

Exemplo:

```text
2026-09-14 14:42:18.431 INFO service=contrato-service thread=http-nio-8081-exec-1 logger=b.c.f.c.a.ContratoApplicationService - contrato.criacao.inicio clienteId=... freelancerId=...
```

O código registra pontos importantes do fluxo, incluindo:

```text
gateway.request.inicio
gateway.request.fim
http.contrato.criar
contrato.criacao.inicio
contrato.dominio.criado
contrato.persistence.save.inicio
contrato.persistence.save.sucesso
contrato.evento.pendente
contrato.criacao.sucesso
reputacao.atualizacao.inicio
reputacao.atualizacao.sucesso
auditoria.registro.inicio
auditoria.registro.sucesso
```

A presença do `correlationId` nas chamadas HTTP permite relacionar logs produzidos durante uma mesma requisição.

---

## Infraestrutura local

Os serviços de infraestrutura estão definidos em:

```text
infra/docker-compose.yml
```

Para iniciar o ambiente:

```bash
cd infra
docker compose up -d
```

Para verificar os containers:

```bash
docker compose ps
```

Para encerrar:

```bash
docker compose down
```

Os dados do PostgreSQL são mantidos em volume Docker.

Para remover também os dados persistidos:

```bash
docker compose down -v
```

---

## Execução das aplicações

A partir da raiz do projeto, cada módulo pode ser iniciado separadamente com Maven.

Eureka Server:

```bash
mvn -pl eureka-server spring-boot:run
```

API Gateway:

```bash
mvn -pl api-gateway spring-boot:run
```

Contrato Service:

```bash
mvn -pl contrato-service spring-boot:run
```

Notificação Service:

```bash
mvn -pl notificacao-service spring-boot:run
```

Reputação Service:

```bash
mvn -pl reputacao-service spring-boot:run
```

Auditoria Service:

```bash
mvn -pl auditoria-service spring-boot:run
```

---

## Portas

| Componente | Porta |
|---|---:|
| API Gateway | `8080` |
| contrato-service | `8081` |
| notificacao-service | `8082` |
| reputacao-service | `8083` |
| auditoria-service | `8084` |
| Eureka Server | `8761` |
| Kafka | `9092` |
| Kafka UI | `8090` |
| PostgreSQL contrato | `5433` |
| PostgreSQL notificação | `5434` |
| PostgreSQL reputação | `5435` |
| PostgreSQL auditoria | `5436` |
| Redis | `6379` |
| Prometheus | `9090` |
| Grafana | `3000` |
| Zipkin | `9411` |
| OpenTelemetry Collector | `4317/4318` |

---

## Teste básico

Com a infraestrutura e as aplicações em execução, um contrato pode ser criado pelo Gateway:

```bash
curl -i -X POST http://localhost:8080/api/contratos \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: teste-contrato-001' \
  -d '{
    "clienteId": "11111111-1111-1111-1111-111111111111",
    "freelancerId": "22222222-2222-2222-2222-222222222222",
    "titulo": "Construção de API de pagamentos",
    "valor": 3500.00
  }'
```

Consulta dos contratos:

```bash
curl http://localhost:8080/api/contratos
```

Consulta de um contrato específico:

```bash
curl http://localhost:8080/api/contratos/{id}
```

---

## Tecnologias

```text
Java 27
Spring Boot 4.1.1
Spring Cloud 2025.1.3
Spring Cloud Gateway
Netflix Eureka
Spring Data JPA
PostgreSQL 18
Apache Kafka 4
Redis 8
Prometheus 3
Grafana 12
OpenTelemetry Collector
Docker Compose
Maven
```

---

## Arquitetura orientada a eventos

O `contrato-service` não chama os serviços auxiliares por HTTP. A alteração do
aggregate e o registro do evento são gravados na tabela `outbox_events` na
mesma transação local. Um publisher assíncrono publica os eventos pendentes no
Kafka; falhas são reprocessadas com backoff e, após o limite configurado,
ficam com status `DEAD` para diagnóstico e reprocessamento operacional.

O envelope de integração é versionado e possui o seguinte formato:

```json
{
  "eventId": "uuid",
  "eventType": "ContratoCriado",
  "eventVersion": 1,
  "occurredAt": "2026-10-13T04:00:00Z",
  "aggregateId": "uuid",
  "contratoId": "uuid",
  "correlationId": "operacao-001",
  "traceparent": "00-...",
  "payload": {}
}
```

### Contratos Kafka

| Tópico | Produtor | Consumidores | Chave |
|---|---|---|---|
| `contratos.v1` | `contrato-service` | `notificacao-service`, `reputacao-service`, `auditoria-service` | `contratoId` |
| `contratos.v1-reputacao-service-dlt` | Kafka retry/DLT | operação/reprocessamento | `contratoId` |
| `contratos.v1-notificacao-service-dlt` | Kafka retry/DLT | operação/reprocessamento | `contratoId` |
| `contratos.v1-auditoria-service-dlt` | Kafka retry/DLT | operação/reprocessamento | `contratoId` |

`contrato-service` publica `ContratoCriado` e `ContratoConcluido`. A
partição é escolhida pela chave `contratoId`: eventos de contratos diferentes
podem ser processados em paralelo, enquanto os eventos de um mesmo contrato
permanecem ordenados na partição. Cada consumidor possui um group id próprio.

Todos os consumidores verificam `eventId` antes de aplicar efeitos. A tabela
`notificacoes` possui unicidade por evento, `reputacao_inbox` funciona como
inbox transacional da reputação e `auditoria_eventos.event_id` é único. Uma
reentrega, portanto, é confirmada sem duplicar o efeito.

Para concluir um contrato já entregue:

```bash
curl -i -X POST http://localhost:8080/api/contratos/{id}/conclusao \
  -H 'X-Correlation-Id: teste-contrato-001'
```

---

## Observabilidade

O Gateway cria ou preserva `X-Correlation-Id` e encaminha o valor ao serviço
de contrato. O `correlationId` e o `traceparent` são registrados no envelope
Kafka e nos logs. Micrometer Tracing exporta os spans para Zipkin:

```text
http://localhost:9411
```

Os serviços usam logs com campos de correlação. O Compose disponibiliza Loki
(`http://localhost:3100`) e Grafana Alloy para centralização dos logs, além de
Prometheus (`http://localhost:9090`) e Grafana (`http://localhost:3000`) com
datasources provisionados. O OpenTelemetry Collector recebe OTLP em `4317` e
`4318` e exporta traces para Zipkin (`http://localhost:9411`). As métricas do
Actuator são coletadas em `/actuator/prometheus`.

---

## Resiliência e cache

O Gateway aplica Circuit Breaker e timeout à rota de contratos, com fallback
HTTP 503 explícito. As métricas de resiliência ficam disponíveis para o
Actuator/Prometheus. O `reputacao-service` usa Redis apenas como cache de
leitura com TTL de 60 segundos; PostgreSQL continua sendo a fonte de verdade e
as atualizações por evento invalidam o cache.

---

## Limites arquiteturais

Os serviços usam instâncias PostgreSQL 18, usuários e volumes independentes no
ambiente local. Em produção, cada bounded context deve manter HA, backup/PITR
e restauração testada; os defaults locais podem ser substituídos por `DB_URL`,
`DB_USER` e `DB_PASSWORD`.

O sistema implementa **CRUD + Outbox + eventos de integração**, não Event
Sourcing: a tabela `outbox_events` não é um event store imutável e o aggregate
não é reconstruído por replay. Também não há CQRS completo; as consultas usam
o modelo transacional. Essa decisão é intencional para o escopo atual e evita
introduzir complexidade sem necessidade de read models, replay e governança de
schemas.

---

## Migrations e isolamento

Cada serviço possui banco lógico, usuário e migration Flyway próprios. O
Hibernate usa `ddl-auto: validate`; nenhum serviço acessa tabelas de outro
bounded context. As migrations ficam em
`src/main/resources/db/migration` dentro do respectivo serviço.

---

## Testes e evidências

O teste de domínio pode ser executado com:

```bash
mvn test
```

Para uma evidência ponta a ponta, inicie a infraestrutura, Eureka e os quatro
serviços, crie um contrato pelo Gateway, consulte o tópico `contratos.v1` no
Kafka UI (`http://localhost:8090`), consulte notificações/reputação/auditoria
e pesquise o `correlationId` nos logs e no trace do Zipkin. Reenviar o mesmo
payload com o mesmo `eventId` demonstra a idempotência.

---

## Eventos, confiabilidade e observabilidade (refatoração)

`contrato-service` grava aggregate e `outbox_events` na mesma transação. Um publisher encaminha o envelope JSON ao tópico `contratos.v1`, usando `aggregateId/contratoId` como key e 3 partições; falhas incrementam `attempts` e chegam a status `DEAD` após cinco tentativas. Consumidores possuem grupos independentes (`notificacao-service`, `reputacao-service`, `auditoria-service`), retry de quatro tentativas e DLT `.dlt`.

Envelope: `{eventId,eventType,eventVersion,occurredAt,aggregateId,contratoId,correlationId,traceparent,payload}`. A persistência local aplica unique `eventId` (inbox na reputação), portanto reentrega não duplica notificações, auditoria ou contagem de `ContratoConcluido`. A key preserva ordem por contrato e partições permitem concorrência entre contratos.

Cada serviço usa Flyway (`src/main/resources/db/migration`) e `ddl-auto=validate`, com bancos/usuários isolados no Compose. `X-Correlation-Id` e `traceparent` são encaminhados pelo Gateway e registrados no MDC/logs; métricas e health estão expostos. Para executar: `mvn test` ou `mvn verify`; infraestrutura: `cd infra && docker compose up -d`.

O tracing usa Micrometer Tracing + Brave/Zipkin (`http://localhost:9411`) quando executado com o profile local; o contexto W3C `traceparent` também permanece no envelope Kafka. Logs podem ser coletados por Promtail e consultados no Loki (`http://localhost:3100`).

<div align="center">

[Projeto Original do Professor](https://github.com/leoinfnet/freela-marketplace-assessment)

</div>

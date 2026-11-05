# Mini Commerce

Esqueleto de microsserviços para portfólio. O fluxo de pedido é uma saga simples: o pedido não chama estoque e pagamento em cadeia. Ele publica um evento, e os outros serviços reagem.

```text
POST /api/orders
        |
        v
 order-service --OrderCreated--> inventory-service
                                      |  \
                                      |   +--InventoryRejected--> order (REJECTED)
                                      |
                                      +--InventoryReserved--> payment-service
                                                                  |  \
                                                                  |   +--PaymentFailed--> order + inventory (libera reserva)
                                                                  |
                                                                  +--PaymentApproved--> order (PAID) + inventory (confirma)
```

## Stack

- Java 17, Spring Boot 4.1, Spring Cloud 2025.1 (Gateway MVC)
- Postgres, um database por serviço, migração com Flyway
- RabbitMQ, exchange topic, fila com DLQ
- Kubernetes + Kustomize
- ArgoCD, app of apps

## Serviços

| Serviço | Porta local | Database | Papel |
|---|---|---|---|
| api-gateway | 8080 | — | entrada HTTP |
| order-service | 8081 | `orderdb` | cria o pedido e acompanha o status |
| inventory-service | 8082 | `inventorydb` | reserva e libera estoque |
| payment-service | 8083 | `paymentdb` | aprova ou recusa o pagamento |

SKUs de demonstração, já inseridos no estoque:

| SKU | Estoque | Resultado |
|---|---|---|
| `BOOK-01` | 20 | `PAID` |
| `MUG-02` | 5 | `PAID` |
| `SKU-FAIL-STOCK` | 0 | `REJECTED` |
| `SKU-FAIL-PAY` | 10 | `PAYMENT_FAILED` e estoque liberado |

## Subir local

```bash
docker compose up -d
mvn -pl services/order-service -am spring-boot:run
mvn -pl services/inventory-service -am spring-boot:run
mvn -pl services/payment-service -am spring-boot:run
mvn -pl services/api-gateway -am spring-boot:run
```

Cada comando em um terminal. Postgres fica em `localhost:5432` e o RabbitMQ em `localhost:5672`. O painel do broker é `http://localhost:15672` (`mini` / `mini`).

```bash
./scripts/demo.sh
```

O script cria três pedidos e espera os status `PAID`, `REJECTED` e `PAYMENT_FAILED`.

Consulta manual:

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"sku":"BOOK-01","quantity":1,"amount":39.90}'
```

## Kubernetes e ArgoCD

Os manifests ficam em `gitops/`. Infra e aplicações são Applications separadas, com sync wave, para o Postgres e o RabbitMQ subirem antes dos serviços.

1. Troque `https://github.com/SEU_USUARIO/mini-commerce.git` em `gitops/argocd/`.
2. Publique o repositório.
3. Construa as imagens e carregue no cluster local:

```bash
make images
kind load docker-image minicommerce/order-service:0.1.0
kind load docker-image minicommerce/inventory-service:0.1.0
kind load docker-image minicommerce/payment-service:0.1.0
kind load docker-image minicommerce/api-gateway:0.1.0
```

4. Com o ArgoCD já instalado:

```bash
kubectl apply -f gitops/argocd/root-app.yaml
```

Sem ArgoCD, o mesmo conteúdo sobe direto:

```bash
kubectl apply -k gitops/infra
kubectl apply -k gitops/apps
```

O Ingress espera o host `minicommerce.local`. Para um teste rápido, use port-forward:

```bash
kubectl -n minicommerce port-forward svc/api-gateway 8080:8080
```

A tag da imagem é controlada em `gitops/apps/kustomization.yaml`. O fluxo GitOps esperado é: CI publica a imagem, abre commit só com a tag nova, e o ArgoCD sincroniza. Esse passo de publicação ainda não está no workflow.

## O que o esqueleto já mostra

- database per service e `ddl-auto=validate`
- contrato de evento em `common-events`, sem compartilhar tabela
- publicação depois do commit, não no meio da transação
- consumidor idempotente por `eventId`
- reserva de estoque com lock pessimista
- DLQ: exceção no listener rejeita a mensagem (`default-requeue-rejected=false`)
- probes de liveness e readiness para o Kubernetes

## O que ainda não está pronto de propósito

- **Outbox.** Se o processo morrer depois do commit e antes do publish, o evento se perde. O próximo passo é uma tabela `outbox` e um publicador.
- **Catálogo e notificação.** O desenho original deixa esses dois serviços para depois. O contrato de eventos já comporta os dois.
- **Segredos.** `mini` / `mini` é só para demonstração. Em cluster real, use Sealed Secrets ou External Secrets.
- **Volume do Postgres.** No cluster o dado fica em `emptyDir` para o Kind subir sem StorageClass. Troque por PVC quando houver provisioner.
- **CI de imagem.** `.github/workflows/ci.yml` só roda `mvn test`.

## Layout

```text
services/          código Java
gitops/infra/      Postgres, RabbitMQ, Secrets
gitops/apps/       Deployments e Services
gitops/argocd/     Application of Applications
deploy/Dockerfile  build multi-stage, argumento MODULE
```

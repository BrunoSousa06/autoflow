# Arquitetura

O projeto utiliza Clean Architecture.

## Camadas

### Domain

Contém:

- entidades;
- value objects;
- regras de negócio;
- interfaces de repositório.

Não pode depender de:

- Spring;
- JPA;
- controllers;
- infraestrutura.

### Application

Contém:

- casos de uso;
- portas de entrada;
- portas de saída;
- DTOs internos.

### Infrastructure

Contém:

- persistência;
- implementações de gateways;
- integrações externas;
- configurações do framework.

### Presentation

Contém:

- controllers REST;
- request DTOs;
- response DTOs;
- tratamento de entrada e saída HTTP.
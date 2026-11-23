# 001 — Stack, arquitetura em camadas, persistência e autenticação

**Data:** 23/11/2026
**Situação:** adotada; revisada no mesmo dia para Spring Data JPA, camadas, MapStruct e Swagger

## Contexto

O levantamento indicava Java + Spring Boot + PostgreSQL como referência. O responsável pelo projeto definiu como padrão Spring Data JPA, organização em camadas (entidade, DTO, mapper MapStruct, repository, service e controller), Spring Security e documentação Swagger. O esquema do banco é mantido no repositório `banco-dados-ordem-de-servico` e já define as regras de integridade críticas: chaves compostas que impedem cobrança de OS não concluída, pagamento de cobrança não paga e lançamento com valor diferente do pagamento, além de unicidade de cobrança por OS, de pagamento por cobrança e de lançamento por pagamento.

## Decisão

- **Java 25 (LTS)** e **Spring Boot 4.2**, com build Maven via `mvnw`.
- **Pacotes por camada** em `br.com.marwil.ordemservico`:
  - `entity`: entidades JPA, uma por tabela; `OrdemServicoTecnico` usa chave composta (`@EmbeddedId` + `@MapsId`).
  - `enums`: perfis, situações e tipos persistidos como texto.
  - `dto.request` / `dto.response`: records de entrada, com validação e normalização de textos, e de saída. Entidades nunca são expostas pela API.
  - `mapper`: interfaces MapStruct com `componentModel = spring` e `unmappedTargetPolicy = ERROR`, para que campos esquecidos quebrem a compilação.
  - `repository`: interfaces Spring Data JPA; `repository.specification` reúne os filtros dinâmicos e a regra de visibilidade do técnico; `repository.projection` reúne os totais calculados por JPQL.
  - `service`: regras de negócio e transações; `service.pdf` gera o documento.
  - `controller`: rotas REST, autorização por perfil e anotações OpenAPI.
  - `security`: `UserDetailsService`, emissão de JWT, conversão do token em usuário autenticado e anotações de perfil.
  - `config`: Spring Security, JWT, OpenAPI, relógio e criação do dono inicial.
  - `exception`: exceções de negócio e tratamento global em `ProblemDetail`.
- **Mapeamento fiel ao esquema:**
  - `ddl-auto=none` em execução e `validate` nos testes, que conferem cada entidade contra o script real.
  - Colunas preenchidas pelo banco (`valor_total` gerado, `criado_em` e `atualizado_em` por default e trigger) são somente leitura e relidas após gravar (`@Generated`).
  - As colunas fixas das chaves compostas (`tecnico_perfil`, `ordem_servico_status`, `cobranca_status`) são mapeadas com o único valor aceito.
- **Concorrência:**
  - A captura de OS, as alterações de equipe, situação e itens, e o envio à cobrança usam `@Lock(PESSIMISTIC_WRITE)` na OS.
  - O pagamento bloqueia a cobrança, grava a baixa para `PAGA` com `saveAndFlush` antes de inserir pagamento e lançamento, porque as chaves compostas exigem essa ordem, e depende das restrições `UNIQUE` do banco como segunda barreira.
- **Spring Security:**
  - Login por `AuthenticationManager` com `DaoAuthenticationProvider`, BCrypt e `UserDetailsService` sobre a entidade `Usuario`.
  - Requisições autenticadas por JWT HS256 (resource server com Nimbus).
  - A cada requisição, perfil e situação ativa são relidos do banco, então desativação e troca de perfil valem na hora.
  - Autorização por `@PreAuthorize`.
- **Swagger:** springdoc-openapi publica `/v3/api-docs` e `/swagger-ui.html` sem autenticação, com esquema Bearer para testar as rotas protegidas.
- **O backend não cria nem migra o esquema.** Os testes de integração usam a cópia `src/test/resources/db/schema.sql` em um PostgreSQL 18 descartável via Testcontainers.
- Fuso horário do negócio configurável (`APP_FUSO_HORARIO`, padrão `America/Sao_Paulo`) para datas de abertura, pagamento, vencimento e impressão; instantes são gravados como `TIMESTAMPTZ` e devolvidos em ISO-8601 UTC.

## Consequências

- Mudanças de esquema exigem alteração coordenada entre o repositório de banco, a cópia de teste e as entidades; a validação do Hibernate nos testes aponta divergências.
- Não há sessão no servidor; o logout é responsabilidade do cliente, descartando o token.
- Os scripts `mvnw`/`mvnw.cmd` e `.mvn/wrapper` são arquivos de terceiros gerados pelo Maven Wrapper e mantêm seus comentários originais, conforme a exceção prevista nos padrões compartilhados.

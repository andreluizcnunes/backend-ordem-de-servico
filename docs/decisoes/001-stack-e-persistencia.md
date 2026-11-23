# 001 — Stack, persistência e autenticação

**Data:** 23/11/2026
**Situação:** adotada na implementação inicial do MVP

## Contexto

O levantamento indicava Java + Spring Boot + PostgreSQL como referência, sem fixar versões. O esquema do banco é mantido no repositório `banco-dados-ordem-de-servico` e já define as regras de integridade críticas: chaves compostas que impedem cobrança de OS não concluída, pagamento de cobrança não paga e lançamento com valor diferente do pagamento, além de unicidade de cobrança por OS, pagamento por cobrança e lançamento por pagamento.

## Decisão

- **Java 25 (LTS)** e **Spring Boot 4.2**, com build Maven via `mvnw`.
- **Spring JDBC (`JdbcClient`)** em vez de JPA. As consultas de visibilidade, os bloqueios `SELECT ... FOR UPDATE`, a coluna gerada `valor_total` e as chaves compostas com valores fixos (`tecnico_perfil`, `ordem_servico_status`, `cobranca_status`) ficam explícitos em SQL, sem mapeamentos que escondam o comportamento.
- **O backend não cria nem migra o esquema.** Ele espera o banco já preparado pelos scripts do repositório de banco. Os testes de integração usam uma cópia do script em `src/test/resources/db/schema.sql`, aplicada em um PostgreSQL 18 descartável via Testcontainers; ao alterar o esquema no repositório de banco, atualizar essa cópia.
- **Autenticação por JWT HS256** emitido pelo próprio backend (`spring-boot-starter-security-oauth2-resource-server` com Nimbus), senhas com BCrypt. Perfil e situação ativa do usuário são lidos do banco a cada requisição, de modo que desativação e troca de perfil valem imediatamente, mesmo com token ainda válido.
- **Autorização no servidor:** perfis por anotação (`@ApenasGestores`, `@ApenasDono`, `@ApenasTecnicos`) e escopo de visibilidade do técnico aplicado na própria consulta SQL.
- **PDF com OpenPDF 3**, gerado em memória.
- Fuso horário do negócio configurável (`APP_FUSO_HORARIO`, padrão `America/Sao_Paulo`) para datas de pagamento, vencimento e impressão; instantes são gravados como `TIMESTAMPTZ` e devolvidos em ISO-8601 UTC.

## Consequências

- Mudanças de esquema exigem alteração coordenada entre o repositório de banco, a cópia de teste e os repositórios SQL do backend.
- Não há sessão no servidor; o logout é responsabilidade do cliente, descartando o token. Um usuário desativado perde acesso na requisição seguinte.
- Os scripts `mvnw`/`mvnw.cmd` e `.mvn/wrapper` são arquivos de terceiros gerados pelo Maven Wrapper e mantêm seus comentários originais, conforme a exceção prevista nos padrões compartilhados.

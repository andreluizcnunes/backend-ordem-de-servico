# 002 — Regras em aberto no levantamento adotadas no MVP

**Data:** 23/11/2026
**Situação:** adotadas provisoriamente; sujeitas à confirmação do negócio

## Contexto

O `PROJETO.md` lista questões sem resposta. Para entregar o fluxo de ponta a ponta, o backend adotou as regras abaixo, escolhidas por serem as mais conservadoras e fáceis de alterar.

## Decisões

1. **Assumir uma OS não muda a situação.** A OS continua `ABERTA` até o técnico informar `EM_ATENDIMENTO`. Atribuição não é status, como diz o levantamento. Para mudar, basta chamar `iniciarAtendimento` dentro de `EquipeService.assumir`.
2. **Quem conclui:** qualquer técnico da equipe ou um gestor (DONO/ADMINISTRATIVO). A OS registra `concluida_por` e `data_conclusao`.
3. **Transições permitidas:** `ABERTA → EM_ATENDIMENTO | AGUARDANDO`, `EM_ATENDIMENTO → AGUARDANDO | CONCLUIDA`, `AGUARDANDO → EM_ATENDIMENTO`. `AGUARDANDO` exige motivo, que também vira um registro do tipo `IMPEDIMENTO`. Somente gestores reabrem uma OS `CONCLUIDA` (volta para `EM_ATENDIMENTO`), e apenas antes do envio à cobrança. Concluir direto de `AGUARDANDO` não é permitido; é preciso retomar antes.
4. **Técnico precisa integrar a equipe** para alterar a situação ou registrar atendimento. Em uma OS disponível ele recebe `403` e deve usar `/assumir` primeiro.
5. **Vencimento opcional.** A cobrança aceita `dataVencimento`; sem ela, aparece apenas como pendente. `vencida = true` só quando existe vencimento já ultrapassado e a cobrança segue pendente. O termo "inadimplente" não é usado pela API.
6. **Pagamento integral e único.** O valor recebido precisa ser igual ao total da cobrança. Pagamento parcial, estorno e cancelamento ficam fora do MVP.
7. **Precificação somente após a conclusão** e antes do envio à cobrança. Depois do envio, itens, equipe, dados da OS e situação ficam congelados.
8. **Preço obrigatório.** O esquema exige `preco_unitario NOT NULL`. Preço ausente é rejeitado com `400`; zero só é aceito quando enviado explicitamente.
9. **Cobrança com total zero não é enviada** (`409`), porque o esquema exige pagamento com valor positivo e ela nunca poderia ser quitada.
10. **Respostas ao técnico:** uma OS fora do seu escopo é tratada como inexistente (`404`), sem revelar dados de outra equipe. Na disputa de captura, quem perde recebe `409`.
11. **PDF por perfil:** gestores recebem valores unitários, subtotais e total. Técnicos recebem o mesmo documento sem nenhuma coluna ou campo financeiro, com linhas em branco para preenchimento em campo.
12. **Auditoria de equipe limitada ao esquema atual:** a tabela `ordens_servico_tecnicos` guarda quem atribuiu e quando, mas remover um técnico apaga a linha. Se o histórico de remoções for exigido, o esquema precisa de uma tabela de histórico.
13. **Itens sem ordem de exibição própria:** o esquema não tem coluna de ordem, então os itens são exibidos na ordem de criação (`id`).

## Consequências

Cada item acima pode ser revisto sem alterar o contrato das rotas, exceto os itens 6, 12 e 13, que dependem de mudança no esquema.

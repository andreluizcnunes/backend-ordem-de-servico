package br.com.marwil.ordemservico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.marwil.ordemservico.dto.response.LancamentoCaixaResponse;
import br.com.marwil.ordemservico.entity.LancamentoCaixa;

@Mapper(uses = ReferenciaMapper.class)
public interface LancamentoCaixaMapper {

	@Mapping(target = "tipo", constant = "ENTRADA")
	@Mapping(target = "origem", constant = "ORDEM_SERVICO")
	@Mapping(target = "pagamentoId", source = "pagamento.id")
	@Mapping(target = "cobrancaId", source = "pagamento.cobranca.id")
	@Mapping(target = "ordemServicoId", source = "pagamento.cobranca.ordemServico.id")
	@Mapping(target = "cliente", source = "pagamento.cobranca.ordemServico.cliente")
	LancamentoCaixaResponse paraResponse(LancamentoCaixa lancamento);

}

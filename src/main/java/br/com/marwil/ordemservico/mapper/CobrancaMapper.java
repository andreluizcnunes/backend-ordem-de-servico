package br.com.marwil.ordemservico.mapper;

import java.time.LocalDate;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.marwil.ordemservico.dto.response.CobrancaResponse;
import br.com.marwil.ordemservico.dto.response.PagamentoResponse;
import br.com.marwil.ordemservico.entity.Cobranca;
import br.com.marwil.ordemservico.entity.Pagamento;

@Mapper(uses = ReferenciaMapper.class)
public interface CobrancaMapper {

	@Mapping(target = "ordemServicoId", source = "ordemServico.id")
	@Mapping(target = "cliente", source = "ordemServico.cliente")
	@Mapping(target = "vencida", expression = "java(cobranca.estaVencida(hoje))")
	CobrancaResponse paraResponse(Cobranca cobranca, @Context LocalDate hoje);

	PagamentoResponse paraPagamento(Pagamento pagamento);

}

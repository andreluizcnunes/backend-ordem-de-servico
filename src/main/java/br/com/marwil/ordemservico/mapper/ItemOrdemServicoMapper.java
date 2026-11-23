package br.com.marwil.ordemservico.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import br.com.marwil.ordemservico.dto.request.ItemOrdemServicoRequest;
import br.com.marwil.ordemservico.dto.response.ItemOrdemServicoResponse;
import br.com.marwil.ordemservico.entity.ItemOrdemServico;

@Mapper
public interface ItemOrdemServicoMapper {

	ItemOrdemServicoResponse paraResponse(ItemOrdemServico item);

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "tipo")
	@Mapping(target = "descricao")
	@Mapping(target = "localRealizacao")
	@Mapping(target = "quantidade")
	@Mapping(target = "precoUnitario")
	ItemOrdemServico paraEntidade(ItemOrdemServicoRequest requisicao);

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "tipo")
	@Mapping(target = "descricao")
	@Mapping(target = "localRealizacao")
	@Mapping(target = "quantidade")
	@Mapping(target = "precoUnitario")
	void atualizar(ItemOrdemServicoRequest requisicao, @MappingTarget ItemOrdemServico item);

}

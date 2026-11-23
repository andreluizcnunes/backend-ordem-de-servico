package br.com.marwil.ordemservico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import br.com.marwil.ordemservico.dto.request.ClienteRequest;
import br.com.marwil.ordemservico.dto.response.ClienteResponse;
import br.com.marwil.ordemservico.entity.Cliente;

@Mapper
public interface ClienteMapper {

	ClienteResponse paraResponse(Cliente cliente);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "criadoPor", ignore = true)
	Cliente paraEntidade(ClienteRequest requisicao);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "criadoPor", ignore = true)
	void atualizar(ClienteRequest requisicao, @MappingTarget Cliente cliente);

}

package br.com.marwil.ordemservico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import br.com.marwil.ordemservico.dto.request.UsuarioAtualizacaoRequest;
import br.com.marwil.ordemservico.dto.request.UsuarioRequest;
import br.com.marwil.ordemservico.dto.response.UsuarioLogadoResponse;
import br.com.marwil.ordemservico.dto.response.UsuarioResponse;
import br.com.marwil.ordemservico.entity.Usuario;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;

@Mapper
public interface UsuarioMapper {

	UsuarioResponse paraResponse(Usuario usuario);

	UsuarioLogadoResponse paraLogado(UsuarioAutenticado usuario);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "senhaHash", ignore = true)
	@Mapping(target = "ativo", constant = "true")
	Usuario paraEntidade(UsuarioRequest requisicao);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "senhaHash", ignore = true)
	void atualizar(UsuarioAtualizacaoRequest requisicao, @MappingTarget Usuario usuario);

}

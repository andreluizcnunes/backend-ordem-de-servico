package br.com.marwil.ordemservico.mapper;

import org.mapstruct.Mapper;

import br.com.marwil.ordemservico.dto.response.ReferenciaResponse;
import br.com.marwil.ordemservico.entity.Cliente;
import br.com.marwil.ordemservico.entity.Usuario;

@Mapper
public interface ReferenciaMapper {

	ReferenciaResponse deUsuario(Usuario usuario);

	ReferenciaResponse deCliente(Cliente cliente);

}

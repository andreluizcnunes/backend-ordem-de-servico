package br.com.marwil.ordemservico.mapper;

import org.mapstruct.Mapper;

import br.com.marwil.ordemservico.dto.response.RegistroAtendimentoResponse;
import br.com.marwil.ordemservico.entity.RegistroAtendimento;

@Mapper(uses = ReferenciaMapper.class)
public interface RegistroAtendimentoMapper {

	RegistroAtendimentoResponse paraResponse(RegistroAtendimento registro);

}

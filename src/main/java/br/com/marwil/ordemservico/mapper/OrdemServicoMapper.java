package br.com.marwil.ordemservico.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import br.com.marwil.ordemservico.dto.request.OrdemServicoAtualizacaoRequest;
import br.com.marwil.ordemservico.dto.request.OrdemServicoRequest;
import br.com.marwil.ordemservico.dto.response.OrdemServicoResponse;
import br.com.marwil.ordemservico.dto.response.OrdemServicoResumoResponse;
import br.com.marwil.ordemservico.dto.response.ReferenciaResponse;
import br.com.marwil.ordemservico.dto.response.TecnicoAtribuidoResponse;
import br.com.marwil.ordemservico.entity.OrdemServico;
import br.com.marwil.ordemservico.entity.OrdemServicoTecnico;
import br.com.marwil.ordemservico.enums.StatusCobranca;

@Mapper(uses = { ClienteMapper.class, ReferenciaMapper.class, RegistroAtendimentoMapper.class })
public interface OrdemServicoMapper {

	@Mapping(target = "id", source = "ordemServico.id")
	@Mapping(target = "statusCobranca", source = "statusCobranca")
	OrdemServicoResponse paraResponse(OrdemServico ordemServico, StatusCobranca statusCobranca);

	@Mapping(target = "id", source = "ordemServico.id")
	@Mapping(target = "statusCobranca", source = "statusCobranca")
	OrdemServicoResumoResponse paraResumo(OrdemServico ordemServico, StatusCobranca statusCobranca);

	@Mapping(target = "id", source = "tecnico.id")
	@Mapping(target = "nome", source = "tecnico.nome")
	TecnicoAtribuidoResponse paraTecnicoAtribuido(OrdemServicoTecnico atribuicao);

	@Mapping(target = "id", source = "tecnico.id")
	@Mapping(target = "nome", source = "tecnico.nome")
	ReferenciaResponse paraReferenciaTecnico(OrdemServicoTecnico atribuicao);

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "dataAbertura")
	@Mapping(target = "descricao")
	@Mapping(target = "enderecoAtendimento")
	@Mapping(target = "contatoAtendimento")
	@Mapping(target = "telefoneAtendimento")
	@Mapping(target = "emailAtendimento")
	OrdemServico paraEntidade(OrdemServicoRequest requisicao);

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "descricao")
	@Mapping(target = "enderecoAtendimento")
	@Mapping(target = "contatoAtendimento")
	@Mapping(target = "telefoneAtendimento")
	@Mapping(target = "emailAtendimento")
	void atualizar(OrdemServicoAtualizacaoRequest requisicao, @MappingTarget OrdemServico ordemServico);

}

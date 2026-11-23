package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;

@Service
public class EquipeService {

	private final OrdemServicoRepository ordens;
	private final EquipeRepository equipes;

	public EquipeService(OrdemServicoRepository ordens, EquipeRepository equipes) {
		this.ordens = ordens;
		this.equipes = equipes;
	}

	@Transactional
	public void assumir(UsuarioAutenticado tecnico, long ordemServicoId) {
		EstadoOrdemServico estado = ordens.bloquearParaAlteracao(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (estado.statusAtendimento() != StatusAtendimento.ABERTA || equipes.possuiTecnicos(ordemServicoId)) {
			throw new ConflitoException("A ordem de serviço não está mais disponível para ser assumida.");
		}
		equipes.atribuir(ordemServicoId, tecnico.id(), tecnico.id());
	}

	@Transactional
	public void definirEquipe(UsuarioAutenticado responsavel, long ordemServicoId, Collection<Long> tecnicosIds) {
		EstadoOrdemServico estado = ordens.bloquearParaAlteracao(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (!estado.statusAtendimento().emCicloOperacional()) {
			throw new ConflitoException("A equipe só pode ser alterada antes da conclusão da ordem de serviço.");
		}
		Set<Long> desejados = new LinkedHashSet<>(tecnicosIds);
		Set<Long> atuais = equipes.idsDaEquipe(ordemServicoId);
		Set<Long> novos = new LinkedHashSet<>(desejados);
		novos.removeAll(atuais);
		garantirTecnicosAtivos(novos);
		Set<Long> removidos = new LinkedHashSet<>(atuais);
		removidos.removeAll(desejados);
		equipes.remover(ordemServicoId, removidos);
		novos.forEach(tecnicoId -> equipes.atribuir(ordemServicoId, tecnicoId, responsavel.id()));
	}

	@Transactional
	public void atribuirNaAbertura(UsuarioAutenticado responsavel, long ordemServicoId, Collection<Long> tecnicosIds) {
		Set<Long> tecnicos = new LinkedHashSet<>(tecnicosIds);
		garantirTecnicosAtivos(tecnicos);
		tecnicos.forEach(tecnicoId -> equipes.atribuir(ordemServicoId, tecnicoId, responsavel.id()));
	}

	private void garantirTecnicosAtivos(Set<Long> tecnicosIds) {
		Set<Long> validos = equipes.filtrarTecnicosAtivos(tecnicosIds);
		if (validos.size() != tecnicosIds.size()) {
			Set<Long> invalidos = new LinkedHashSet<>(tecnicosIds);
			invalidos.removeAll(validos);
			throw new RequisicaoInvalidaException(
					"Os usuários " + invalidos + " não são técnicos ativos e não podem integrar a equipe.");
		}
	}

}

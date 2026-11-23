package br.com.marwil.ordemservico.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.dto.request.EquipeRequest;
import br.com.marwil.ordemservico.entity.OrdemServico;
import br.com.marwil.ordemservico.entity.OrdemServicoTecnico;
import br.com.marwil.ordemservico.entity.Usuario;
import br.com.marwil.ordemservico.enums.Perfil;
import br.com.marwil.ordemservico.enums.StatusAtendimento;
import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.repository.OrdemServicoRepository;
import br.com.marwil.ordemservico.repository.OrdemServicoTecnicoRepository;
import br.com.marwil.ordemservico.repository.UsuarioRepository;
import br.com.marwil.ordemservico.security.UsuarioAutenticado;

@Service
public class EquipeService {

	private final OrdemServicoRepository ordens;
	private final OrdemServicoTecnicoRepository atribuicoes;
	private final UsuarioRepository usuarios;
	private final Clock relogio;

	public EquipeService(OrdemServicoRepository ordens, OrdemServicoTecnicoRepository atribuicoes,
			UsuarioRepository usuarios, Clock relogio) {
		this.ordens = ordens;
		this.atribuicoes = atribuicoes;
		this.usuarios = usuarios;
		this.relogio = relogio;
	}

	@Transactional
	public void assumir(UsuarioAutenticado tecnico, Long ordemServicoId) {
		OrdemServico ordem = ordens.findComBloqueioById(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (ordem.getStatusAtendimento() != StatusAtendimento.ABERTA
				|| atribuicoes.existsByOrdemServicoId(ordemServicoId)) {
			throw new ConflitoException("A ordem de serviço não está mais disponível para ser assumida.");
		}
		Usuario usuario = usuarios.getReferenceById(tecnico.id());
		ordem.getTecnicos().add(new OrdemServicoTecnico(ordem, usuario, usuario, OffsetDateTime.now(relogio)));
	}

	@Transactional
	public void definirEquipe(UsuarioAutenticado responsavel, Long ordemServicoId, EquipeRequest requisicao) {
		OrdemServico ordem = ordens.findComBloqueioById(ordemServicoId)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (ordem.getStatusAtendimento() == StatusAtendimento.CONCLUIDA) {
			throw new ConflitoException("A equipe só pode ser alterada antes da conclusão da ordem de serviço.");
		}
		Set<Long> desejados = new LinkedHashSet<>(requisicao.tecnicosIds());
		ordem.getTecnicos().removeIf(atribuicao -> !desejados.contains(atribuicao.getTecnico().getId()));
		Set<Long> atuais = ordem.getTecnicos().stream()
				.map(atribuicao -> atribuicao.getTecnico().getId())
				.collect(Collectors.toSet());
		Set<Long> novos = new LinkedHashSet<>(desejados);
		novos.removeAll(atuais);
		atribuir(responsavel, ordem, novos);
	}

	@Transactional
	public void atribuirNaAbertura(UsuarioAutenticado responsavel, OrdemServico ordem, Collection<Long> tecnicosIds) {
		atribuir(responsavel, ordem, new LinkedHashSet<>(tecnicosIds));
	}

	private void atribuir(UsuarioAutenticado responsavel, OrdemServico ordem, Set<Long> tecnicosIds) {
		if (tecnicosIds.isEmpty()) {
			return;
		}
		Map<Long, Usuario> tecnicos = usuarios.findByIdInAndPerfilAndAtivoTrue(tecnicosIds, Perfil.TECNICO).stream()
				.collect(Collectors.toMap(Usuario::getId, Function.identity()));
		List<Long> invalidos = tecnicosIds.stream().filter(id -> !tecnicos.containsKey(id)).toList();
		if (!invalidos.isEmpty()) {
			throw new RequisicaoInvalidaException(
					"Os usuários " + invalidos + " não são técnicos ativos e não podem integrar a equipe.");
		}
		Usuario atribuidoPor = usuarios.getReferenceById(responsavel.id());
		OffsetDateTime agora = OffsetDateTime.now(relogio);
		tecnicosIds.forEach(id -> ordem.getTecnicos()
				.add(new OrdemServicoTecnico(ordem, tecnicos.get(id), atribuidoPor, agora)));
	}

}

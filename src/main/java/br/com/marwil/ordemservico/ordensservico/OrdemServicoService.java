package br.com.marwil.ordemservico.ordensservico;

import br.com.marwil.ordemservico.enums.StatusAtendimento;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marwil.ordemservico.auth.UsuarioAutenticado;
import br.com.marwil.ordemservico.clientes.Cliente;
import br.com.marwil.ordemservico.clientes.ClienteRepository;
import br.com.marwil.ordemservico.exception.ConflitoException;
import br.com.marwil.ordemservico.comum.Pagina;
import br.com.marwil.ordemservico.comum.Paginacao;
import br.com.marwil.ordemservico.exception.RequisicaoInvalidaException;
import br.com.marwil.ordemservico.util.Textos;
import br.com.marwil.ordemservico.comum.UsuarioResumido;

@Service
public class OrdemServicoService {

	private final OrdemServicoRepository ordens;
	private final EquipeRepository equipes;
	private final RegistroAtendimentoRepository registros;
	private final ClienteRepository clientes;
	private final AcessoOrdemServico acesso;
	private final EquipeService equipeService;

	public OrdemServicoService(OrdemServicoRepository ordens, EquipeRepository equipes,
			RegistroAtendimentoRepository registros, ClienteRepository clientes, AcessoOrdemServico acesso,
			EquipeService equipeService) {
		this.ordens = ordens;
		this.equipes = equipes;
		this.registros = registros;
		this.clientes = clientes;
		this.acesso = acesso;
		this.equipeService = equipeService;
	}

	@Transactional
	public long abrir(UsuarioAutenticado responsavel, DadosOrdemServico.Abertura dados) {
		Cliente cliente = clientes.buscarPorId(dados.clienteId())
				.orElseThrow(() -> new RequisicaoInvalidaException("O cliente informado não existe."));
		String endereco = Textos.primeiroPreenchido(dados.enderecoAtendimento(), cliente.endereco());
		if (endereco == null) {
			throw new RequisicaoInvalidaException(
					"Informe o endereço de atendimento ou cadastre o endereço do cliente.");
		}
		long id = ordens.inserir(new OrdemServicoRepository.NovaOrdemServico(
				cliente.id(),
				dados.dataAbertura(),
				Textos.obrigatorio(dados.descricao()),
				endereco,
				Textos.primeiroPreenchido(dados.contatoAtendimento(), cliente.contato()),
				Textos.primeiroPreenchido(dados.telefoneAtendimento(), cliente.telefone()),
				Textos.primeiroPreenchido(dados.emailAtendimento(), cliente.email()),
				responsavel.id()));
		equipeService.atribuirNaAbertura(responsavel, id, dados.tecnicosOuVazio());
		return id;
	}

	@Transactional
	public void atualizar(long id, DadosOrdemServico.Atualizacao dados) {
		EstadoOrdemServico estado = ordens.bloquearParaAlteracao(id)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada);
		if (estado.cobrancaEnviada()) {
			throw new ConflitoException("A ordem de serviço já foi enviada à cobrança e não pode ser editada.");
		}
		ordens.atualizarDados(id,
				Textos.obrigatorio(dados.descricao()),
				Textos.obrigatorio(dados.enderecoAtendimento()),
				Textos.opcional(dados.contatoAtendimento()),
				Textos.opcional(dados.telefoneAtendimento()),
				Textos.opcional(dados.emailAtendimento()));
	}

	@Transactional(readOnly = true)
	public OrdemServicoDetalhe detalhar(UsuarioAutenticado usuario, long id) {
		acesso.garantirVisivel(usuario, id);
		OrdemServicoDetalhe detalhe = ordens.buscarDetalhe(id)
				.orElseThrow(AcessoOrdemServico::ordemNaoEncontrada)
				.comEquipeERegistros(equipes.listar(id), registros.listar(id));
		return usuario.ehGestor() ? detalhe : detalhe.semDadosFinanceiros();
	}

	@Transactional(readOnly = true)
	public Optional<OrdemServicoDetalhe> detalharSeVisivel(UsuarioAutenticado usuario, long id) {
		if (usuario.ehTecnico() && !ordens.visivelAoTecnico(id, usuario.id())) {
			return Optional.empty();
		}
		return Optional.of(detalhar(usuario, id));
	}

	@Transactional(readOnly = true)
	public Pagina<ResumoOrdemServico> listar(UsuarioAutenticado usuario, FiltroOrdensServico filtro,
			Paginacao paginacao) {
		if (usuario.ehGestor()) {
			return comEquipes(usuario, ordens.listarParaGestor(filtro, paginacao), paginacao,
					ordens.contarParaGestor(filtro));
		}
		return comEquipes(usuario,
				ordens.listarVisiveisAoTecnico(usuario.id(), filtro.statusAtendimento(), paginacao), paginacao,
				ordens.contarVisiveisAoTecnico(usuario.id(), filtro.statusAtendimento()));
	}

	@Transactional(readOnly = true)
	public Pagina<ResumoOrdemServico> listarDisponiveis(UsuarioAutenticado usuario, Paginacao paginacao) {
		return comEquipes(usuario, ordens.listarDisponiveis(paginacao), paginacao, ordens.contarDisponiveis());
	}

	@Transactional(readOnly = true)
	public Pagina<ResumoOrdemServico> listarDoTecnico(UsuarioAutenticado tecnico, StatusAtendimento status,
			Paginacao paginacao) {
		return comEquipes(tecnico, ordens.listarDoTecnico(tecnico.id(), status, paginacao), paginacao,
				ordens.contarDoTecnico(tecnico.id(), status));
	}

	private Pagina<ResumoOrdemServico> comEquipes(UsuarioAutenticado usuario, List<ResumoOrdemServico> resumos,
			Paginacao paginacao, long total) {
		Map<Long, List<UsuarioResumido>> equipesPorOrdem = equipes
				.listarPorOrdens(resumos.stream().map(ResumoOrdemServico::id).toList());
		List<ResumoOrdemServico> completos = resumos.stream()
				.map(resumo -> resumo.comTecnicos(equipesPorOrdem.getOrDefault(resumo.id(), List.of())))
				.map(resumo -> usuario.ehGestor() ? resumo : resumo.semDadosFinanceiros())
				.toList();
		return Pagina.de(completos, paginacao, total);
	}

}

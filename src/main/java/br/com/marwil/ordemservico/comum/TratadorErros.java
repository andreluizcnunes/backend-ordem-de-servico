package br.com.marwil.ordemservico.comum;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class TratadorErros extends ResponseEntityExceptionHandler {

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException excecao) {
		return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", excecao.getMessage());
	}

	@ExceptionHandler(ConflitoException.class)
	ProblemDetail tratarConflito(ConflitoException excecao) {
		return problema(HttpStatus.CONFLICT, "Conflito", excecao.getMessage());
	}

	@ExceptionHandler(RequisicaoInvalidaException.class)
	ProblemDetail tratarRequisicaoInvalida(RequisicaoInvalidaException excecao) {
		return problema(HttpStatus.BAD_REQUEST, "Requisição inválida", excecao.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail tratarAcessoNegado(AccessDeniedException excecao) {
		return problema(HttpStatus.FORBIDDEN, "Acesso negado", "O perfil do usuário não permite esta operação.");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail tratarViolacaoIntegridade(DataIntegrityViolationException excecao) {
		logger.warn("Violação de integridade no banco de dados", excecao);
		return problema(HttpStatus.CONFLICT, "Conflito",
				"A operação viola uma regra de integridade dos dados e não foi aplicada.");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException excecao,
			HttpHeaders cabecalhos, HttpStatusCode status, WebRequest requisicao) {
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"Um ou mais campos possuem valores inválidos.");
		List<Map<String, String>> erros = excecao.getBindingResult().getFieldErrors().stream()
				.map(TratadorErros::descreverErroCampo)
				.toList();
		problema.setProperty("erros", erros);
		return handleExceptionInternal(excecao, problema, cabecalhos, HttpStatus.BAD_REQUEST, requisicao);
	}

	private static Map<String, String> descreverErroCampo(FieldError erro) {
		String mensagem = erro.getDefaultMessage() == null ? "valor inválido" : erro.getDefaultMessage();
		return Map.of("campo", erro.getField(), "mensagem", mensagem);
	}

	private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
		problema.setTitle(titulo);
		return problema;
	}

}

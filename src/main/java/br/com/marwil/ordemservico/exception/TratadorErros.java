package br.com.marwil.ordemservico.exception;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

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

	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail tratarFalhaAutenticacao(AuthenticationException excecao) {
		return problema(HttpStatus.UNAUTHORIZED, "Não autenticado", "E-mail ou senha inválidos.");
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

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException excecao,
			HttpHeaders cabecalhos, HttpStatusCode status, WebRequest requisicao) {
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"O corpo da requisição está ausente ou não é um JSON válido.");
		MismatchedInputException erro = entradaIncompativel(excecao);
		if (erro != null && !erro.getPath().isEmpty()) {
			problema.setDetail("Um ou mais campos possuem valores inválidos.");
			problema.setProperty("erros", List.of(Map.of(
					"campo", caminho(erro.getPath()),
					"mensagem", mensagemTipoInvalido(erro.getTargetType()))));
		}
		return handleExceptionInternal(excecao, problema, cabecalhos, HttpStatus.BAD_REQUEST, requisicao);
	}

	private static MismatchedInputException entradaIncompativel(Throwable excecao) {
		for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
			if (causa instanceof MismatchedInputException erro) {
				return erro;
			}
		}
		return null;
	}

	private static String caminho(List<JacksonException.Reference> referencias) {
		StringBuilder caminho = new StringBuilder();
		for (JacksonException.Reference referencia : referencias) {
			if (referencia.getPropertyName() != null) {
				if (!caminho.isEmpty()) {
					caminho.append('.');
				}
				caminho.append(referencia.getPropertyName());
			} else if (referencia.getIndex() >= 0) {
				caminho.append('[').append(referencia.getIndex()).append(']');
			}
		}
		return caminho.toString();
	}

	private static String mensagemTipoInvalido(Class<?> tipo) {
		if (tipo == null) {
			return "Valor inválido.";
		}
		if (tipo.isEnum()) {
			String aceitos = Arrays.stream(tipo.getEnumConstants())
					.map(constante -> ((Enum<?>) constante).name())
					.collect(Collectors.joining(", "));
			return "Valor inválido. Valores aceitos: " + aceitos + ".";
		}
		if (Number.class.isAssignableFrom(tipo) || tipo == long.class || tipo == int.class) {
			return "Informe um número válido, sem aspas e com ponto como separador decimal.";
		}
		if (tipo == LocalDate.class) {
			return "Informe uma data válida no formato AAAA-MM-DD.";
		}
		if (tipo == OffsetDateTime.class) {
			return "Informe data e hora válidas no formato ISO-8601 com fuso, por exemplo 2026-11-24T08:30:00-04:00.";
		}
		if (tipo == Boolean.class || tipo == boolean.class) {
			return "Informe true ou false.";
		}
		if (Collection.class.isAssignableFrom(tipo)) {
			return "Informe uma lista.";
		}
		if (tipo == String.class) {
			return "Informe um texto.";
		}
		return "Valor inválido.";
	}

	private static Map<String, String> descreverErroCampo(FieldError erro) {
		String mensagem = erro.getDefaultMessage() == null ? "Valor inválido." : erro.getDefaultMessage();
		return Map.of("campo", erro.getField(), "mensagem", mensagem);
	}

	private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
		problema.setTitle(titulo);
		return problema;
	}

}

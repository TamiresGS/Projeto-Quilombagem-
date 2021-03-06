package service;


import java.util.Optional;
import java.nio.charset.Charset;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;



import model.Usuario;
import model.UsuarioDTO;
import repository.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository repository;

	/**
	 * Método utilizado para cadastrar um usuario no banco de dados, o mesmo é
	 * responsavel por retornar vazio caso Usuario exista
	 * 
	 * @param novoUsuario do tipo Usuario
	 * @return Usuario Criado quando não existir no banco
	 *
	 */	public Optional<Object> cadastrarUsuario(Usuario novoUsuario) {
			return repository.findByEmail(novoUsuario.getEmail()).map(usuarioExistente -> {
				return Optional.empty();
			}).orElseGet(() -> {
				BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
				String senhaCriptografada = encoder.encode(novoUsuario.getSenha());
				novoUsuario.setSenha(senhaCriptografada);
				return Optional.ofNullable(repository.save(novoUsuario));
			});
		}
	
	 
	 /**
		 * Metodo utilizado para pegar credenciais do usuario com Tokem (Formato Basic),
		 * este método sera utilizado para retornar ao front o token utilizado para ter
		 * acesso aos dados do usuario e mantelo logado no sistema
		 * 
		 * @param usuarioParaAutenticar do tipo UsuarioLoginDTO necessario email e senha
		 *                              para validar
		 * @return UsuarioLoginDTO preenchido com informações mais o Token
		 */
	 
	 
		public Optional<?> pegarCredenciais(UsuarioDTO usuarioParaAutenticar) {
			return repository.findByEmail(usuarioParaAutenticar.getEmail()).map(usuarioExistente -> {
				BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

				if (encoder.matches(usuarioParaAutenticar.getSenha(), usuarioExistente.getSenha())) {
					String estruturaBasic = usuarioParaAutenticar.getEmail() + ":" + usuarioParaAutenticar.getSenha();
					byte[] autorizacaoBase64 = Base64.encodeBase64(estruturaBasic.getBytes(Charset.forName("US-ASCII")));
					String autorizacaoHeader = "Basic " + new String(autorizacaoBase64);

					usuarioParaAutenticar.setToken(autorizacaoHeader);
					usuarioParaAutenticar.setId(usuarioExistente.getId());
					usuarioParaAutenticar.setNomeCompleto(usuarioExistente.getNomeCompleto());
					usuarioParaAutenticar.setSenha(usuarioExistente.getSenha());
					usuarioParaAutenticar.setTipoUsuario(usuarioExistente.getTipoUsuario());
					return Optional.ofNullable(usuarioParaAutenticar);
				} else {
					return Optional.empty();
				}
			}).orElseGet(() -> {
				return Optional.empty();
			});
		}
		
		
		/**
		 * Metodo utilizado para alterar um usuario fornecido pelo FRONT, O mesmo
		 * retorna um Optional com entidade Usuario dentro e senha criptografada. Caso
		 * falho retorna um Optional.empty()
		 * 
		 * @param usuarioParaAlterar do tipo Usuario
		 * @return Optional com Usuario Alterado
		 */
		
		public Optional<?> alterarUsuario(UsuarioDTO usuarioParaAlterar) {
			return repository.findById(usuarioParaAlterar.getId()).map(usuarioExistente -> {
				BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
				String senhaCriptografada = encoder.encode(usuarioParaAlterar.getSenha());

				usuarioExistente.setNomeCompleto(usuarioParaAlterar.getNomeCompleto());
				usuarioExistente.setSenha(senhaCriptografada);
				return Optional.ofNullable(repository.save(usuarioExistente));
			}).orElseGet(() -> {
				return Optional.empty();
			});
		}
}




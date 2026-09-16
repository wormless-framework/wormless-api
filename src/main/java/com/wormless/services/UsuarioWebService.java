package com.wormless.services;

import com.wormless.entities.UsuarioWeb;
import com.wormless.exception.BusinessException;
import com.wormless.exception.ResourceNotFoundException;
import com.wormless.repository.UsuarioWebRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioWebService {

    private final UsuarioWebRepository usuarioWebRepository;

    @Transactional
    public UsuarioWeb salvar(UsuarioWeb usuario) {

        if (usuario == null) {
            throw new BusinessException(
                    "O usuário não pode ser nulo."
            );
        }

        if (usuario.getNome() == null
                || usuario.getNome().isBlank()) {
            throw new BusinessException(
                    "O nome do usuário deve ser informado."
            );
        }

        if (usuario.getEmail() == null
                || usuario.getEmail().isBlank()) {
            throw new BusinessException(
                    "O e-mail do usuário deve ser informado."
            );
        }

        if (usuarioWebRepository.existsByEmail(usuario.getEmail())) {
            throw new BusinessException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        return usuarioWebRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioWeb buscarPorId(Long id) {

        if (id == null) {
            throw new BusinessException(
                    "O ID do usuário deve ser informado."
            );
        }

        return usuarioWebRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com o ID: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<UsuarioWeb> listarTodos() {
        return usuarioWebRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UsuarioWeb buscarPorEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new BusinessException(
                    "O e-mail deve ser informado."
            );
        }

        return usuarioWebRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com o e-mail: " + email
                ));
    }

    @Transactional
    public UsuarioWeb atualizar(
            Long id,
            UsuarioWeb usuario) {

        if (id == null) {
            throw new BusinessException(
                    "O ID do usuário deve ser informado."
            );
        }

        if (usuario == null) {
            throw new BusinessException(
                    "O usuário não pode ser nulo."
            );
        }

        UsuarioWeb usuarioExistente = buscarPorId(id);

        if (usuario.getNome() == null
                || usuario.getNome().isBlank()) {
            throw new BusinessException(
                    "O nome do usuário deve ser informado."
            );
        }

        if (usuario.getEmail() == null
                || usuario.getEmail().isBlank()) {
            throw new BusinessException(
                    "O e-mail do usuário deve ser informado."
            );
        }

        if (!usuarioExistente.getEmail().equals(usuario.getEmail())
                && usuarioWebRepository.existsByEmail(
                        usuario.getEmail())) {
            throw new BusinessException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        usuarioExistente.setNome(usuario.getNome());
        usuarioExistente.setEmail(usuario.getEmail());

        return usuarioWebRepository.save(usuarioExistente);
    }

    @Transactional
    public void excluir(Long id) {

        if (id == null) {
            throw new BusinessException(
                    "O ID do usuário deve ser informado."
            );
        }

        if (!usuarioWebRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Usuário não encontrado com o ID: " + id
            );
        }

        usuarioWebRepository.deleteById(id);
    }
}

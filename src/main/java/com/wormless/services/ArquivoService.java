package com.wormless.services;

import com.wormless.entities.Arquivo;
import com.wormless.exception.BusinessException;
import com.wormless.exception.ResourceNotFoundException;
import com.wormless.repository.ArquivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArquivoService {

    private final ArquivoRepository arquivoRepository;

    @Transactional
    public Arquivo salvar(Arquivo arquivo) {

        if (arquivo == null) {
            throw new BusinessException(
                    "O arquivo não pode ser nulo."
            );
        }

        if (!arquivo.validarFormato()) {
            throw new BusinessException(
                    "Formato de arquivo não permitido."
            );
        }

        if (!arquivo.validarTamanho()) {
            throw new BusinessException(
                    "Tamanho de arquivo excede o limite permitido."
            );
        }

        return arquivoRepository.save(arquivo);
    }

    @Transactional(readOnly = true)
    public Arquivo buscarPorId(Long id) {

        if (id == null) {
            throw new BusinessException(
                    "O ID do arquivo deve ser informado."
            );
        }

        return arquivoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Arquivo não encontrado com o ID: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Arquivo> listarTodos() {

        return arquivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Arquivo> buscarPorNome(String nome) {

        if (nome == null || nome.isBlank()) {
            throw new BusinessException(
                    "O nome do arquivo deve ser informado."
            );
        }

        List<Arquivo> arquivos =
                arquivoRepository.findByNomeOriginal(nome);

        if (arquivos.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Nenhum arquivo encontrado com o nome: " + nome
            );
        }

        return arquivos;
    }

    @Transactional(readOnly = true)
    public List<Arquivo> listarPorUsuario(Long usuarioWebId) {

        if (usuarioWebId == null) {
            throw new BusinessException(
                    "O ID do usuário deve ser informado."
            );
        }

        return arquivoRepository.findByUsuarioWebId(usuarioWebId);
    }

    @Transactional
    public void excluir(Long id) {

        if (id == null) {
            throw new BusinessException(
                    "O ID do arquivo deve ser informado."
            );
        }

        if (!arquivoRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Arquivo não encontrado com o ID: " + id
            );
        }

        arquivoRepository.deleteById(id);
    }
}
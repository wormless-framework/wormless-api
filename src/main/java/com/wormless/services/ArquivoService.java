package com.wormless.services;

import com.wormless.entities.Arquivo;
import com.wormless.repository.ArquivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ArquivoService {

    private final ArquivoRepository arquivoRepository;

    public ArquivoService(ArquivoRepository arquivoRepository) {
        this.arquivoRepository = arquivoRepository;
    }

    public Arquivo salvar(Arquivo arquivo) {
        validarArquivo(arquivo);
        return arquivoRepository.save(arquivo);
    }

    @Transactional(readOnly = true)
    public Arquivo buscarPorId(Long id) {
        return arquivoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Arquivo não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Arquivo> listarTodos() {
        return arquivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Arquivo> buscarPorNome(String nome) {
        return arquivoRepository
                .findByNomeOriginalContainingIgnoreCase(nome);
    }

    public void excluir(Long id) {
        if (!arquivoRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Arquivo não encontrado: " + id);
        }

        arquivoRepository.deleteById(id);
    }

    private void validarArquivo(Arquivo arquivo) {
        if (!arquivo.validarFormato()) {
            throw new IllegalArgumentException(
                    "Formato de arquivo inválido.");
        }

        if (!arquivo.validarTamanho()) {
            throw new IllegalArgumentException(
                    "Tamanho de arquivo inválido.");
        }
    }
}
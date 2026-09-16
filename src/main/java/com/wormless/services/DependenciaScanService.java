package com.wormless.services;

import com.wormless.entities.DependenciaScan;
import com.wormless.exception.BusinessException;
import com.wormless.exception.ResourceNotFoundException;
import com.wormless.repository.DependenciaScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DependenciaScanService {

    private final DependenciaScanRepository dependenciaScanRepository;

    @Transactional
    public DependenciaScan salvar(DependenciaScan dependenciaScan) {

        if (dependenciaScan == null) {
            throw new BusinessException(
                    "A dependência não pode ser nula."
            );
        }

        return dependenciaScanRepository.save(dependenciaScan);
    }

    @Transactional(readOnly = true)
    public DependenciaScan buscarPorId(Long id) {

        if (id == null) {
            throw new BusinessException(
                    "O ID da dependência deve ser informado."
            );
        }

        return dependenciaScanRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Dependência não encontrada com o ID: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<DependenciaScan> listarPorJob(Long jobId) {

        if (jobId == null) {
            throw new BusinessException(
                    "O ID da análise deve ser informado."
            );
        }

        return dependenciaScanRepository.findByAnaliseJobId(jobId);
    }

    @Transactional(readOnly = true)
    public List<DependenciaScan> buscarPacote(String nomePacote) {

        if (nomePacote == null || nomePacote.isBlank()) {
            throw new BusinessException(
                    "O nome do pacote deve ser informado."
            );
        }

        List<DependenciaScan> dependencias =
                dependenciaScanRepository.findByNomePacote(nomePacote);

        if (dependencias.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Nenhum pacote encontrado: " + nomePacote
            );
        }

        return dependencias;
    }

    @Transactional(readOnly = true)
    public List<DependenciaScan> buscarPacoteEVersao(
            String nomePacote,
            String versao) {

        if (nomePacote == null || nomePacote.isBlank()) {
            throw new BusinessException(
                    "O nome do pacote deve ser informado."
            );
        }

        if (versao == null || versao.isBlank()) {
            throw new BusinessException(
                    "A versão do pacote deve ser informada."
            );
        }

        List<DependenciaScan> dependencias =
                dependenciaScanRepository.findByNomePacoteAndVersao(
                        nomePacote,
                        versao
                );

        if (dependencias.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Dependência não encontrada: "
                            + nomePacote
                            + " versão "
                            + versao
            );
        }

        return dependencias;
    }
}
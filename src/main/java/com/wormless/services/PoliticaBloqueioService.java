package com.wormless.services;

import com.wormless.entities.PoliticaBloqueio;
import com.wormless.entities.RelatorioAmeaca;
import com.wormless.exception.BusinessException;
import com.wormless.exception.ResourceNotFoundException;
import com.wormless.repository.PoliticaBloqueioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PoliticaBloqueioService {

    private final PoliticaBloqueioRepository politicaBloqueioRepository;

    @Transactional
    public PoliticaBloqueio salvar(
            PoliticaBloqueio politicaBloqueio) {

        if (politicaBloqueio == null) {
            throw new BusinessException(
                    "A política de bloqueio não pode ser nula."
            );
        }

        if (politicaBloqueio.getLimiteSeveridade() == null) {
            throw new BusinessException(
                    "O limite de severidade deve ser informado."
            );
        }

        return politicaBloqueioRepository.save(politicaBloqueio);
    }

    @Transactional(readOnly = true)
    public PoliticaBloqueio buscarPorId(Long id) {

        return politicaBloqueioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Política de bloqueio não encontrada com o ID: "
                                        + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public boolean avaliar(
            Long politicaId,
            RelatorioAmeaca relatorio) {

        if (politicaId == null) {
            throw new BusinessException(
                    "O ID da política deve ser informado."
            );
        }

        if (relatorio == null) {
            throw new BusinessException(
                    "O relatório deve ser informado para avaliação."
            );
        }

        PoliticaBloqueio politica = buscarPorId(politicaId);

        return politica.avaliar(relatorio);
    }
}
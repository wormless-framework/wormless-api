package com.wormless.services;

import org.springframework.stereotype.Service;

import com.wormless.entities.AnaliseJob;
import com.wormless.entities.RelatorioAmeaca;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class AnaliseIAServiceImpl implements AnaliseIAService {

    @Override
    public RelatorioAmeaca analisar(
            String resultadoBruto,
            AnaliseJob analiseJob) {

        // interpretar resultado do Sandbox
        // chamar IA
        // montar RelatorioAmeaca

        return null;
    }
}
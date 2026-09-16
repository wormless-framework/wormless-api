package com.wormless.services;

import com.wormless.entities.AnaliseJob;
import com.wormless.entities.RelatorioAmeaca;

public interface AnaliseIAService {

    RelatorioAmeaca analisar(
            String resultadoBruto,
            AnaliseJob analiseJob
    );
}
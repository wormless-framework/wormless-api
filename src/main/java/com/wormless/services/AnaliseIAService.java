package com.wormless.services;

import com.wormless.entities.RelatorioAmeaca;

public interface AnaliseIAService {

    RelatorioAmeaca interpretarResultado(
            String resultadoBruto
    );
}

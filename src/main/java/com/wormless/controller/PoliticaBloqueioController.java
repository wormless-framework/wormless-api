package com.wormless.controller;

import com.wormless.entities.PoliticaBloqueio;
import com.wormless.services.PoliticaBloqueioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/politicas-bloqueio")
@RequiredArgsConstructor
public class PoliticaBloqueioController {

    private final PoliticaBloqueioService politicaBloqueioService;

    // Os endpoints dependem dos métodos existentes
    // no PoliticaBloqueioService.
}
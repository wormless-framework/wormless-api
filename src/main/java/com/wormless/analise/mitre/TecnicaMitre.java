package com.wormless.analise.mitre;

/**
 * Subconjunto do catálogo MITRE ATT&CK (Enterprise) relevante para ameaças
 * entregues por documentos, executáveis e arquivos compactados.
 *
 * <p>Cada achado do motor pode ser atribuído a uma técnica, o que permite
 * montar a matriz de cobertura do relatório e dar contexto tático à análise
 * feita pela IA.</p>
 */
public enum TecnicaMitre {

    T1036_005("T1036.005", "Masquerading: Match Legitimate Name or Location", "Defense Evasion"),
    T1036_007("T1036.007", "Masquerading: Double File Extension", "Defense Evasion"),
    T1027("T1027", "Obfuscated Files or Information", "Defense Evasion"),
    T1027_002("T1027.002", "Obfuscated Files or Information: Software Packing", "Defense Evasion"),
    T1055("T1055", "Process Injection", "Defense Evasion"),
    T1622("T1622", "Debugger Evasion", "Defense Evasion"),

    T1566_002("T1566.002", "Phishing: Spearphishing Link", "Initial Access"),

    T1059_004("T1059.004", "Command and Scripting Interpreter: Unix Shell", "Execution"),
    T1059_005("T1059.005", "Command and Scripting Interpreter: Visual Basic", "Execution"),
    T1059_007("T1059.007", "Command and Scripting Interpreter: JavaScript", "Execution"),
    T1203("T1203", "Exploitation for Client Execution", "Execution"),
    T1204_002("T1204.002", "User Execution: Malicious File", "Execution"),
    T1559_002("T1559.002", "Inter-Process Communication: Dynamic Data Exchange", "Execution"),

    T1056_001("T1056.001", "Input Capture: Keylogging", "Collection"),

    T1071_001("T1071.001", "Application Layer Protocol: Web Protocols", "Command and Control"),
    T1105("T1105", "Ingress Tool Transfer", "Command and Control"),

    T1187("T1187", "Forced Authentication", "Credential Access");

    private final String id;
    private final String nome;
    private final String tatica;

    TecnicaMitre(String id, String nome, String tatica) {
        this.id = id;
        this.nome = nome;
        this.tatica = tatica;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTatica() {
        return tatica;
    }

    /**
     * Rótulo curto usado nos relatórios: {@code T1059.007 - Command and
     * Scripting Interpreter: JavaScript (Execution)}.
     */
    public String getRotulo() {
        return id + " - " + nome + " (" + tatica + ")";
    }
}

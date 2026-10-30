package com.medsafe.service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** Le o campo horariosProgramados ("08:00,20:00"); entradas invalidas de registros antigos sao ignoradas. */
final class Horarios {

    private Horarios() {
    }

    static List<LocalTime> interpretar(String horariosProgramados) {
        TreeSet<LocalTime> horarios = new TreeSet<>();
        if (horariosProgramados == null) {
            return new ArrayList<>();
        }
        for (String texto : horariosProgramados.split(",")) {
            try {
                horarios.add(LocalTime.parse(texto.trim()));
            } catch (DateTimeParseException ignorado) {
                // registro antigo fora do formato HH:mm: nao gera dose
            }
        }
        return new ArrayList<>(horarios);
    }
}

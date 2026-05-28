package br.com.oficina.oficina.model;

import java.util.EnumSet;
import java.util.Set;

public enum StatusAtendimento {

    AGUARDANDO {
        @Override
        public Set<StatusAtendimento> transicoesPermitidas() {
            return EnumSet.of(ANDAMENTO, CANCELADO);
        }
    },
    ANDAMENTO {
        @Override
        public Set<StatusAtendimento> transicoesPermitidas() {
            return EnumSet.of(CONCLUIDO, CANCELADO);
        }
    },
    CONCLUIDO {
        @Override
        public Set<StatusAtendimento> transicoesPermitidas() {
            return EnumSet.noneOf(StatusAtendimento.class); // terminal
        }
    },
    CANCELADO {
        @Override
        public Set<StatusAtendimento> transicoesPermitidas() {
            return EnumSet.noneOf(StatusAtendimento.class); // terminal
        }
    };

    public abstract Set<StatusAtendimento> transicoesPermitidas();

    public boolean podeTransicionarPara(StatusAtendimento destino) {
        return transicoesPermitidas().contains(destino);
    }
}

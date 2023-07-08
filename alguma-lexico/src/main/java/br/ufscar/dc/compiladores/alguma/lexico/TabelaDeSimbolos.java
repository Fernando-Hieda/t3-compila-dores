package br.ufscar.dc.compiladores.alguma.lexico;

import java.util.HashMap;
import java.util.Map;


public class TabelaDeSimbolos {
    public TabelaDeSimbolos.TipoAlguma returnType;
    
    public enum TipoAlguma {
        literal,
        inteiro,
        real,
        logico,
        tipo,
        var,
        invalido,
        cadeia,
    }

    class EntradaTabelaDeSimbolos {
        String nome;
        TipoAlguma tipo;

        private EntradaTabelaDeSimbolos(String nome, TipoAlguma tipo) {
            this.nome = nome;
            this.tipo = tipo;
        }
    }

    private final Map<String, EntradaTabelaDeSimbolos> tabela;

    public TabelaDeSimbolos() {
        this.tabela = new HashMap<>();
    }

    public TabelaDeSimbolos(TabelaDeSimbolos.TipoAlguma returnType) {
        this.tabela = new HashMap<>();
        this.returnType = returnType;
    }

    public void adicionar(String nome, TipoAlguma tipo) {
        tabela.put(nome, new EntradaTabelaDeSimbolos(nome, tipo));
    }

    public boolean existe(String nome) {
        return tabela.containsKey(nome);
    }

    public TipoAlguma verificar(String nome) {
        if(tabela.containsKey(nome))
            return tabela.get(nome).tipo;
        return null;
    }

    public boolean exists(String name){
        return tabela.containsKey(name); 
    }
}





package br.ufscar.dc.compiladores.alguma.lexico;

import java.util.ArrayList;
import java.util.HashMap;

public class TabelaDeSimbolos {
    public TabelaDeSimbolos.TipoAlguma returnType;
    
    public enum TipoAlguma {
        literal, //
        inteiro,
        real,
        logico,
        invalido,
        cadeia,
        reg,
        var,
        constante,
        proc,
        func,
        tipo,
        Void
    }

    class EntradaTabelaDeSimbolos {
        String nome;
        TipoAlguma tipo;

        private EntradaTabelaDeSimbolos(String nome, TipoAlguma tipo) {
            this.nome = nome;
            this.tipo = tipo;
        }
    }

    private final HashMap<String, EntradaTabelaDeSimbolos> tabela;
    private final HashMap<String, ArrayList<EntradaTabelaDeSimbolos>> tipoTabela;

    public TabelaDeSimbolos() {
        tabela = new HashMap<>();
        tipoTabela = new HashMap<>();
    }

    public TabelaDeSimbolos(TabelaDeSimbolos.TipoAlguma returnType) {
        this.tabela = new HashMap<>();
        tipoTabela = new HashMap<>();
        this.returnType = returnType;
    }

    public void adicionar(String nome, TipoAlguma tipo) {
        EntradaTabelaDeSimbolos entrada = new EntradaTabelaDeSimbolos(nome, tipo);
        tabela.put(nome, entrada);
    }

    public void adicionar(EntradaTabelaDeSimbolos entrada) {
        tabela.put(entrada.nome, entrada);
    }

    public void adicionar(String tipoNome, EntradaTabelaDeSimbolos entrada){
        if(tipoTabela.containsKey(tipoNome)){
            tipoTabela.get(tipoNome).add(entrada);
        }else{
            ArrayList<EntradaTabelaDeSimbolos> list = new ArrayList<>();
            list.add(entrada);
            tipoTabela.put(tipoNome, list);
        }
    }

    public boolean existe(String nome) {
        return tabela.containsKey(nome);
    }

    public TipoAlguma verificar(String nome) {
        if(tabela.containsKey(nome))
            return tabela.get(nome).tipo;
        return null;
    }

    public ArrayList<EntradaTabelaDeSimbolos> getTypeProperties(String name){
        return tipoTabela.get(name);
    }
}





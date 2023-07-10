package br.ufscar.dc.compiladores.alguma.lexico;

import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.CmdAtribuicaoContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.CmdRetorneContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.Declaracao_constanteContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.Declaracao_tipoContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.Declaracao_variavelContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.IdentificadorContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.Parcela_unarioContext;
import br.ufscar.dc.compiladores.alguma.lexico.TabelaDeSimbolos.EntradaTabelaDeSimbolos;
import br.ufscar.dc.compiladores.alguma.lexico.TabelaDeSimbolos.TipoAlguma;

public class AlgumaSemantico extends AlgumaBaseVisitor<Void> {

    TabelaDeSimbolos tabela;

    PilhaTabelas pilhadeTabelas = new PilhaTabelas(TabelaDeSimbolos.TipoAlguma.Void);
    
    @Override
    public Void visitPrograma(AlgumaParser.ProgramaContext ctx) {
        return super.visitPrograma(ctx);
    }

    //declaracao_tipo
    @Override
    public Void visitDeclaracao_tipo(Declaracao_tipoContext ctx) {
        TabelaDeSimbolos escopoAtual = pilhadeTabelas.getPilhaTabelas();
        
        if (escopoAtual.existe(ctx.IDENT().getText())) {
             AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText() + " declarado duas vezes num mesmo escopo");
        } else {
            TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.tipo().getText());
            if (tipo != null) {
                escopoAtual.adicionar(ctx.IDENT().getText(), tipo);
            } else {
                String nameVar = ctx.IDENT().getText();
                if (escopoAtual.existe (nameVar)) {
                    AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "identificador " + nameVar
                            + " ja declarado anteriormente");
                }
                else {
                    // SemanticoUtils.adicionarErroSemantico(id.start, "oi rs tamo adicionando " + re.name );
                    escopoAtual.adicionar(ctx.IDENT().getText(), TipoAlguma.invalido);
                }
            }
            TabelaDeSimbolos.TipoAlguma t =  AlgumaSemanticoUtils.getTipo(ctx.tipo().getText());
            escopoAtual.adicionar(ctx.IDENT().getText(), t);
        }
        return super.visitDeclaracao_tipo(ctx);
    }

    //ao declarar variavel, verificamos se o identificador é novo, caso positivo podemos salvar
    @Override
    public Void visitDeclaracao_variavel(Declaracao_variavelContext ctx) {
        TabelaDeSimbolos escopoAtual = pilhadeTabelas.getPilhaTabelas();
        for (IdentificadorContext id : ctx.variavel().identificador()) {
            String nomeId = "";
            int i = 0;
            for (TerminalNode ident : id.IDENT()) {
                if (i++ > 0){
                    nomeId += ".";
                }
                nomeId += ident.getText();
            }
            if (escopoAtual.existe(nomeId)) {
                AlgumaSemanticoUtils.adicionarErroSemantico(id.start, "identificador " + nomeId + " ja declarado anteriormente");
            } else {
                TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.variavel().tipo().getText());
                if (tipo != null) {
                    escopoAtual.adicionar(nomeId, tipo);
                } else {
                    escopoAtual.adicionar(nomeId, TipoAlguma.invalido);
                }
            }
        }
        return super.visitDeclaracao_variavel(ctx);
    }

    //declaracao constante
    @Override
    public Void visitDeclaracao_constante(Declaracao_constanteContext ctx) {
        TabelaDeSimbolos escopoAtual = pilhadeTabelas.getPilhaTabelas();
        if (escopoAtual.existe(ctx.IDENT().getText())) {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "constante" + ctx.IDENT().getText() + " ja declarado anteriormente");
        } else {
            TabelaDeSimbolos.TipoAlguma tipo = TabelaDeSimbolos.TipoAlguma.inteiro;
            TabelaDeSimbolos.TipoAlguma aux = AlgumaSemanticoUtils.getTipo(ctx.tipo_basico().getText()) ;
            if (aux != null)
                tipo = aux;
            escopoAtual.adicionar(ctx.IDENT().getText(), tipo);
        }

        return super.visitDeclaracao_constante(ctx);
    }

    //tipo-basico
    @Override
    public Void visitTipo_basico_ident(AlgumaParser.Tipo_basico_identContext ctx) {

        if (ctx.IDENT() != null) {
           boolean existe = false;
           for (TabelaDeSimbolos tabela : pilhadeTabelas.getPilha()) {
                if (tabela.existe(ctx.IDENT().getText())) {
                    existe = true;
                }
           }
           if (!existe) {
                AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText() + " nao declarado");
           }
        }

        return super.visitTipo_basico_ident(ctx);
    }

    //identificador
    public Void visitIdentificador(IdentificadorContext ctx) {
        String nomeVar = "";
        int i = 0;
        for (TerminalNode id : ctx.IDENT()){
            if (i++ > 0)
                nomeVar += ".";
            nomeVar += id.getText();
        }
        boolean erro = true;
        for (TabelaDeSimbolos escopo : pilhadeTabelas.getPilha()) {

            if (escopo.existe(nomeVar)) {
                erro = false;
            }
        }
        if (erro)
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "identificador " + nomeVar + " nao declarado");
        return super.visitIdentificador(ctx);
    }

    //para casos de atribuição
    @Override
    public Void visitCmdAtribuicao(CmdAtribuicaoContext ctx) {
        TabelaDeSimbolos.TipoAlguma tipoExpressao = AlgumaSemanticoUtils.verificarTipo(pilhadeTabelas, ctx.expressao());
        boolean error = false;
        String pointerChar = ctx.getText().charAt(0) == '^' ? "^" : "";
        String nomeVar = "";
        int i = 0;

        for (TerminalNode id : ctx.identificador().IDENT()){
            if (i++ > 0)
                nomeVar += ".";
            nomeVar += id.getText();
        }
        if (tipoExpressao != TabelaDeSimbolos.TipoAlguma.invalido) {
            boolean found = false;
            for (TabelaDeSimbolos escopo : pilhadeTabelas.getPilha()){
                if (escopo.existe(nomeVar) && !found)  {
                    found = true;
                    TabelaDeSimbolos.TipoAlguma tipoVariavel = AlgumaSemanticoUtils.verificarTipo(pilhadeTabelas, nomeVar);
                    Boolean varNumeric = tipoVariavel == TabelaDeSimbolos.TipoAlguma.real || tipoVariavel == TabelaDeSimbolos.TipoAlguma.inteiro;
                    Boolean expNumeric = tipoExpressao == TabelaDeSimbolos.TipoAlguma.real || tipoExpressao == TabelaDeSimbolos.TipoAlguma.inteiro;
                    if (!(varNumeric && expNumeric) && tipoVariavel != tipoExpressao && tipoExpressao != TabelaDeSimbolos.TipoAlguma.invalido) {
                        error = true;
                    }
                } 
            }
        } else {
            error = true;
        }

        if (error) {
            nomeVar = ctx.identificador().getText();
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + pointerChar + nomeVar );
        }

        return super.visitCmdAtribuicao(ctx);
    }

    //comando de retorno
    @Override
    public Void visitCmdRetorne(CmdRetorneContext ctx) {
        if (pilhadeTabelas.getPilhaTabelas().returnType == TabelaDeSimbolos.TipoAlguma.Void){
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "comando retorne nao permitido nesse escopo");
        } 
        return super.visitCmdRetorne(ctx);
    }

    //para parcela unarios
    @Override
    public Void visitParcela_unario(Parcela_unarioContext ctx) {
        TabelaDeSimbolos escopoAtual = pilhadeTabelas.getPilhaTabelas();
        if (ctx.IDENT() != null) {
            String name = ctx.IDENT().getText();
            if (escopoAtual.existe(ctx.IDENT().getText())) {
                List<EntradaTabelaDeSimbolos> params = escopoAtual.getTypeProperties(name);
                boolean error = false;
                if (params.size() != ctx.expressao().size()) {
                    error = true;
                } else {
                    for(int i = 0; i < params.size(); i++){
                        if(params.get(i).tipo != AlgumaSemanticoUtils.verificarTipo(pilhadeTabelas, ctx.expressao().get(i))){
                            error = true;
                        }
                    }
                }
                if (error) {
                    AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "incompatibilidade de parametros na chamada de " + name);
                }
            }
        }

        return super.visitParcela_unario(ctx);
    }
}
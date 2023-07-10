package br.ufscar.dc.compiladores.alguma.lexico;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.CmdAtribuicaoContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.CmdRetorneContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.Declaracao_constanteContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.Declaracao_globalContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.Declaracao_tipoContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.Declaracao_variavelContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.IdentificadorContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.ParametroContext;
import br.ufscar.dc.compiladores.alguma.lexico.AlgumaParser.Parcela_unarioContext;
import br.ufscar.dc.compiladores.alguma.lexico.TabelaDeSimbolos.EntradaTabelaDeSimbolos;

public class AlgumaSemantico extends AlgumaBaseVisitor<Void> {

    TabelaDeSimbolos tabela;

    PilhaTabelas pilhadeTabelas = new PilhaTabelas(TabelaDeSimbolos.TipoAlguma.Void);
    
    @Override
    public Void visitPrograma(AlgumaParser.ProgramaContext ctx) {
        return super.visitPrograma(ctx);
    }

     @Override
    public Void visitDeclaracao_tipo(Declaracao_tipoContext ctx) {
        TabelaDeSimbolos escopoAtual = pilhadeTabelas.getPilhaTabelas();
        
        if (escopoAtual.existe(ctx.IDENT().getText())) {
             AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText() + " declarado duas vezes num mesmo escopo");
        } else {
            TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.tipo().getText());
            if(tipo != null) {
                escopoAtual.adicionar(ctx.IDENT().getText(), tipo);
            }
            TabelaDeSimbolos.TipoAlguma t =  AlgumaSemanticoUtils.getTipo(ctx.tipo().getText());
            escopoAtual.adicionar(ctx.IDENT().getText(), t);
        }
        return super.visitDeclaracao_tipo(ctx);
    }

    //ao declarar variavel, verificamos se o identificador é novo, caso positivo podemos salvar
    //também devemos verificar caso seja um tipo registro para variavel, temos de associar essa
    //as variaveis do registro.
    @Override
    public Void visitDeclaracao_variavel(Declaracao_variavelContext ctx) {
        TabelaDeSimbolos escopoAtual = pilhadeTabelas.getPilhaTabelas();
        for (IdentificadorContext id : ctx.variavel().identificador()) {
            String nomeId = "";
            int i = 0;
            for(TerminalNode ident : id.IDENT()){
                if(i++ > 0)
                    nomeId += ".";
                nomeId += ident.getText();
            }
            if (escopoAtual.existe(nomeId)) {
                AlgumaSemanticoUtils.adicionarErroSemantico(id.start, "identificador " + nomeId + " ja declarado anteriormente");
            } else {
                TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.variavel().tipo().getText());
                if(tipo != null)
                    escopoAtual.adicionar(nomeId, tipo);
                else{
                    TerminalNode identTipo =    ctx.variavel().tipo() != null
                                                && ctx.variavel().tipo().tipo_estendido() != null 
                                                && ctx.variavel().tipo().tipo_estendido().tipo_basico_ident() != null  
                                                && ctx.variavel().tipo().tipo_estendido().tipo_basico_ident().IDENT() != null 
                                                ? ctx.variavel().tipo().tipo_estendido().tipo_basico_ident().IDENT() : null;
                    if(identTipo != null){
                        ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> regVars = null;
                        boolean found = false;
                        for(TabelaDeSimbolos t: pilhadeTabelas.getPilha()){
                            if(!found){
                                if(t.existe(identTipo.getText())){
                                    regVars = t.getTypeProperties(identTipo.getText());
                                    found = true;
                                }
                            }
                        }
                        if(escopoAtual.existe(nomeId)){
                            AlgumaSemanticoUtils.adicionarErroSemantico(id.start, "identificador " + nomeId + " ja declarado anteriormente");
                        }
                    }
                    else if(ctx.variavel().tipo().registro() != null){
                        ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> varReg = new ArrayList<>(); 
                        escopoAtual.adicionar(nomeId, TabelaDeSimbolos.TipoAlguma.reg);

                        for(TabelaDeSimbolos.EntradaTabelaDeSimbolos re : varReg){
                            String nomeVar = nomeId + '.' + re.nome;
                            if (escopoAtual.existe(nomeVar)) {
                                AlgumaSemanticoUtils.adicionarErroSemantico(id.start, "identificador " + nomeVar
                                        + " ja declarado anteriormente");
                            }
                            else{
                                // AlgumaSemanticoUtils.adicionarErroSemantico(id.start, "oi rs tamo adicionando " + re.nome );
                                escopoAtual.adicionar(re);
                                escopoAtual.adicionar(nomeVar, re.tipo);
                            }
                        }

                    }
                    else{//tipo registro estendido
                        escopoAtual.adicionar(id.getText(), TabelaDeSimbolos.TipoAlguma.inteiro);
                    }
                }
            }
        }
        return super.visitDeclaracao_variavel(ctx);
    }

    @Override
    public Void visitDeclaracao_constante(Declaracao_constanteContext ctx) {
        TabelaDeSimbolos escopoAtual = pilhadeTabelas.getPilhaTabelas();
        if (escopoAtual.existe(ctx.IDENT().getText())) {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "constante" + ctx.IDENT().getText() + " ja declarado anteriormente");
        } else {
            TabelaDeSimbolos.TipoAlguma tipo = TabelaDeSimbolos.TipoAlguma.inteiro;
            TabelaDeSimbolos.TipoAlguma aux = AlgumaSemanticoUtils.getTipo(ctx.tipo_basico().getText()) ;
            if(aux != null)
                tipo = aux;
            escopoAtual.adicionar(ctx.IDENT().getText(), tipo);
        }

        return super.visitDeclaracao_constante(ctx);
    }

    //Para casos de função temos de declarar elas
    //tratando o fator de que o nome é único e suas variaveis também devem ter nome único,
    //para isso criamos um escopo para a função e tratamos as variaveis sendo criadas lá, para não interferirem em outros escopos
    @Override
    public Void visitDeclaracao_global(Declaracao_globalContext ctx) {
        TabelaDeSimbolos escopoAtual = pilhadeTabelas.getPilhaTabelas();
        Object ret;
        if (escopoAtual.existe(ctx.IDENT().getText())) {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, ctx.IDENT().getText()
                    + " ja declarado anteriormente");
            ret = super.visitDeclaracao_global(ctx);
        } else {
            TabelaDeSimbolos.TipoAlguma returnTypeFunc = TabelaDeSimbolos.TipoAlguma.Void;
            if(ctx.getText().startsWith("funcao")){
                returnTypeFunc = AlgumaSemanticoUtils.getTipo(ctx.tipo_estendido().getText());
                escopoAtual.adicionar(ctx.IDENT().getText(), returnTypeFunc);
            }
            else{
                returnTypeFunc = TabelaDeSimbolos.TipoAlguma.Void;
                escopoAtual.adicionar(ctx.IDENT().getText(), returnTypeFunc);
            }
            pilhadeTabelas.create(returnTypeFunc);
            escopoAtual = pilhadeTabelas.getPilhaTabelas();
            if(ctx.parametros() != null){
                for(ParametroContext p : ctx.parametros().parametro()){
                    for (IdentificadorContext id : p.identificador()) {
                        String nomeId = "";
                        int i = 0;
                        for(TerminalNode ident : id.IDENT()){
                            if(i++ > 0)
                                nomeId += ".";
                            nomeId += ident.getText();
                        }
                        if (escopoAtual.existe(nomeId)) {
                            AlgumaSemanticoUtils.adicionarErroSemantico(id.start, "identificador " + nomeId
                                    + " ja declarado anteriormente");
                        } else {
                            TerminalNode identTipo =    p.tipo_estendido().tipo_basico_ident() != null  
                                                        && p.tipo_estendido().tipo_basico_ident().IDENT() != null 
                                                        ? p.tipo_estendido().tipo_basico_ident().IDENT() : null;
                            if(identTipo != null){
                                ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> regVars = null;
                                boolean found = false;
                                for(TabelaDeSimbolos t: pilhadeTabelas.getPilha()){
                                    if(!found){
                                        if(t.existe(identTipo.getText())){
                                            regVars = t.getTypeProperties(identTipo.getText());
                                            found = true;
                                        }
                                    }
                                }


                                AlgumaSemanticoUtils.adicionarErroSemantico(id.start, "identificador " + nomeId+ " ja declarado anteriormente");
                            }
                        }
                    }
                }
            }
            ret = super.visitDeclaracao_global(ctx);
            pilhadeTabelas.dropPilhaTabelas();

        }
        return super.visitDeclaracao_global(ctx);
    }



    @Override
    public Void visitTipo_basico_ident(AlgumaParser.Tipo_basico_identContext ctx) {

        // Verificar se a variável já foi declarada
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

    public Void visitIdentificador(IdentificadorContext ctx) {
        String nomeVar = "";
        int i = 0;
        for(TerminalNode id : ctx.IDENT()){
            if(i++ > 0)
                nomeVar += ".";
            nomeVar += id.getText();
        }
        boolean erro = true;
        for(TabelaDeSimbolos escopo : pilhadeTabelas.getPilha()) {

            if(escopo.existe(nomeVar)) {
                erro = false;
            }
        }
        if(erro)
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
        for(TerminalNode id : ctx.identificador().IDENT()){
            if(i++ > 0)
                nomeVar += ".";
            nomeVar += id.getText();
        }
        if (tipoExpressao != TabelaDeSimbolos.TipoAlguma.invalido) {
            boolean found = false;
            for(TabelaDeSimbolos escopo : pilhadeTabelas.getPilha()){
                if (escopo.existe(nomeVar) && !found)  {
                    found = true;
                    TabelaDeSimbolos.TipoAlguma tipoVariavel = AlgumaSemanticoUtils.verificarTipo(pilhadeTabelas, nomeVar);
                    Boolean varNumeric = tipoVariavel == TabelaDeSimbolos.TipoAlguma.real || tipoVariavel == TabelaDeSimbolos.TipoAlguma.inteiro;
                    Boolean expNumeric = tipoExpressao == TabelaDeSimbolos.TipoAlguma.real || tipoExpressao == TabelaDeSimbolos.TipoAlguma.inteiro;
                    if  (!(varNumeric && expNumeric) && tipoVariavel != tipoExpressao && tipoExpressao != TabelaDeSimbolos.TipoAlguma.invalido) {
                        error = true;
                    }
                } 
            }
        } else{
            error = true;
        }

        if(error){
            nomeVar = ctx.identificador().getText();
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + pointerChar + nomeVar );
        }

        return super.visitCmdAtribuicao(ctx);
    }

    //o comando de retorno deve ser diferente do tipo void que nao retorna nada
    @Override
    public Void visitCmdRetorne(CmdRetorneContext ctx) {
        if(pilhadeTabelas.getPilhaTabelas().returnType == TabelaDeSimbolos.TipoAlguma.Void){
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "comando retorne nao permitido nesse escopo");
        } 
        return super.visitCmdRetorne(ctx);
    }

    //para parcela unarios, verificamos se a variavel existe
    @Override
    public Void visitParcela_unario(Parcela_unarioContext ctx) {
        TabelaDeSimbolos escopoAtual = pilhadeTabelas.getPilhaTabelas();
        if(ctx.IDENT() != null){
            String name = ctx.IDENT().getText();
            if(escopoAtual.existe(ctx.IDENT().getText())){
                List<EntradaTabelaDeSimbolos> params = escopoAtual.getTypeProperties(name);
                boolean error = false;
                if(params.size() != ctx.expressao().size()){
                    error = true;
                } else {
                    for(int i = 0; i < params.size(); i++){
                        if(params.get(i).tipo != AlgumaSemanticoUtils.verificarTipo(pilhadeTabelas, ctx.expressao().get(i))){
                            error = true;
                        }
                    }
                }
                if(error){
                    AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "incompatibilidade de parametros na chamada de " + name);
                }
            }
        }

        return super.visitParcela_unario(ctx);
    }



    // @Override
    // public Void visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {
    //     TabelaDeSimbolos tabelaAtual = pilhadeTabelas.getPilhaTabelas();
        
    //     String strTipoVar = ctx.variavel().tipo().tipo_estendido().tipo_basico_ident().tipo_basico().getText();
    //     System.out.println(strTipoVar);
    //     String nomeVar = ctx.variavel().identificador(0).getText();
    //     System.out.println(nomeVar);
    //     TipoAlguma tipoVar = TipoAlguma.invalido;

    //     switch (strTipoVar) {
    //         case "literal":
    //             tipoVar = TipoAlguma.literal;
    //             break;
    //         case "inteiro":
    //             tipoVar = TipoAlguma.inteiro;
    //             break;
    //         case "real":
    //             tipoVar = TipoAlguma.real;
    //             break;
    //         case "logico":
    //             tipoVar = TipoAlguma.logico;
    //             break;
    //         case "tipo":
    //             tipoVar = TipoAlguma.tipo;
    //             break;
    //         case "var":
    //             tipoVar = TipoAlguma.var;
    //             break;
    //         default:
    //             // Nunca irá acontecer, pois o analisador sintático
    //             // não permite
    //             break;
    //     }

    //     // Verificar se a variável já foi declarada
    //     if (tabela.existe(nomeVar)) {
    //         AlgumaSemanticoUtils.adicionarErroSemantico(ctx.IDENT().getSymbol(), "Variável " + nomeVar + " já existe");
    //     } else {
    //         tabela.adicionar(nomeVar, tipoVar);
    //     }

    //     tabela.adicionar(nomeVar, tipoVar);

    //     return super.visitDeclaracao_local(ctx);
    // }

    // @Override
    // public Void visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
    //     TipoAlguma tipoExpressao = AlgumaSemanticoUtils.verificarTipo(pilha, ctx.expressao());
    //     if (tipoExpressao != TipoAlguma.invalido) {
    //         String nomeVar = ctx.identificador().getText();
    //         if (!tabela.existe(nomeVar)) {
    //             AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().getToken(), "Variável " + nomeVar + " não foi declarada antes do uso");
    //         } else {
    //             TipoAlguma tipoIDENT = AlgumaSemanticoUtils.verificarTipo(tabela, nomeVar);
    //             if (tipoIDENT != tipoExpressao) {
    //                 AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().getSymbol(), "Tipo da variável " + nomeVar + " não é compatível com o tipo da expressão");
    //             }
    //         }
    //     }
    //     return super.visitCmdAtribuicao(ctx);
    // }

    // @Override
    // public Void visitCmdLeia(AlgumaParser.CmdLeiaContext ctx) {
    //     String nomeVar = ctx.identificador().getText();
    //     if (!tabela.existe(nomeVar)) {
    //         AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().getSymbol(), "Variável " + nomeVar + " não foi declarada antes do uso");
    //     }
    //     return super.visitCmdLeia(ctx);
    // }

    // @Override
    // public Void visitExp_aritmetica(AlgumaParser.Exp_aritmeticaContext ctx) {
    //     AlgumaSemanticoUtils.verificarTipo(tabela, ctx);
    //     return super.visitExp_aritmetica(ctx);
    // }
}
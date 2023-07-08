package br.ufscar.dc.compiladores.alguma.lexico;

import br.ufscar.dc.compiladores.alguma.lexico.TabelaDeSimbolos.TipoAlguma;

public class AlgumaSemantico extends AlgumaBaseVisitor<Void> {

    TabelaDeSimbolos tabela;

    @Override
    public Void visitPrograma(AlgumaParser.ProgramaContext ctx) {
        tabela = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }

    @Override
    public Void visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {
        String nomeVar = ctx.variavel().getText(); ////erro aqui
        System.out.println(nomeVar);
        String strTipoVar = ctx.tipo().getText();
        System.out.println(strTipoVar);
        TipoAlguma tipoVar = TipoAlguma.invalido;
        switch (strTipoVar) {
            case "literal":
                tipoVar = TipoAlguma.literal;
                break;
            case "inteiro":
                tipoVar = TipoAlguma.inteiro;
                break;
            case "real":
                tipoVar = TipoAlguma.real;
                break;
            case "logico":
                tipoVar = TipoAlguma.logico;
                break;
            case "tipo":
                tipoVar = TipoAlguma.tipo;
                break;
            case "var":
                tipoVar = TipoAlguma.var;
                break;
            default:
                // Nunca irá acontecer, pois o analisador sintático
                // não permite
                break;
        }

        // Verificar se a variável já foi declarada
        if (tabela.existe(nomeVar)) {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.IDENT().getSymbol(), "Variável " + nomeVar + " já existe");
        } else {
            tabela.adicionar(nomeVar, tipoVar);
        }

        return super.visitDeclaracao_local(ctx);
    }

    // @Override
    // public Void visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
    //     TipoAlguma tipoExpressao = AlgumaSemanticoUtils.verificarTipo(tabela, ctx.expressao());
    //     if (tipoExpressao != TipoAlguma.invalido) {
    //         String nomeVar = ctx.identificador().getText();
    //         if (!tabela.existe(nomeVar)) {
    //             AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().getSymbol(), "Variável " + nomeVar + " não foi declarada antes do uso");
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
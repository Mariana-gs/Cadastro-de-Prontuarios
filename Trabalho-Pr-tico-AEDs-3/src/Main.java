import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

/**
 * Trabalho Prático de AEDs III - Parte 3
 * @author Mariana Galvão Soares
 */

public class Main {

    /**
     * Gera um conjunto de chaves entre 1 e 999999999, sem repetição
     * @param k  Quantidade de chaves
     * @return   Vetor de chaves
     */
    public static int[] gerarChaves(int k){
        boolean chavesB[] = new boolean[1000000000];
        int chaves[] = new int[k];
        int chave = 0;
        Random random = new Random();

        for(int i = 0; i < k; i++){
            chave = random.nextInt(1000000000);
            while(chavesB[chave] == true || chave == 0){
                chave = random.nextInt(1000000000);
                if(chavesB[chave] == false && chave != 0){
                    break;
                }
            }
                chaves[i] = chave;
                chavesB[chave] = true;

        }
        return chaves;
    }

    /**
     * Exibe o Menu com a opções para o usuário
     * @param caminho Caminho do Arquivo
     * @throws IOException
     */
    public static void Menu(String caminho, String caminhoSimulacao) throws IOException {

        Scanner e = new Scanner(System.in);
        int op = 0; //Opção
        int m;

        //Lê sistema de arquivos já existente
        HashExtensivel indice = new HashExtensivel(caminho);
        Arquivo sisArq = new Arquivo(caminho, indice);

        while (op != 7) {
            System.out.println("\n ==== Cadastro de Prontuários ==== ");
            System.out.println("1 - Criar Arquivo");
            System.out.println("2 - Inserir Registro");
            System.out.println("3 - Editar Registro");
            System.out.println("4 - Remover Registro");
            System.out.println("5 - Imprimir Arquivos");
            System.out.println("6 - Simulação");
            System.out.println("7 - Sair");
            System.out.print("Selecione uma opção: ");
            op = e.nextInt();

            String nome, nasc;
            int cpf = 0, aux;
            boolean sexo, cadastrado = true;
            long inicio, fim;


            switch (op) {

                /* Criar Arquivo */
                case 1:

                    int PG, EB;    //PG: Profundidade Global e EB: Elementos por Buckets

                    //Criando Índice
                    System.out.println("Informe a Profundidade Global do Diretório: ");
                    do {
                        PG = e.nextInt();
                        if(PG < 0){
                            System.out.println("Por favor, informe um número positivo: ");
                        }
                    }while (PG < 0);

                    System.out.println("Informe o número de entradas por Buckets: ");
                    do {
                        EB = e.nextInt();
                        if(EB < 0){
                            System.out.println("Por favor, informe um número positivo: ");
                        }
                    }while (EB < 0);

                    indice.criarIndice(PG, EB);
                    System.out.println("Índice Criado com Sucesso!");

                    //Criando Arquivo
                    System.out.println("Informe a quantidade de caracteres do campo de anotações: ");
                    do {
                        m = e.nextInt();
                        if(m < 0){
                            System.out.println("Por favor, informe um número positivo: ");
                        }
                    }while (m < 0);
                    sisArq.criarArquivo(m, indice);
                    System.out.println("Arquivo Criado com Sucesso!");
                    break;

                /* Inserir Registro */
                case 2:
                   cpf = 0;
                   cadastrado = true;
                    e.nextLine();
                    System.out.println("Por favor, informe os dados a seguir: ");
                    System.out.println("Nome: ");
                    nome = e.nextLine();
                    System.out.println("Data de Nascimento (dd/mm/aaaa): ");
                    nasc = e.nextLine();
                    while(cadastrado || cpf < 0) {
                        System.out.println("CPF: ");
                        cpf = e.nextInt();
                        if(cpf < 0){
                            System.out.println("Não foi possível inserir o Registro: Chave Negativa! \nTente novamente:");
                            continue;
                        }
                        if(sisArq.lerRegistro(cpf) == null){
                            cadastrado = false;
                        }else{
                            System.out.println("Não foi possível inserir o Registro: CPF já cadastrado! \nTente novamente:");
                        }
                    }
                    do {
                        System.out.println("Sexo: (0 - Masculino / 1 - Feminino)");
                        aux = e.nextInt();
                        if (aux == 0) sexo = false;
                        else sexo = true;
                    } while (aux != 0 && aux != 1);

                    inicio = System.currentTimeMillis();
                    if(sisArq.inserirRegistro(nome, nasc, cpf, sexo)){
                        fim = System.currentTimeMillis();

                        System.out.println("Registro Inserido com Sucesso!");
                        System.out.println("Tempo decorrido no processo: " + (fim - inicio) + " ms");

                    }else {
                        System.out.println("Não foi possível inserir o registro!");
                    }

                    break;

                /* Editar Registro */
                case 3:
                    String anotacao;
                    Prontuario p = new Prontuario();

                        System.out.println("Informe o CPF: ");
                        cpf = e.nextInt();
                        e.nextLine();

                    while(cpf < 0) {
                            System.out.println("Não foi possível buscar o Registro: Chave Negativa! \nTente novamente:");
                            cpf = e.nextInt();
                            e.nextLine();
                    }

                        p = sisArq.lerRegistro(cpf);

                        if(p != null){
                            System.out.println("Informe a nova anotação: ");
                            anotacao = e.nextLine();
                            p.setM(sisArq.getM());
                            p.setNotas(anotacao);

                            inicio = System.currentTimeMillis();
                            if(sisArq.editarRegistro(cpf, p)){
                                fim = System.currentTimeMillis();
                                System.out.println("Registro Editado com Sucesso!");
                                System.out.println("Tempo decorrido no processo: " + (fim - inicio) + " ms");
                            }else{
                                System.out.println("Não foi possível editar o Registro!");
                            }

                        }else{
                            System.out.println("Não será possível editar o Registro: CPF não cadastrado!");
                        }
                    break;

                /* Remover Registro */
                case 4:
                    System.out.println("Informe o CPF: ");
                    cpf = e.nextInt();
                    while(cpf < 0) {
                        System.out.println("Não foi possível buscar o Registro: Chave Negativa! \nTente novamente:");
                        cpf = e.nextInt();
                    }
                    if(sisArq.removerRegistro(cpf)){
                        System.out.println("Registro Removido com Sucesso!");
                    }else {
                        System.out.println("Não será possível remover o Registro: CPF não cadastrado!");
                    }
                    break;

                /* Imprimir Registros */
                case 5:
                    sisArq.imprimirArquivo();
                    break;

                /* Simulação */
                case 6:
                    FileWriter fw = new FileWriter(caminhoSimulacao + "dados.txt");
                    PrintWriter pw = new PrintWriter(fw);

                    HashExtensivel indiceSimulacao = new HashExtensivel(caminhoSimulacao);
                    Arquivo sisArqSimulacao = new Arquivo(caminhoSimulacao, indiceSimulacao);
                    Prontuario prontuario = new Prontuario();

                    System.out.println("\n == Iniciando Simulação == \n");

                    // k: quantidade de chaves que devem ser geradas
                    // EB: (n) elementos por bucket

                    int variacoes = 10;
                    int k = 1000;
                    int chaves[];
                    PG = 2;
                    EB = 340;
                    m = 300;
                    long tInsercao, tBusca;
                    pw.println("Quantidade de Chaves, Profundidade Global, Entradas por Bucket, Tamanho adicional do Registro, Tempo de Inserção, Tempo de Busca");

                    /* alterando quantidade de chaves */
                    System.out.println(" ====== PARTE 1 ====== ");
                    System.out.println("Variando quantidade de chaves: \n");
                    for(int i = 0; i < variacoes; i++){
                        System.out.println("Simulação #" + (i+1));
                        chaves = gerarChaves(k);

                        //Criar novo sistema de Arquivos
                        indiceSimulacao.criarIndice(PG, EB);
                        sisArqSimulacao.criarArquivo(m, indiceSimulacao);

                        /*Inserindo Chaves*/
                        inicio = System.currentTimeMillis();
                        for(int j = 0; j < k; j++){
                            sisArqSimulacao.inserirRegistro("Nome","01/01/2000", chaves[j],true);
                        }
                        fim = System.currentTimeMillis();
                        tInsercao = fim-inicio;
                        System.out.println( k + " chaves inseridas!");

                        /*Buscando chaves*/
                        inicio = System.currentTimeMillis();
                        for(int j = 0; j < k; j++){
                            prontuario = sisArqSimulacao.lerRegistro(chaves[j]);
                        }
                        fim = System.currentTimeMillis();
                        tBusca = fim-inicio;
                        System.out.println( k + " chaves recuperadas!");

                        pw.println(k + "," + PG + "," + EB + "," +  m  + "," + tInsercao + "," + tBusca);
                        k+=5000;
                    }

                    /* alterando profundidade global */
                    k = 1000;
                    PG = 2;
                    EB = 340;
                    m = 300;
                    chaves = gerarChaves(k);
                    System.out.println("\n ====== PARTE 2 ====== ");
                    System.out.println("Variando Profundidade Global: \n");
                    //alterando quantidade de chaves
                    for(int i = 0; i < variacoes; i++){
                        System.out.println("Simulação #" + (i+1));
                        System.out.println("Profundidade: " + PG);

                        //Criar novo sistema de Arquivos
                        indiceSimulacao.criarIndice(PG, EB);
                        sisArqSimulacao.criarArquivo(m, indiceSimulacao);

                        /*Inserindo Chaves*/
                        inicio = System.currentTimeMillis();
                        for(int j = 0; j < k; j++){
                            sisArqSimulacao.inserirRegistro("Nome","01/01/2000", chaves[j],true);
                        }
                        fim = System.currentTimeMillis();
                        tInsercao = fim-inicio;
                        System.out.println( k + " chaves inseridas!");

                        /*Buscando chaves*/
                        inicio = System.currentTimeMillis();
                        for(int j = 0; j < k; j++){
                            prontuario = sisArqSimulacao.lerRegistro(chaves[j]);
                        }
                        fim = System.currentTimeMillis();
                        tBusca = fim-inicio;
                        System.out.println( k + " chaves recuperadas!");

                        pw.println(k + "," + PG + "," + EB + "," +  m  + "," + tInsercao + "," + tBusca);
                        PG+=2;
                    }

                    k = 1000;
                    PG = 2;
                    EB = 340;
                    m = 300;
                    /* alterando número de entradas por bucket */
                    chaves = gerarChaves(k);
                    System.out.println("\n ====== PARTE 3 ====== ");
                    System.out.println("Variando Número de Entradas por Bucket: \n");

                    for(int i = 0; i < variacoes; i++){
                        System.out.println("Simulação #" + (i+1));
                        System.out.println("Número de Entradas por Bucket: " + EB);

                        //Criar novo sistema de Arquivos
                        indiceSimulacao.criarIndice(PG, EB);
                        sisArqSimulacao.criarArquivo(m, indiceSimulacao);

                        /*Inserindo Chaves*/
                        inicio = System.currentTimeMillis();
                        for(int j = 0; j < k; j++){
                            sisArqSimulacao.inserirRegistro("Nome","01/01/2000", chaves[j],true);
                        }
                        fim = System.currentTimeMillis();
                        tInsercao = fim-inicio;
                        System.out.println( k + " chaves inseridas!");

                        /*Buscando chaves*/
                       inicio = System.currentTimeMillis();
                        for(int j = 0; j < k; j++){
                            prontuario = sisArqSimulacao.lerRegistro(chaves[j]);
                        }
                        fim = System.currentTimeMillis();
                        tBusca = fim-inicio;
                        System.out.println( k + " chaves recuperadas!");

                        pw.println(k + "," + PG + "," + EB + "," +  m  + "," + tInsercao + "," + tBusca);
                        EB*=2;
                    }


                    k = 1000;
                    PG = 2;
                    EB = 340;
                    m = 300;
                    /* Alterando número de Tamanho adicional do Registro */
                    chaves = gerarChaves(k);
                    System.out.println("\n ====== PARTE 4 ====== ");
                    System.out.println("Variando Tamanho adicional do Registro: \n");

                    for(int i = 0; i < variacoes; i++){
                        System.out.println("Simulação #" + (i+1));
                        System.out.println("Tamanho adicional do Registro: " + m);

                        //Criar novo sistema de Arquivos
                        indiceSimulacao.criarIndice(PG, EB);
                        sisArqSimulacao.criarArquivo(m, indiceSimulacao);

                        /*Inserindo Chaves*/
                        inicio = System.currentTimeMillis();
                        for(int j = 0; j < k; j++){
                            sisArqSimulacao.inserirRegistro("Nome","01/01/2000", chaves[j],true);
                        }
                        fim = System.currentTimeMillis();
                        tInsercao = fim-inicio;
                        System.out.println( k + " chaves inseridas!");

                        /*Buscando chaves*/
                       inicio = System.currentTimeMillis();
                        for(int j = 0; j < k; j++){
                            prontuario = sisArqSimulacao.lerRegistro(chaves[j]);
                        }
                        fim = System.currentTimeMillis();
                        tBusca = fim-inicio;
                        System.out.println( k + " chaves recuperadas!");

                        pw.println(k + "," + PG + "," + EB + "," +  m  + "," + tInsercao + "," + tBusca);
                        m+=300;
                    }


                    k = 200000;
                    m = 6000;
                    PG = 4;
                    EB = 1000;

                    chaves = gerarChaves(k);
                    System.out.println("\n ====== Arquivo Mestre de 1GB ======");
                    System.out.println("Quantidade de Registros: " + k);
                    System.out.println("Tamanho adicional do Registro: " + m);

                    //Criar novo sistema de Arquivos
                    indiceSimulacao.criarIndice(PG, EB);
                    sisArqSimulacao.criarArquivo(m, indiceSimulacao);

                    /*Inserindo Chaves*/
                    inicio = System.currentTimeMillis();
                    for(int j = 0; j < k; j++){
                        sisArqSimulacao.inserirRegistro("Nome","01/01/2000", chaves[j],true);
                    }
                    fim = System.currentTimeMillis();
                    tInsercao = fim-inicio;
                    System.out.println( k + " chaves inseridas!");

                    /*Buscando chaves*/
                    inicio = System.currentTimeMillis();
                    for(int j = 0; j < k; j++){
                        prontuario = sisArqSimulacao.lerRegistro(chaves[j]);
                    }
                    fim = System.currentTimeMillis();
                    tBusca = fim-inicio;
                    System.out.println(k + " chaves recuperadas!");

                    pw.println(k + "," + PG + "," + EB + "," +  m  + "," + tInsercao + "," + tBusca);


                    System.out.println("\n\n == Fim da Simulação == ");

                    pw.close();
                    fw.close();
                    break;

                case 7:
                    System.out.println("Fim do programa!");
                    break;
                default:
                    System.out.println("Opção Inválida");
                    break;
            }
        }
        e.close();
    }

    public static void main(String[] args) {
        String caminho = "D:\\GitHub\\Cadastro-de-Prontuarios\\Trabalho-Pr-tico-AEDs-3\\src\\Arquivo\\";
        String caminhoSimulacao = "D:\\GitHub\\Cadastro-de-Prontuarios\\Trabalho-Pr-tico-AEDs-3\\src\\Simulacao\\";

        try {
            Menu(caminho, caminhoSimulacao);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

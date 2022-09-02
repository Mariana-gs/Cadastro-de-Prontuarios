import java.io.*;

public class HashExtensivel {

    private String caminhoDiretorio;      //Caminho do arquivo de diretório
    private String caminhoBuckets;        //Caminho do arquivo de buckets
    private Diretorio diretorio;          //Atributo do tipo Diretorio
    private int quantidade;               //Quantidade de registros suportado pelo bucket

    //Getters and Setters
    public String getCaminhoDiretorio() {
        return caminhoDiretorio;
    }
    public void setCaminhoDiretorio(String caminhoDiretorio) {
        this.caminhoDiretorio = caminhoDiretorio;
    }
    public String getCaminhoBuckets() {
        return caminhoBuckets;
    }
    public void setCaminhoBuckets(String caminhoBuckets) {
        this.caminhoBuckets = caminhoBuckets;
    }

    /**
     * Lê diretório já existente, se não existe cria um novo com valores
     * iniciais e cria um bucket vazio e escreve ambos nos arquivos
     * @param caminhoArquivo
     * @throws IOException
     */
    HashExtensivel(String caminhoArquivo) throws IOException {
        this.diretorio = new Diretorio(0);

        int TAM = 0;

        this.caminhoDiretorio = caminhoArquivo + "diretorio.db";
        this.caminhoBuckets = caminhoArquivo + "buckets.db";

        RandomAccessFile fileD = new RandomAccessFile(caminhoDiretorio, "rw");
        fileD.seek(0);
        RandomAccessFile fileB = new RandomAccessFile(caminhoBuckets, "rw");
        fileB.seek(0);

        try{

            //Lê o tamanho no arquivo do Diretorio
            fileD.seek(4);
            TAM = fileD.readInt();
            this.diretorio.setTamanho(TAM);

            byte b[] = new byte[8 + (TAM*8)];
            fileD.seek(0);
            fileD.read(b);


            this.diretorio.fromByteArray(b);
            //Lê a quantidade de dados suportados por bucket
            fileB.seek(this.diretorio.getDiretorio()[0] + 4);
            this.quantidade = fileB.readInt();

        }catch (EOFException e){
            //Se arquivo Vazio

            //Criando 1 Bucket Inicial com 1 posição e valor inválido
            this.quantidade = 1;
            Bucket bucketInicial = new Bucket(this.quantidade, 0);

            //Cabeçalho com a quantidade de buckets existentes
            fileB.writeInt(1);
            long endBucket = fileB.getFilePointer();
            fileB.write(bucketInicial.toByteArray());

            //Escreve novo diretório com profundidade global igual a 0
            this.diretorio.getDiretorio()[0] = endBucket;
            fileD.seek(0);
            fileD.write(this.diretorio.toByteArray());
        }
    }

    /**
     * Cria novo sistema de Arquivos para o Índice
     * @param PG Profundidade Global do Diretório
     * @param EB Quantidade de Entradas por Bucket
     */
    public void criarIndice(int PG, int EB) throws IOException {
        this.quantidade = EB;
        int qtdBuckets = (int) Math.pow(2,PG);
        long endBucket;

        //Criando Diretório
        this.diretorio = new Diretorio(PG);
        FileOutputStream fosD = new FileOutputStream(this.caminhoDiretorio);
        DataOutputStream dosD = new DataOutputStream(fosD);

        //Criando Bucket
        RandomAccessFile fileB = new RandomAccessFile(this.caminhoBuckets, "rw");
        Bucket bucket = new Bucket(this.quantidade, PG);
        fileB.writeInt(qtdBuckets);

        /* Coloca endereços no diretório */
        for(int i = 0; i < qtdBuckets; i++){
            endBucket = fileB.getFilePointer();
            this.diretorio.getDiretorio()[i] = endBucket;
            fileB.write(bucket.toByteArray());
        }

        dosD.write(this.diretorio.toByteArray());
    }

    /**
     * Insere Registro no índice
     * @param chave CPF
     * @param endArqMestre Endereço do registro no Arquivo Mestre
     * @return
     * @throws IOException
     */
    public boolean inserir(int chave, long endArqMestre) throws IOException { //end no arquivo de dados

        int qtdBuckets;
        byte b[];
        long endereco;
        int chaves[];

        FileOutputStream fosD = new FileOutputStream(this.caminhoDiretorio);
        DataOutputStream dosD = new DataOutputStream(fosD);

        RandomAccessFile file = new RandomAccessFile(this.caminhoBuckets, "rw");
        file.seek(0);
        qtdBuckets = file.readInt();
        Bucket bucket = new Bucket(this.quantidade, 0);

        //Busca endereço do Bucket no diretório
        endereco = this.diretorio.buscar(chave);
        file.seek(endereco);

        //Carrega o Bucket
        b = new byte[12+(12*this.quantidade)];
        file.read(b);
        bucket.fromByteArray(b);

        //Se já tem registro com mesma chave
        if(bucket.read(chave) != -1){
            return false;
        }

        if(!bucket.full()){
            bucket.inserir(chave, endArqMestre);
            file.seek(endereco);
            file.write(bucket.toByteArray());
            dosD.write(this.diretorio.toByteArray());
            return true;
        }

        if(this.diretorio.getPG() == bucket.getPL()) {
            this.diretorio.duplicar();
        }

            //Escreve o bucket com PL atualizado no lugar do anterior
            Bucket B1 = new Bucket(this.quantidade, (bucket.getPL()+1));
            file.seek(endereco);
            file.write(B1.toByteArray());

            //Escreve novo bucket no fim do arquivo
            Bucket B2 = new Bucket(this.quantidade, (bucket.getPL()+1));
            int fimArquivo = 4+(qtdBuckets*(12+(12*this.quantidade)));
            file.seek(fimArquivo);
            file.write(B2.toByteArray());
            this.diretorio.atualizarEndereco(endereco, fimArquivo);

            //Atualizar o cabeçalho com a quantidade de buckets existentes no arquivo
            qtdBuckets++;
            file.seek(0);
            file.writeInt(qtdBuckets);

            dosD.write(this.diretorio.toByteArray());

            //Reorganiza as Chaves do Bucket
            chaves = bucket.getCpfs();
            for(int i = 0; i < chaves.length; i++){
                inserir(chaves[i], bucket.read(chaves[i]));
            }

            inserir(chave, endArqMestre);

        return true;
    }

    /**
     * Busca no Índice o endereço do registro no Arquivo Mestre
     * @param chave CPF do registro
     * @return Endereço do registro no Arquivo Mestre
     * @throws IOException
     */
    public long buscar(int chave) throws IOException {
        long endArqMestre;
        byte b[];

        RandomAccessFile file = new RandomAccessFile(caminhoBuckets, "r");
        Bucket bucket = new Bucket(quantidade, 0);

        long endereco = this.diretorio.buscar(chave);
        file.seek(endereco);

        //Carrega o Bucket
        b = new byte[12+(12*this.quantidade)];
        file.read(b);
        bucket.fromByteArray(b);

        endArqMestre = bucket.read(chave);
        return endArqMestre;
    }

    /**
     * Deleta um registro do Índice
     * @param chave CPF do registro a ser deletado
     */
    public void delete(int chave) throws IOException {

        byte b[];

        RandomAccessFile file = new RandomAccessFile(caminhoBuckets, "rw");

        Bucket bucket = new Bucket(quantidade, 0);
        long endereco = this.diretorio.buscar(chave);
        file.seek(endereco);

        //Carrega o Bucket
        b = new byte[12+(12*this.quantidade)];
        file.read(b);
        bucket.fromByteArray(b);


        bucket.delete(chave);

        file.seek(endereco);
        file.write(bucket.toByteArray());
    }

    /**
     * Atualiza endereço (Arquivo Mestre) do registro no índice
     * @param chave CPF do registro
     * @param endNovo Novo endereço no Arquivo Mestre
     * @throws IOException
     */
    public void update(int chave, long endNovo) throws IOException {
        byte b[];
        RandomAccessFile file = new RandomAccessFile(caminhoBuckets, "rw");
        Bucket bucket = new Bucket(quantidade, 0);

        long endereco = this.diretorio.buscar(chave);
        file.seek(endereco);

        b = new byte[12+(12*this.quantidade)];
        file.read(b);
        bucket.fromByteArray(b);
        bucket.update(chave, endNovo);

        file.seek(endereco);
        file.write(bucket.toByteArray());

    }

    /**
     * Imprime o conteúdo dos Arquivos que compõem o Índice
     * @throws IOException
     */
    public void imprimirIndice() throws IOException {

        int qtdBuckets;
        long endBucket;
        byte b[];

        System.out.println("\n ===== Diretório =====");
        System.out.println("\n Profundidade Global: " + this.diretorio.getPG());
        System.out.println(" Tamanho: " + this.diretorio.getTamanho());
        System.out.print("\n [  ");
        for(int i = 0; i < this.diretorio.getTamanho(); i++){
            if(i < this.diretorio.getTamanho()-1){
                System.out.print(this.diretorio.getDiretorio()[i] + "  |  ");
            }else{
                System.out.print(this.diretorio.getDiretorio()[i]);
            }

        }
        System.out.println("  ]");


        System.out.println("\n\n ===== Buckets =====");
        System.out.println(" Quantidade de elementos por Bucket: " + this.quantidade);

        //Criando Bucket
        RandomAccessFile fileB = new RandomAccessFile(this.caminhoBuckets, "r");
        Bucket bucket = new Bucket(this.quantidade, this.diretorio.getPG());

        //Cabeçalho do arquivo de bucket
        qtdBuckets = fileB.readInt();
        System.out.println(" Quantidade de Buckets: " + qtdBuckets);

        /* lendo buckets do arquivo */
        for(int i = 0; i < qtdBuckets; i++){
            System.out.println("\n Bucket #" + i);
            endBucket = fileB.getFilePointer();
            System.out.println(" Endereço: " + endBucket);
            b = new byte[12+(12*this.quantidade)];
            fileB.read(b);
            bucket.fromByteArray(b);
            System.out.println(" Profundidade Local: " + bucket.getPL());
            System.out.println(" Quantidade de Posições Usadas: " + bucket.getContador());

            System.out.print(" ( ");
            for(int j = 0; j < this.quantidade; j++){
                System.out.print("[ chave: " + bucket.getCpfs()[j] + " | endereço: " + bucket.getEnderecos()[j] + " ] ");
            }
            System.out.println(")");
        }

    }

}

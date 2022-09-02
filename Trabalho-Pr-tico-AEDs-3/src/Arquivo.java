import java.io.*;

public class Arquivo {
    private String caminho;         //Caminho do Arquivo Mestre
    private int m;                  //Quantidade de Caracteres do campo de Anotações
    private int tamReg;             //Tamanho máximo de cada Registro
    private int pos;                //Posição do primeiro registro após o cabeçalho
    private int qtdExc;             //Quantidade de Registros Excluídos
    private int qtdReg;             //Quantidade de Registros Existentes
    private HashExtensivel indice;  //Índice

    //Getters and Setters
    public String getCaminho() {
        return caminho;
    }
    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }
    public int getM() {
        return m;
    }
    public void setM(int m) {
        this.m = m;
    }
    public HashExtensivel getIndice() {
        return indice;
    }
    public void setIndice(HashExtensivel indice) {
        this.indice = indice;
    }

    /**
     * Construtor que inicializa os atributos com os
     * valores existentes no arquivo
     * @param caminho Caminho do Arquivo
     * @throws IOException
     */
    Arquivo(String caminho, HashExtensivel indice) throws IOException {
        this.indice = indice;
        this.caminho = caminho + "arquivo_mestre.db";
        RandomAccessFile file = new RandomAccessFile(this.caminho, "rw");
        file.seek(0);

        try{
            this.m = file.readInt();;
            this.qtdExc = file.readInt();
            this.qtdReg = file.readInt();
        }catch (EOFException e){
            //Se não houver a quantidade no arquivo, inicializa o atributo "m" com 0
            this.m = 0;
            file.writeInt(this.m);
            file.seek(0);
        }
        this.tamReg = 72 + m;
        this.pos = 12;

        file.close();
    }

    /**
     * Cria novo arquivo e escreve o cabeçalho
     * @throws IOException
     */
    public void criarArquivo(int m, HashExtensivel indice) throws IOException {
        this.indice = indice;

        FileOutputStream fos = new FileOutputStream(this.caminho);
        DataOutputStream dos = new DataOutputStream(fos);

        //Escreve a quantidade de caracteres das anotações
        dos.writeInt(m);

        //Escreve a quantidade de registro excluídos
        dos.writeInt(0);
        this.qtdExc = 0;

        //Escreve a quantidade de registros existentes
        dos.writeInt(0);
        this.qtdReg = 0;

        fos.close();
        dos.close();
    }

    /**
     * (READ)
     * Faz a busca por um registro no Arquivo a
     * partir do CPF informado
     * @param cpf Chave do Registro a ser buscado
     * @return Objeto de Prontuário
     * @throws IOException
     */
    public Prontuario lerRegistro(int cpf) throws IOException {

        Prontuario prontuario = new Prontuario();
        byte b[];
        long endereco;

       endereco = this.indice.buscar(cpf);

        RandomAccessFile file = new RandomAccessFile(this.caminho, "r");

            if (endereco != -1) {
                file.seek(endereco+1); //endereço retornado + 1 byte da lápide
                b = new byte[this.tamReg-1];
                file.read(b);
                prontuario.fromByteArray(b);
                return prontuario;
            }

            file.close();
        return null;
    }

    /**
     * (CREATE)
     * Insere um novo Registro no arquivo
     * a partir de dados informados pelo usuário.
     * Reaproveita espaços de registros Excluídos.
     * @throws IOException
     */
    public boolean inserirRegistro(String nome, String nasc, int cpf, boolean sexo) throws IOException {

        long endereco;

        boolean encontrou = false, inseriu = false;
        RandomAccessFile file = new RandomAccessFile(this.caminho, "rw");

        long pLapide, ponteiroAtual;
        Prontuario p = new Prontuario(cpf, nome, nasc, sexo);

        //realiza a escrita ao final do arquivo
        if(this.qtdExc == 0){

            endereco = 12 + (this.qtdReg * this.tamReg);
            file.seek(endereco);
            file.write(p.toByteArray());

            //atualização do cabeçalho
            this.qtdReg++;
            file.seek(8);
            file.writeInt(this.qtdReg);

            this.indice.inserir(cpf, endereco);
            inseriu = true;

        }else{
            //Faz a busca por espaço disponível
            file.seek(this.pos);
            ponteiroAtual = file.getFilePointer();

            //Procurando lápide
            while(!encontrou){
                if(file.readBoolean() == true){

                    //atualiza a lápide para false
                    encontrou = true;
                    pLapide = file.getFilePointer()-2;
                    file.seek(pLapide);
                    file.writeBoolean(false);
                    endereco = file.getFilePointer();
                    file.write(p.toByteArray());

                    //atualizando valores do cabeçalho
                    this.qtdExc--;
                    file.seek(4);
                    file.writeInt(this.qtdExc);
                    this.qtdReg++;
                    file.seek(8);
                    file.writeInt(this.qtdReg);

                    this.indice.inserir(cpf, endereco);
                    inseriu = true;
                }else{
                    //Vai para o próximo registro
                    ponteiroAtual+=(72 + this.m);
                    file.seek(ponteiroAtual);
                }
            }
        }

        file.close();
        return inseriu;
    }

    /**
     * (UPDATE)
     * Edita o campo de anotações do médico de acordo
     * com o conteúdo informado por parâmetro
     * @param cpf CPF do paciente que será editado
     * @param p Objeto da classe prontuario que será escrito no arquivo
     * @throws IOException
     */
    public boolean editarRegistro(int cpf, Prontuario p) throws IOException {
        boolean editou = false;
        RandomAccessFile file = new RandomAccessFile(this.caminho, "rw");
        long endereco;
        endereco = this.indice.buscar(cpf);

            if (endereco != -1) {
                file.seek(endereco);
                file.write(p.toByteArray());
                editou = true;
            }

            file.close();
            return editou;
    }

    /**
     * (DELETE)
     * Faz uma Remoção Lógica do Registro a partir do CPF do paciente.
     * @throws IOException
     */
    public boolean removerRegistro(int cpf) throws IOException {

        RandomAccessFile file = new RandomAccessFile(this.caminho, "rw");
        boolean removeu = false;

        long endereco;
        endereco = this.indice.buscar(cpf);
            if(endereco != -1){
                //Zera o cpf
                file.seek(endereco+1);
                file.writeInt(0);

                //Atualiza a Lápide
                long ponteiro = file.getFilePointer()-5;
                file.seek(ponteiro);
                file.writeBoolean(true);

                //Atualiza o Cabeçalho
                this.qtdExc++;
                file.seek(4);
                file.writeInt(this.qtdExc);
                this.qtdReg--;
                file.seek(8);
                file.writeInt(this.qtdReg);

                this.indice.delete(cpf);
                removeu = true;
            }

            file.close();
        return removeu;
    }

    /**
     * Imprime todos os registros do Arquivo
     * para o usuário
     * @throws IOException
     */
    public void imprimirArquivo() throws IOException {

        this.indice.imprimirIndice();
        int aux = 0; //Contador
        boolean lapide = false;
        byte b[];
        long ponteiro;

        RandomAccessFile file = new RandomAccessFile(this.caminho, "r");
        Prontuario p = new Prontuario();
        file.seek(this.pos);
        ponteiro = file.getFilePointer();
        System.out.println("\n ===== Arquivo Mestre =====");
        System.out.println("\nTotal de Registros: " + this.qtdReg);

        //Percorre todos os Registros
        while (aux != (this.qtdExc + this.qtdReg)){
            lapide = file.readBoolean();
            if(!lapide){
                b = new byte[this.tamReg-1];
                file.read(b);
                p.fromByteArray(b);
                System.out.println(p);

                ponteiro += this.tamReg;
                file.seek(ponteiro);
            }else{
                //Avança para o próximo registro
                ponteiro += this.tamReg;
                file.seek(ponteiro);
            }
            aux++;
        }
    file.close();
    }
}

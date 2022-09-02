import java.io.*;

public class Bucket {

    private int PL;            //Profundidade Local
    private int quantidade;    //Quantidade de dados suportado pelo bucket
    private int[] cpfs;        //Vetor com as chaves
    private long[] enderecos;  //Vetor com os endereços das chaves no arquivo mestre
    private int contador;      //Quantas posições usadas

    //Getters and Setters
    public int getPL() {
        return PL;
    }
    public int[] getCpfs() {
        return cpfs;
    }
    public long[] getEnderecos() {
        return enderecos;
    }
    public int getContador() {
        return contador;
    }

    /**
     * Construtor
     * @param quantidade Quantidade de Registros do Bucket
     */
    Bucket(int quantidade, int PL){
        this.quantidade = quantidade;
        this.PL = PL;
        this.cpfs = new int[this.quantidade];
        this.enderecos = new long[this.quantidade];
        this.contador = 0;
        for(int i = 0; i < quantidade; i++){
            this.cpfs[i] = -1;
            this.enderecos[i] = -1;
        }
    }

    /**
     * Busca o CPF no bucket e retorna endereço
     * do registro no Arquivo Mestre
     * @param chave CPF a ser pesquisado
     * @return endereço que aponta para o arquivo mestre
     */
    public long read(int chave){
        long endereco = -1;

        for(int i = 0; i < this.quantidade; i++){
            if(this.cpfs[i] == chave){
                endereco = this.enderecos[i];
                break;
            }
        }
        return endereco;
    }

    /**
     * Deleta um registro do bucket
     * @param chave CPF a ser Excluido
     * @return
     */
    public boolean delete(int chave){

        int i;

        for(i = 0; i < this.quantidade; i++){
            if(this.cpfs[i] == chave){
                if(i < this.quantidade-1){
                    //Reajusta os elementos uma posição para trás
                    while(i < this.quantidade-1){
                        this.cpfs[i] = this.cpfs[i+1];
                        this.enderecos[i] = this.enderecos[i+1];
                        i++;
                    }
                }
                //limpa a última posição
                this.cpfs[this.quantidade-1] = -1;
                this.enderecos[this.quantidade-1] = -1;
                this.contador--;
                return true;
            }
        }

        return false;
    }

    /**
     * Atualiza o novo endereço do registro
     * @param chave CPF a ser buscado
     * @param endereco Novo endereço no arquivo mestre
     */
    public void update(int chave, long endereco){
        for(int i = 0; i < this.quantidade; i++){
            if(this.cpfs[i] == chave){
                this.enderecos[i] = endereco;
                break;
            }
        }
    }

    /**
     * Inserir Registro no Bucket
     * @param chave CPF a ser inserido
     * @param endereco Endereço do registro no Arquivo Mestre
     */
    public void inserir(int chave, long endereco){

        this.cpfs[this.contador] = chave;
        this.enderecos[this.contador] = endereco;

        this.contador++;
    }

    /**
     * Se Bucket estiver cheio
     * @return
     */
    public boolean full(){
        if(this.contador == this.quantidade) return true;
        else return false;
    }

    /**
     * Se Bucket estiver vazio
     * @return
     */
    public boolean empty(){
        //verifica ultima posição
        if(this.cpfs[0] == -1) return true;
        else return false;
    }

    /**
     * Escreve no Arquivo o vetor de Bytes com
     * os valores do bucket
     * @return Vetor de Bytes
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.PL);
        dos.writeInt(this.quantidade);
        dos.writeInt(this.contador);

        //escrever vetores
        for(int i = 0; i < this.quantidade; i++) {
            dos.writeInt(this.cpfs[i]);
        }
        for(int i = 0; i < this.quantidade; i++) {
            dos.writeLong(this.enderecos[i]);
        }

        return baos.toByteArray();
    }

    /**
     * Lê um vetor de Bytes e atribui os valores
     * aos atributos do Bucket
     * @param ba
     * @throws IOException
     */
    public void fromByteArray(byte[] ba) throws IOException{

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.PL = dis.readInt();
        this.quantidade = dis.readInt();
        this.contador = dis.readInt();
        for(int i = 0; i < this.quantidade; i++) {
            this.cpfs[i] = dis.readInt();
        }
        for(int i = 0; i < this.quantidade; i++) {
            this.enderecos[i] = dis.readLong();
        }

    }
}

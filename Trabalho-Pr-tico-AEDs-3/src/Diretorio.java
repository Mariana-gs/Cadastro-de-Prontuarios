import java.io.*;

public class Diretorio {

    private int PG;            //Profundidade Global
    private int tamanho;       //Tamanho do Diretório
    private long[] diretorio;  //Vetor com o Conteúdo do Diretório

    // Getters and Setters
    public int getPG() {
        return PG;
    }
    /**
     * Seta Valor de Profundidade Global e Tamanho do Diretorio
     * @param PG
     */
    public void setPG(int PG) {
        this.PG = PG;
        this.tamanho = (int) Math.pow(2,PG);
    }
    public int getTamanho() {
        return tamanho;
    }
    public void setTamanho(int tamanho) { this.tamanho = tamanho; }
    public long[] getDiretorio() {
        return diretorio;
    }
    public void setDiretorio(long[] diretorio) {
        this.diretorio = diretorio;
    }

    /**
     * Construtor
     * @param PG Profundidade Global
     */
    public Diretorio(int PG){
        this.PG = PG;
        this.tamanho = (int) Math.pow(2,PG);
        this.diretorio = new long[this.tamanho];
    }

    /**
     * Retorna qual a posição do vetor do diretório
     * será usada
     * @param chave CPF do paciente
     * @return
     */
    public int hash(int chave){
        return (int)(chave%this.tamanho);
    }

    /**
     * Retorna o endereço do Bucket onde o registro
     * pode estar
     * @param chave CPF a ser buscado
     * @return Endereço do Cesto
     */
    public long buscar(int chave){
        return this.diretorio[hash(chave)];
    }

    /**
     * Incrementa a profundidade Global e
     * Duplica o diretório
     */
    public void duplicar(){
        this.PG++;
        this.tamanho = (int) Math.pow(2,PG);
        long dir[] = new long[this.tamanho]; //Novo diretorio

        int j = 0; //Percorre o vetor antigo

        for(int i = 0; i < this.tamanho; i++){
            dir[i] = this.diretorio[j];
            j++;
            if(j == this.diretorio.length){
                j = 0;
            }
        }
        setDiretorio(dir);
    }

    /**
     * Atualiza endereço do bucket no diretorio
     * @param endereco Endereço Antigo
     * @param endNovo  Novo Endereço
     */
    public void atualizarEndereco(long endereco, int endNovo){
        boolean altera = false;
        for(int i = 0; i < this.tamanho; i++){
            if(this.diretorio[i] == endereco){
                if(altera){
                    this.diretorio[i] = endNovo;
                    altera = false;
                }else{
                    altera = true;
                }
            }
        }
    }

    /**
     * Escreve no Arquivo o vetor de Bytes com
     * os valores do diretorio
     * @return Vetor de Bytes
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.PG);
        dos.writeInt(this.tamanho);
        for(int i = 0; i < this.tamanho; i++){
            dos.writeLong(this.diretorio[i]);
        }
        return baos.toByteArray();
    }

    /**
     * Lê um vetor de Bytes e atribui os valores
     * aos atributos do diretorio
     * @param ba
     * @throws IOException
     */
    public void fromByteArray(byte[] ba) throws IOException{
        int pg, tam;

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.PG = dis.readInt();
        this.tamanho = dis.readInt();
        this.diretorio = new long[this.tamanho];
        for(int i = 0; i < this.tamanho; i++){
            this.diretorio[i] = dis.readLong();
        }
    }

}

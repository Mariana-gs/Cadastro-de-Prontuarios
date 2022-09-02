import java.io.*;

public class Prontuario {

    private int cpf;              //CPF do paciente
    private String nome;          //Nome do paciente
    private String nasc;          //Data de Nascimento do paciente
    private boolean sexo;         // 0 - Masculino, 1 - Feminino
    private String notas = "";    //Anotações do Médico
    private int m;                //Quantidade de Caracteres do campo de Anotações

    /**
     * Construtor que inicializa atributos
     * com valores inválidos (temporários)
     */
    Prontuario() {
        this.cpf = -1;     //4
        this.nome = "";    //50 + 2
        this.nasc = "";    // 10 +2
        this.sexo = false; //1
        this.notas = "";
        this.m = 0;
    }

    /**
     * Construtor Inicializa os Atributos com os valores
     * informados por parâmetro
     * @param cpf CPF do Paciente
     * @param nome Nome do Paciente
     * @param nasc Data de Nascimento do Paciente
     * @param sexo Sexo do Paciente
     */
    Prontuario(int cpf, String nome, String nasc, boolean sexo){
        this.cpf = cpf;

        if(nome.length() > 50){
            this.nome = nome.substring(0, 49);
        }else{
            this.nome = nome;
        }

        if(nasc.length() > 10){
            this.nasc = nasc.substring(0, 9);
        }else{
            this.nasc = nasc;
        }

        this.sexo = sexo;

    }

    //Getters and Setters
    public int getM() {
        return m;
    }
    public void setM(int m) {
        this.m = m;
    }
    public int getCpf() {
        return cpf;
    }
    public void setCpf(int cpf) {
        this.cpf = cpf;
    }
    public String getNome() {
        return nome;
    }
    /**
     * Faz a atualização e o tratamento do tamanho do
     * campo do nome do paciente
     * @param nome Nome do Paciente
     */
    public void setNome(String nome) {
        if(nome.length() > 50){
            this.nome = nome.substring(0, 49);
        }else{
            this.nome = nome;
        }
    }
    public String getNasc() {
        return nasc;
    }
    public void setNasc(String nasc) {
        if(nasc.length() > 10){
            this.nasc = nasc.substring(0, 9);
        }else{
            this.nasc = nasc;
        }
    }
    public boolean isSexo() {
        return sexo;
    }
    public void setSexo(boolean sexo) {
        this.sexo = sexo;
    }
    public String getNotas() {
        return notas;
    }
    /**
     * Faz a atualização e o tratamento do tamanho do campo de anotações
     * do médico
     * @param notas Anotações do Médico
     */
    public void setNotas(String notas) {

        if(notas.length() > m){
            this.notas = notas.substring(0, m);
        }else{
            this.notas = notas;
            for(int i = 0; i < (m-notas.length()); i++){
                notas+=" ";
            }
        }

    }

    /**
     * Escreve no Arquivo o vetor de Bytes com
     * os valores do registro
     * @return Vetor de Bytes
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        //Campo da Lápide
        dos.writeBoolean(false);

        dos.writeInt(this.cpf);
        dos.writeUTF(this.nome);
        dos.writeUTF(this.nasc);
        dos.writeBoolean(this.sexo);
        dos.writeUTF(this.notas);

        return baos.toByteArray();
    }

    /**
     * Lê um vetor de Bytes e atribui os valores
     * aos atributos do objeto de Prontuário
     * @param ba
     * @throws IOException
     */
    public void fromByteArray(byte[] ba) throws IOException{

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.cpf = dis.readInt();
        this.nome = dis.readUTF();
        this.nasc = dis.readUTF();
        this.sexo = dis.readBoolean();
        this.notas = dis.readUTF();
        this.m = notas.length();
    }

    public String toString(){
        return "\nCPF: " + cpf +
                "\nNome: " + nome +
                "\nData de Nascimento: " + nasc +
                "\nSexo: " + (sexo ? "Feminino" : "Masculino") +
                "\nAnotações: " + notas;
    }
}

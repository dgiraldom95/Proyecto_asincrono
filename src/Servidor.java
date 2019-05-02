
public class Servidor extends Thread {
    private int id;
    private Buffer buffer;

    public Servidor(int id, Buffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        while(!buffer.termino()){
            buffer.retirarMensaje();
        }
    }
}

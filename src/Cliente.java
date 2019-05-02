import java.util.Random;

public class Cliente extends Thread{
    private int id;
    private int numMensajes;
    private Buffer buffer;

    public Cliente(int id, int numMensajes, Buffer buffer){
        this.id = id;
        this.numMensajes = numMensajes;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        for (int i = 0; i < numMensajes; i++) {
            Mensaje m = new Mensaje(new Random().nextInt(10000));
            int valorAnterior = m.getContenido();
            buffer.depositarMensaje(m);
            int valorFinal = m.getContenido();
            System.out.printf("cliente %d: %d -> %d - Mensaje %d/%d\n", id, valorAnterior, valorFinal, i+1, numMensajes);
        }
        buffer.notificarTerminacion(id);
    }
}

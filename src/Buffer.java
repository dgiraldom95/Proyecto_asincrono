import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Buffer {
    private int capacidad;
    private int numMensajes;
    private Integer numClientes;
    private final Object monitorClientes;
    private LinkedList<Mensaje> mensajes;

    private HashMap<Integer, Boolean> clienteEsperando;


    public Buffer(int capacidad, int numClientes){
        this.capacidad = capacidad;
        this.numMensajes = 0;
        mensajes = new LinkedList<>();
        this.numClientes = numClientes;
        monitorClientes = new Object();
    }

//    Metodo usado por los clientes para depositar su mensaje
    public void depositarMensaje(Mensaje m){
        boolean flag = false;
        while(!flag){
            synchronized (this){
//                Si hay espacio en el buffer agrega en el buffer y activa el flag para salir del while
                if(numMensajes != capacidad){
                    numMensajes++;
                    System.out.println("Depositando mensaje - Tamaño buffer: "+numMensajes+"/"+capacidad);
                    mensajes.add(m);
                    flag = true;
                }
            }
//            Si todavia no hay espacio (flag = false) cede su turno
            if(!flag) {
                Thread.yield();
            }
        }

//        Despues de mandar el mensaje el thread espera sobre el mensaje que mando
        synchronized (m){
            try {
                m.wait();
            } catch (InterruptedException e){}
        }
    }

//    Metodo usado por los servidores para retirar un mensaje
    public void retirarMensaje(){
//        Para evitar "Local variable may not have been initialized"
        Mensaje m = new Mensaje(-1);

        boolean flag = false;
        while (!flag){
//            Revisa si todavia quedan clientes
            if(termino()){
                return;
            }
            synchronized (this){
//                Si el buffer no esta vacia retira un mensaje y sale de el while
                if(numMensajes != 0){
                    numMensajes--;
                    System.out.println("Retirando mensaje - Tamaño buffer: "+numMensajes+"/"+capacidad);
                    m = mensajes.pollLast();
                    flag = true;
                }
            }
//            Cede el turno para no notificar sobre el mensaje antes que el cliente este esperando
            Thread.yield();
        }
//      Una vez retira el mensaje incrementa el valor del mensaje y notifica sobre este (a el cliente que lo deposito)
        synchronized (m) {
            int valor = m.getContenido();
            m.setContenido(++valor);
            m.notify();
        }
    }

//    Metodo usado por el cliente para notificar que ya mando todos sus mensajes
    public void notificarTerminacion(int idCliente){
        synchronized (monitorClientes){
            numClientes--;
            System.out.println("Cliente "+idCliente+" termina - "+numClientes+" restantes");
        }
    }

//    Retorna true si ya no hay mas clientes esperando
    public boolean termino(){
        synchronized (monitorClientes){
            return numClientes == 0;
        }
    }

    public static void main(String[] args){
        int capacidadBuffer = 50;
        int numClientes = 50;
        int numServidores = 20;

        Buffer buffer = new Buffer(capacidadBuffer, numClientes);
        Cliente[] clientes = new Cliente[numClientes];
        Servidor[] servidores = new Servidor[numServidores];
        for (int i = 0; i < clientes.length; i++) {
            clientes[i] = new Cliente(i, new Random().nextInt(50), buffer);
            clientes[i].start();
        }
        for (int i = 0; i < servidores.length; i++) {
            servidores[i] = new Servidor(i, buffer);
            servidores[i].start();
        }

        try {
            for(Cliente c : clientes){
                c.join();
            }
            for(Servidor s : servidores){
                s.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

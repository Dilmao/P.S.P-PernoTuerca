import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class PernoTuerca {
    // VERSION CON 2 SEMAFOROS (QUITAR a_SemaforoReceptor Y TOKENS_RECEPTOR PARA VOLVER AL ORIGINAL (RENOMBRAR TOKENS_EMISOR Y a_SemaforoEmisor TAMBIEN))
    public static final int TOKENS_EMISOR = 0;
    public static final int TOKENS_RECEPTOR = 2;
    public static final int NUM_HILOS = 3;
    public static final int CADENCIA_A = 100;
    public static final int CADENCIA_B = 200;

    public static final String C_VERDE = "\u001B[32m";
    public static final String C_AZUL = "\u001B[34m";
    public static final String C_TURQUESA = "\u001B[36m";

    static class LineaA implements Runnable
    {
        private Buzon a_Buzon;
        public LineaA(Buzon p_Buzon) {this.a_Buzon = p_Buzon;}

        public void run() {
            // Declaración variables
            int l_Contador = 0;
            String l_Perno = "";

            while (true) {
                try {
                    // Cadencia de creación de pernos: 100 ms
                    Thread.sleep(CADENCIA_A);
                    a_Buzon.a_SemaforoReceptor.acquire(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Actualizar variables
                l_Contador++;
                l_Perno = "P" + l_Contador;
                a_Buzon.a_ContadorP = l_Contador;

                // Imprimir el perno con el número de serie
                System.out.print(C_VERDE);
                System.out.println(l_Perno);
                a_Buzon.a_Perno = l_Perno;

                a_Buzon.a_SemaforoEmisor.release(1);
            }
        }
    }   // LineaA()

    static class LineaB implements Runnable
    {
        private Buzon a_Buzon;
        public LineaB(Buzon p_Buzon) {this.a_Buzon = p_Buzon;}

        public void run() {
            // Declaración variables
            int l_Contador = 0;
            String l_Tuerca = "";

            while (true) {
                try {
                    // Cadencia de creación de tuercas: 200 ms
                    a_Buzon.a_SemaforoReceptor.acquire(1);
                    Thread.sleep(CADENCIA_B);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Actualizar variables
                l_Contador++;
                l_Tuerca = "T" + l_Contador;
                a_Buzon.a_ContadorT = l_Contador;

                // Imprimir la tuerca con el número de serie
                System.out.print(C_AZUL);
                System.out.println(l_Tuerca);
                a_Buzon.a_Tuerca = l_Tuerca;

                a_Buzon.a_SemaforoEmisor.release(1);
            }
        }
    }   // LineaB()

    static class Caja implements Runnable
    {
        private Buzon a_Buzon;

        public Caja(Buzon p_Buzon) {
            this.a_Buzon = p_Buzon;
        }

        public void run() {
            String l_Mezcla = "";
            while (true) {
                if (a_Buzon.a_ContadorP == a_Buzon.a_ContadorT) {
                    try {
                        a_Buzon.a_SemaforoEmisor.acquire(2);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    // Asegurarse de que hay al menos un perno y una tuerca disponibles
                    l_Mezcla = a_Buzon.a_Perno + a_Buzon.a_Tuerca;

                    System.out.print(C_TURQUESA);
                    System.out.println(l_Mezcla);

                    a_Buzon.a_SemaforoReceptor.release(2);
                }
            }
        }
    } // Caja()

    static class Buzon
    {
        private String a_Perno = "";
        private String a_Tuerca = "";
        private int a_ContadorP = 0;
        private int a_ContadorT = 0;
        private Semaphore a_SemaforoEmisor = new Semaphore(TOKENS_EMISOR);
        private Semaphore a_SemaforoReceptor = new Semaphore(TOKENS_RECEPTOR);
    }   // Buzon()

    public static void main(String[] args) {
        ExecutorService l_Executor = (ExecutorService) Executors.newFixedThreadPool(NUM_HILOS);
        Buzon l_Buzon = new Buzon();

        LineaA l_Tarea1 = new LineaA(l_Buzon);
        LineaB l_Tarea2 = new LineaB(l_Buzon);
        Caja l_Tarea3 = new Caja(l_Buzon);

        l_Executor.submit(l_Tarea1);
        l_Executor.submit(l_Tarea2);
        l_Executor.submit(l_Tarea3);
    }   // main()
}
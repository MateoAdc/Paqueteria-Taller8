import java.util.ArrayList;
import java.util.List;

public class Lista {
    private List<Paqueteria> serviEntrega;

    public Lista() {
        serviEntrega = new ArrayList<>();
    }

    public List<Paqueteria> getServiEntrega() {
        return serviEntrega;
    }

    public void adicionarElementos(Paqueteria paquete) throws Exception {
        if (paquete == null) {
            throw new Exception("El paquete no puede ser nulo.");
        }
        if (paquete.getTracking() <= 0) {
            throw new Exception("El número de tracking debe ser mayor que cero.");
        }
        if (paquete.getPeso() <= 0) {
            throw new Exception("El peso debe ser mayor que cero.");
        }
        if (paquete.getCedulaReceptor() == null || paquete.getCedulaReceptor().trim().isEmpty()) {
            throw new Exception("Debe ingresar la cédula del receptor.");
        }
        for (Paqueteria p : serviEntrega) {
            if (p.getTracking() == paquete.getTracking()) {
                throw new Exception("Ya existe un paquete con el tracking " + paquete.getTracking() + ".");
            }
        }
        serviEntrega.add(paquete);
    }

    public String listarPaquetes() {
        if (serviEntrega.isEmpty()) {
            return "No existen paquetes registrados.";
        }
        StringBuilder sb = new StringBuilder();
        for (Paqueteria p : serviEntrega) {
            sb.append(p).append("\n");
        }
        return sb.toString();
    }

    public Paqueteria buscarPaquete(int tracking) throws Exception {
        for (Paqueteria p : serviEntrega) {
            if (p.getTracking() == tracking) {
                return p;
            }
        }
        throw new Exception("No existe un paquete con el tracking " + tracking + ".");
    }

    public void editar(int tracking, double peso, String ciudadRecepcion,
                       String ciudadEntrega, String cedulaReceptor) throws Exception {
        if (peso <= 0) {
            throw new Exception("El peso debe ser mayor que cero.");
        }
        if (cedulaReceptor == null || cedulaReceptor.trim().isEmpty()) {
            throw new Exception("Debe ingresar la cédula del receptor.");
        }
        Paqueteria p = buscarPaquete(tracking);
        p.setPeso(peso);
        p.setCiudadRecepcion(ciudadRecepcion);
        p.setCiudadEntrega(ciudadEntrega);
        p.setCedulaReceptor(cedulaReceptor);
    }

    public String estadoPaquete(int tracking) {
        Paqueteria encontrado = null;
        for (Paqueteria p : serviEntrega) {
            if (p.getTracking() == tracking) {
                encontrado = p;
                break;
            }
        }
        if (encontrado == null) {
            throw new IllegalArgumentException("No existe un paquete con el tracking " + tracking + ".");
        }

        switch (encontrado.getEstado()) {
            case "Receptado":
                encontrado.setEstado("Enviado");
                return "Enviado";
            case "Enviado":
                encontrado.setEstado("Recibido");
                return "Recibido";
            default:
                return "Finalizado";
        }
    }

    public int sumarTotalPaquetes() {
        return contarPaquetes(0);
    }

    private int contarPaquetes(int indice) {
        if (indice == serviEntrega.size()) {
            return 0;
        }

        return 1 + contarPaquetes(indice + 1);
    }

    public double sumarTotalPeso() {

        return sumarPesos(0);
    }

    private double sumarPesos(int indice) {
        if (indice == serviEntrega.size()) {
            return 0;
        }

        return serviEntrega.get(indice).getPeso() + sumarPesos(indice + 1);
    }

    public double sumarTotalPesoCiudad(String ciudad) {
        return sumarPesoCiudad(ciudad, 0);
    }

    private double sumarPesoCiudad(String ciudad, int indice) {
        if (indice == serviEntrega.size()) {
            return 0;
        }
        Paqueteria p = serviEntrega.get(indice);
        double pesoActual = p.getCiudadRecepcion().equalsIgnoreCase(ciudad) ? p.getPeso() : 0;

        return pesoActual + sumarPesoCiudad(ciudad, indice + 1);
    }

    public int sumarTotalPaquetesEstado(String estado) {
        return contarPorEstado(estado, 0);
    }

    private int contarPorEstado(String estado, int indice) {
        if (indice == serviEntrega.size()) {
            return 0;
        }
        int cuenta = serviEntrega.get(indice).getEstado().equalsIgnoreCase(estado) ? 1 : 0;

        return cuenta + contarPorEstado(estado, indice + 1);
    }

    public List<Paqueteria> ordenarPaquetesPorTracking() {
        List<Paqueteria> copia = new ArrayList<>(serviEntrega);
        int n = copia.size();
        for (int i = 0; i < n - 1; i++) {
            boolean huboIntercambio = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (copia.get(j).getTracking() > copia.get(j + 1).getTracking()) {
                    Paqueteria aux = copia.get(j);
                    copia.set(j, copia.get(j + 1));
                    copia.set(j + 1, aux);
                    huboIntercambio = true;
                }
            }
            if (!huboIntercambio) {
                break;
            }
        }
        return copia;
    }

    public List<Paqueteria> ordenarPaquetesPorPeso() {
        List<Paqueteria> copia = new ArrayList<>(serviEntrega);
        for (int i = 1; i < copia.size(); i++) {
            Paqueteria actual = copia.get(i);
            int j = i - 1;
            while (j >= 0 && copia.get(j).getPeso() > actual.getPeso()) {
                copia.set(j + 1, copia.get(j));
                j--;
            }
            copia.set(j + 1, actual);
        }
        return copia;
    }

    public int busquedaBinaria(List<Paqueteria> listaOrdenada, int tracking) {
        int inicio = 0;
        int fin = listaOrdenada.size() - 1;
        while (inicio <= fin) {
            int medio = (inicio + fin) / 2;
            int valorMedio = listaOrdenada.get(medio).getTracking();
            if (valorMedio == tracking) {
                return medio;
            } else if (valorMedio < tracking) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }
        return -1;
    }

    public List<Paqueteria> buscarPaquetesPorCedulaYEstado(String cedula, String estado) {
        List<Paqueteria> resultado = new ArrayList<>();
        for (Paqueteria p : serviEntrega) {
            if (p.getCedulaReceptor().equals(cedula) && p.getEstado().equalsIgnoreCase(estado)) {
                resultado.add(p);
            }
        }
        return resultado;
    }
}

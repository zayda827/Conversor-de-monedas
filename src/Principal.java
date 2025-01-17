import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Principal {
    public static void main(String[] args) {
        boolean item = true;
        Scanner lectura = new Scanner(System.in); // Solo una instancia de Scanner

        while (item) {
            // Menú
            String menu = """
                *****************************************
                Bienvenido al conversor de moneda
                Escoja una de las siguientes opciones:
                1) Dólar -> Peso argentino
                2) Peso argentino -> Dólar
                3) Dólar -> Real brasileño
                4) Real brasileño -> Dólar 
                5) Dólar -> Peso colombiano
                6) Peso colombiano -> Dólar
                7) Salir
                """;
            System.out.println(menu);

            // Leer opción
            int opcion = obtenerOpcion(lectura);

            if (opcion == 7) {
                System.out.println("Programa finalizado");
                item = false;
                continue;
            }

            // Solicitar valor a convertir
            double valor = obtenerValorAConvertir(lectura);

            // Realizar la conversión según la opción
            switch (opcion) {
                case 1 -> realizarConversion(valor, "USD", "ARS");
                case 2 -> realizarConversion(valor, "ARS", "USD");
                case 3 -> realizarConversion(valor, "USD", "BRL");
                case 4 -> realizarConversion(valor, "BRL", "USD");
                case 5 -> realizarConversion(valor, "USD", "COP");
                case 6 -> realizarConversion(valor, "COP", "USD");
                default -> System.out.println("Seleccione una opción válida.");
            }
        }

        lectura.close(); // Cerramos el Scanner
    }

    // Opciones no validas del menú
    private static int obtenerOpcion(Scanner lectura) {
        int opcion = -1;
        while (opcion < 1 || opcion > 7) {
            try {
                System.out.print("Seleccione una opción: ");
                opcion = Integer.parseInt(lectura.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Por favor ingrese un número válido.");
            }
        }
        return opcion;
    }

    // Valor a convertir ingresado por el usuario
    private static double obtenerValorAConvertir(Scanner lectura) {
        double valor = 0;
        boolean validado = false;
        while (!validado) {
            try {
                System.out.print("Ingrese el valor que desea convertir: ");
                valor = Double.parseDouble(lectura.nextLine());
                validado = true;
            } catch (NumberFormatException e) {
                System.out.println("Por favor ingrese un valor numérico válido.");
            }
        }
        return valor;
    }

    // Conversion de monedas
    private static void realizarConversion(double cantidad, String monedaOrigen, String monedaDestino) {
        double resultado = convertirMoneda(cantidad, monedaOrigen, monedaDestino);
        if (resultado != 0) {
            System.out.printf("El valor de %.2f %s es igual a %.2f %s.%n", cantidad, monedaOrigen, resultado, monedaDestino);
        } else {
            System.out.println("Error al realizar la conversión.");
        }
    }

    // Usando la API
    private static double convertirMoneda(double cantidad, String monedaOrigen, String monedaDestino) {
        try {
            // API URL para obtener tasas de conversión en USD
            String urlStr = "https://v6.exchangerate-api.com/v6/9ed103276bf2fede1b26e62c/latest/" + monedaOrigen;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
            connection.setRequestMethod("GET");

            // Leer la respuesta de la API
            Scanner sc = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (sc.hasNext()) {
                response.append(sc.nextLine());
            }
            sc.close();

            // Convertir la respuesta JSON usando Gson
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            if (!jsonResponse.get("result").getAsString().equals("success")) {
                throw new RuntimeException("Error al obtener la tasa de conversión");
            }

            // Obtener la tasa de conversión entre las dos monedas
            JsonObject conversionRates = jsonResponse.getAsJsonObject("conversion_rates");
            if (!conversionRates.has(monedaDestino)) {
                throw new RuntimeException("Moneda de destino no disponible.");
            }

            double tasa = conversionRates.get(monedaDestino).getAsDouble();
            return cantidad * tasa;

        } catch (Exception e) {
            System.out.println("Error al realizar la conversión: " + e.getMessage());
            return 0;
        }
    }
}

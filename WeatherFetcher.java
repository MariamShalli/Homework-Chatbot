import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class WeatherFetcher {

    private static final String WEATHER_API_KEY = "85c6c7aec1dce2c62862503fba94e6cc";
    private static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String EXCHANGE_RATE_API_KEY = "4a5c464d7bd44f9aba8bfd48";
    private static final String EXCHANGE_RATE_BASE_URL = "https://v6.exchangerate-api.com/v6/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Get weather information
        System.out.print("Enter the name of a city: ");
        String cityName = scanner.nextLine();

        // Get exchange rate information
        System.out.print("Enter the base currency code (e.g., USD): ");
        String baseCurrency = scanner.nextLine();
        System.out.print("Enter the target currency code (e.g., EUR): ");
        String targetCurrency = scanner.nextLine();

        scanner.close();

        try {
            String weatherData = fetchWeather(cityName);
            System.out.println("Weather Information: \n" + weatherData);

            String exchangeRateData = fetchExchangeRate(baseCurrency, targetCurrency);
            System.out.println("Exchange Rate Information: \n" + exchangeRateData);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String fetchWeather(String cityName) throws Exception {
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
        String apiUrl = WEATHER_BASE_URL + "?q=" + encodedCity + "&appid=" + WEATHER_API_KEY + "&units=metric";

        URI uri = new URI(apiUrl);
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseData.append(line);
            }
            reader.close();

            String jsonResponse = responseData.toString();
            String temperature = extractValue(jsonResponse, "\"temp\":", ",");
            String feelsLike = extractValue(jsonResponse, "\"feels_like\":", ",");
            String minTemp = extractValue(jsonResponse, "\"temp_min\":", ",");
            String maxTemp = extractValue(jsonResponse, "\"temp_max\":", ",");

            return String.format("Current Temperature: %.2f°C\nFeels Like: %.2f°C\nMin Temperature: %.2f°C\nMax Temperature: %.2f°C",
                    Double.parseDouble(temperature), Double.parseDouble(feelsLike), Double.parseDouble(minTemp), Double.parseDouble(maxTemp));
        } else {
            throw new Exception("Failed to retrieve weather data. HTTP response code: " + responseCode);
        }
    }

    private static String fetchExchangeRate(String baseCurrency, String targetCurrency) throws Exception {
        String apiUrl = EXCHANGE_RATE_BASE_URL + EXCHANGE_RATE_API_KEY + "/pair/" + baseCurrency + "/" + targetCurrency;

        URI uri = new URI(apiUrl);
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseData.append(line);
            }
            reader.close();

            String jsonResponse = responseData.toString();
            String rate = extractValue(jsonResponse, "\"conversion_rate\":", "}");

            return String.format("Exchange Rate (%s to %s): %.4f", baseCurrency, targetCurrency, Double.parseDouble(rate));
        } else {
            throw new Exception("Failed to retrieve exchange rate data. HTTP response code: " + responseCode);
        }
    }

    private static String extractValue(String json, String key, String delimiter) {
        int startIndex = json.indexOf(key) + key.length();
        int endIndex = json.indexOf(delimiter, startIndex);
        return json.substring(startIndex, endIndex).trim();
    }
}

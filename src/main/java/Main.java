import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {

    static String getUrlFromJson(String json) {
        String url = "";
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONObject jsonObject = (JSONObject) obj;
            url = jsonObject.get("url").toString();
        } catch (org.json.simple.parser.ParseException exp) {
            exp.printStackTrace();
        }
        return url;
    }

    public static void main(String[] args) throws IOException {
        // ключ
        String apiKey = "wqoXroFCgDbCpbdKQPSSu1EfZMCdcPd8OEpjbl15";
        // httpClient
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();
        // стартовая строка запроса
        HttpGet request = new HttpGet("https://api.nasa.gov/planetary/apod?api_key="+apiKey);
        // заголовки запроса
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        // ответ от сервера
        CloseableHttpResponse response = httpClient.execute(request);
        // json ответа
        String json = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        // url изображения
        String url = getUrlFromJson(json);
        // новый запрос по полученному url
        request = new HttpGet(url);
        response = httpClient.execute(request);
        // имя файла
        String filePath = url.substring(url.lastIndexOf('/')+1);
        // потоки ввода вывода
        try (BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
                FileOutputStream fos = new FileOutputStream(filePath)
        ) {
            // буфер
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            // чтение-запись
            while ((bytesRead = bis.read(dataBuffer, 0, 1024)) != -1) {
                fos.write(dataBuffer, 0, bytesRead);
            }
        }
        // закрытие клиента
        httpClient.close();
    }
}

package ru.netology;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TicketFetcher {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите email: ");
        String email = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        try {
            String cookie = loginAndGetCookie(email, password);
            if (cookie != null) {
                String response = fetchTickets(cookie);
                System.out.println("Список вопросов: ");
                System.out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static String loginAndGetCookie(String email, String password) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String loginUrl = "https://netology.ru/backend/admin/sign_in";

        HttpPost post = new HttpPost(loginUrl);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        post.setHeader("Referer", "https://netology.ru/backend/admin/sign_in");

        StringEntity entity = new StringEntity(
                "admin[email]=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
                        "&admin[password]=" + URLEncoder.encode(password, StandardCharsets.UTF_8) +
                        "&commit=Войти"
        );
        post.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                System.out.println("Ошибка авторизации. Код ответа: " + statusCode);
                String errorResponse = EntityUtils.toString(response.getEntity());
                System.out.println("Ответ сервера: " + errorResponse);
                return null;
            }

            String setCookie = null;
            if (response.getFirstHeader("Set-Cookie") != null) {
                setCookie = response.getFirstHeader("Set-Cookie").getValue();
            } else {
                System.out.println("Не удалось получить куки из ответа.");
                return null;
            }

            return setCookie;
        }
    }

    private static String fetchTickets(String cookie) throws IOException {
        String ticketsUrl = "https://netology.ru/frontendadmin/help_desk/tickets/opened";

        HttpGet ticketsGet = new HttpGet(ticketsUrl);
        ticketsGet.setHeader("Cookie", cookie);
        ticketsGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        try (CloseableHttpResponse ticketsResponse = HttpClients.createDefault().execute(ticketsGet)) {
            return EntityUtils.toString(ticketsResponse.getEntity());
        }
    }
}

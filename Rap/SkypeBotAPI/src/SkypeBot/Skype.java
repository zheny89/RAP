package SkypeBot;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Skype {
    static private Skype instance;
    private SkypeServer skypeServer;
    private TokenThread tokenThread;
    private String appID, appPass, token;
    private String baseUrl, skypeName;
    private boolean isConnected;
    private boolean isNeedAuth;
    private boolean isDebug;
    private boolean isLoading;
    private String channelID = "skype";
    private ArrayList<User> userList;

    private Skype() {
        skypeServer = SkypeServer.getInstance();
        tokenThread = new TokenThread();
        skypeName = "SkypeBot";
        baseUrl = "https://smba.trafficmanager.net/apis";
        isConnected = false;
        isNeedAuth = true;
        isDebug = false;
        userList = new ArrayList<>();
        isLoading = true;
        loadContacts();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setDebug(boolean isDebug){
        this.isDebug = isDebug;
    }

    public boolean getIsDebug(){
        return isDebug;
    }

    /**
     * Определяет нужна ли индентификация на серверах Microsoft
     *
     * @param needAuth true - да, false - нет
     */
    public void setNeedAuth(boolean needAuth) {
        this.isNeedAuth = needAuth;
        if(isDebug)
            System.out.println("Аунтификация по токену: "+needAuth);
    }

    public boolean isNeedAuth() {
        return isNeedAuth;
    }

    @Deprecated
    public void setSkypeName(String skypeName) {
        this.skypeName = skypeName;
        if(isDebug)
            System.out.println("Имя изменено");
    }

    /**
     * Устанавливает порт для приема сообщений от сервера
     *
     * @param port порт
     */
    public void setSkypeServerPort(int port){
        skypeServer.setPort(port);
    }

    @Deprecated
    public String getSkypeName() {
        return skypeName;
    }

    /**
     * Устанавливает базовый адрес для отправки сообщений
     * @param baseUrl
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        if(isDebug)
            System.out.println("Базовый адрес изменен");
    }

    /**
     * @return Список добавленный пользователей
     */
    public ArrayList<User> getUserList() {
        return userList;
    }

    /**
     * @param name ассоциативное время
     * @param userID id пользователя (не логин)
     */
    public void addUser(String name, String userID) {
        User user = new User(name, userID);
        if (userList.contains(user)) return;
        userList.add(new User(name, userID));
        if (!isLoading)
            saveContacts();
    }

    /**
     * Удаляет пользователя из базы
     * @param userID id пользователя(не логин)
     */
    public void removeUser(String userID) {
        userList.remove(new User(null, userID));
        saveContacts();
    }

    /**
     * @see Skype#setBaseUrl(String)
     * @return базовый адрес
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param name имя пользователя в базе
     * @return {@link User}
     */
    public User getUserByName(String name) {
        for (User user : userList) {
            if (user.getName().equals(name))
                return user;
        }
        return null;
    }

    /**
     * @param id id пользователя в базе
     * @return {@link User}
     */
    public User getUserByID(String id) {
        for (User user : userList) {
            if (user.getId().equals(id))
                return user;
        }
        return null;
    }

    /**
     * Устанавливает слушателя на событие
     * @param action событие, смотреть в документации botframework
     * @param listener {@link ActionListener}
     */
    public void setSkypeListener(String action, ActionListener listener) {
        skypeServer.setSkypeListener(action, listener);
        if(isDebug)
            System.out.println("Добавление нового листенера: "+action);
    }

    /**
     * Подключается к серверу и начинает работу
     * @param appID id бота( из botframework)
     * @param appPass пароль (из botframework)
     * @throws IOException если нет соединения
     */
    public void connect(String appID, String appPass) throws IOException {
        if (isConnected) return;
        this.appID = appID;
        this.appPass = appPass;
        skypeServer.start();
        if (isNeedAuth)
            tokenThread.start();
        isConnected = true;
        while (isLoading) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (Exception e) {
            }
        }
        if(isDebug)
            System.out.println("Скайп-бот подключен. Сервер запущен. Порт: "+skypeServer.getPort());
    }

    /**
     * Отключает сервер и сохраняет контакты. Нормальный выход.
     */
    public void disconnect() {
        if (!isConnected) return;
        skypeServer.stop();
        tokenThread.interrupt();
        isConnected = false;
        if(isDebug)
            System.out.println("Скайп-бот отключен");
        saveContacts();
    }

    /**
     * @return {@link Skype}
     */
    static public Skype getInstance() {
        if (instance == null)
            instance = new Skype();
        return instance;
    }

    //actions Methods
    @Deprecated
    public JSONReader getMembers(String conversationID) throws IOException {
        if (!isConnected) throw new RuntimeException("SkypeBot.Skype is not connected!");
        if(isDebug)
            System.out.println("Получение списка конфы");

        HttpURLConnection connection =
                (HttpURLConnection) new URL(baseUrl + "/v3/conversations/" + conversationID + "/members").openConnection();
        if (isNeedAuth)
            connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.setDoInput(true);
        connection.connect();

        System.out.println(connection.getResponseCode() + " " + connection.getResponseMessage());
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        while (reader.ready())
            builder.append(reader.readLine()).append("\n");
        connection.disconnect();
        JSONReader jsonReader = new JSONReader();
        jsonReader.read(builder.toString());
        if (isDebug)
            System.out.println(builder.toString());
        return jsonReader;
    }

    /**
     * Ответ на пришедшее сообщение
     * @param message текст
     * @param jwkReader {@link JSONReader}
     * @throws IOException если нет соединения
     */
    public void replayToMessage(String message, JSONReader jwkReader) throws IOException {
        if (!isConnected) throw new RuntimeException("SkypeBot.Skype is not connected!");
        if(isDebug)
            System.out.println("Отправка ответа на сообщение");

        String baseUrl = jwkReader.getField("serviceUrl");
        String conversationID = jwkReader.getField("conversation.id");
        String activityID = jwkReader.getField("id");
        String jsonMessage =
                "{\n" +
                        "    \"type\": \"message\",\n" +
                        "    \"from\": {\n" +
                        "        \"id\": \"" + jwkReader.getField("recipient.id") + "\",\n" +
                        "        \"name\": \"" + jwkReader.getField("recipient.name") + "\"\n" +
                        "    },\n" +
                        "    \"conversation\": {\n" +
                        "        \"id\": \"" + jwkReader.getField("conversation.id") + "\"\n" +
                        "    },\n" +
                        "   \"recipient\": {\n" +
                        "        \"id\": \"" + jwkReader.getField("from.id") + "\",\n" +
                        "        \"name\": \"" + jwkReader.getField("from.name") + "\"\n" +
                        "    },\n" +
                        "    \"text\": \"" + message + "\",\n" +
                        "    \"replyToId\": \"" + jwkReader.getField("id") + "\"\n" +
                        "}";

        HttpURLConnection connection =
                (HttpURLConnection) new URL(baseUrl + "/v3/conversations/" + conversationID + "/activities/" + activityID).openConnection();

        connection.setRequestMethod("POST");
        if (isNeedAuth)
            connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();

        OutputStream out = connection.getOutputStream();
        out.write(jsonMessage.getBytes("UTF-8"));
        out.flush();

        connection.disconnect();

        int rcode = connection.getResponseCode();
        if(isDebug)
            if (rcode != 500 && rcode != 400 && rcode != 401 && rcode != 403 && rcode != 404 && rcode != 503)
                System.out.println("Ответ отправлен");
            else
                System.err.println("Ответ не отправлен. " + connection.getResponseCode() + " : " + connection.getResponseMessage());
    }

    /**
     * Отправляет собщение
     * @param message текст
     * @param conversationID id конференции
     * @throws IOException если нет соединения
     */
    public void sendMessage(String conversationID, String message) throws IOException {
        if (!isConnected) throw new RuntimeException("SkypeBot.Skype is not connected!");
        if(isDebug)
            System.out.println("Отправка сообщения");
        String jsonMessage =
                "{\n" +
                        "\"type\": \"message\",\n" +
                        "\"from\": {\n" +
                        "\"id\": \"" + appID + "\",\n" +
                        "\"skypeName\": \"" + skypeName + "\"\n" +
                        "},\n" +
                        "\"conversation\": {\n" +
                        "\"id\": \"" + conversationID + "\"\n" +
                        "},\n" +
                        "\"text\": \"" + message + "\"\n" +
                        "}";
        HttpURLConnection connection =
                (HttpURLConnection) new URL(baseUrl + "/v3/conversations/" + conversationID + "/activities").openConnection();
        connection.setRequestMethod("POST");
        if(isNeedAuth)
            connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.connect();

        OutputStream out = connection.getOutputStream();
        out.write(jsonMessage.getBytes("UTF-8"));
        out.flush();

        connection.disconnect();
        int rcode = connection.getResponseCode();
        if(isDebug)
            if (rcode != 500 && rcode != 400 && rcode != 401 && rcode != 403 && rcode != 404 && rcode != 503)
                System.out.println("Отправлено");
            else
                System.err.println("Не отправлено." + connection.getResponseCode() + " : " + connection.getResponseMessage());
    }

    /**
     * @param name имя пользователя в базе
     * @return id пользователя
     */
    public String getUserId(String name) {
        for (User user : userList) {
            if (user.getName().equals(name))
                return user.getId();
        }
        return null;
    }

    @Deprecated
    private String getUserMass(String[] users) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < users.length; i++) {
            builder.append("{\n").append("\"id\": \"").append(users[i]).append("\"\n").append("}");
            if (i != users.length - 1)
                builder.append(",\n");
            else builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Начинает нову конферению, необзажима для начала отправки сообщений
     * @param topicName название конференции ( не используется)
     * @param userIDs id приглашеных пользователей
     * @return id конференции
     * @throws IOException
     */
    public String startConversation(String topicName, String... userIDs) throws IOException {
        if (!isConnected) throw new RuntimeException("SkypeBot.Skype is not connected!");
        if(isDebug)
            System.out.println("Бот начинает новую беседу");
        String isGroup = "false";
        if (userIDs.length > 1)
            isGroup = "true";
        String jsonMessage =
                "{\n" +
                        "    \"bot\": {\n" +
                        "        \"id\": \""+appID+"\",\n" +
                        "        \"name\": \""+skypeName+"\"\n" +
                        "    },\n" +
                        "    \"isGroup\": " + isGroup + ",\n" +
                        "    \"members\": [\n" +
                        getUserMass(userIDs) +
                        "    ],\n" +
                        "    \"topicName\": \""+topicName+"\"\n" +
                        "}";
        HttpURLConnection connection =
                (HttpURLConnection) new URL(baseUrl+"/v3/conversations").openConnection();
        connection.setRequestMethod("POST");
        if(isNeedAuth)
            connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();

        OutputStream out = connection.getOutputStream();
        out.write(jsonMessage.getBytes());
        out.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        while (reader.ready())
            response.append(reader.readLine()).append("\n");
        connection.disconnect();

        JSONReader jsonReader = new JSONReader();
        jsonReader.read(response.toString());
        String convId = jsonReader.getField("id");
        if(isDebug)
            System.out.println("Новая беседа создана. ID: " + convId);
        return convId;
    }

    /**
     * Сохраняет контакты в файл
     */
    public void saveContacts() {
        try {
            File file = new File("skypecontact");
            if (file.exists()) file.delete();
            file.createNewFile();
            FileWriter out = new FileWriter(file);
            out.write(String.valueOf(userList.size()));
            out.write("\r\n");
            for (User user : userList) {
                out.write(user.getName());
                out.write("::");
                out.write(user.getId());
                out.write("\r\n");
                out.flush();
            }
            out.close();
            System.out.println("Сохранено: " + userList.size());
        } catch (Exception e) {
            System.err.println("Не смог сохранить контакты");
        }
    }

    /**
     * Загружает контакты из файла
     */
    private void loadContacts() {
        try {
            File file = new File("skypecontact");
            if (!file.exists()) return;
            BufferedReader in = new BufferedReader(new FileReader(file));
            int count = Integer.valueOf(in.readLine());
            for (int i = 0; i < count; i++) {
                String str = in.readLine();
                String[] mass = str.split("::");
                String name = mass[0];
                String userID = mass[1];
                addUser(name, userID);
            }
            in.close();
            System.out.println("Загружено: " + userList.size());
        } catch (Exception e) {
            System.err.println("Не смог загрузить контакты");
        }
    }

    /**
     * Поток, получающий токен каждые 50 минут
     */
    private class TokenThread extends Thread {
        private int expiresTime;
        private JSONReader jwkReader;
        final String GET_TOKEN_URL = "https://login.microsoftonline.com/botframework.com/oauth2/v2.0/token";

        TokenThread() {
            setDaemon(true);
            jwkReader = new JSONReader();
            expiresTime = 0;
        }

        @Override
        public void run() {
            final String TOKEN_REQUEST =
                    "grant_type=client_credentials&" +
                            "client_id=" + appID + "&" +
                            "client_secret=" + appPass + "&" +
                            "scope=https%3A%2F%2Fapi.botframework.com%2F.default";

            while (skypeServer.isRunning()) {
                if (expiresTime <= 0) {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(GET_TOKEN_URL).openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.connect();

                        OutputStream out = connection.getOutputStream();
                        out.write(TOKEN_REQUEST.getBytes());
                        out.flush();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                        String response = reader.readLine();
                        connection.disconnect();

                        jwkReader.read(response);
                        token = jwkReader.getField("access_token");
                        //  expiresTime = Integer.valueOf(jwkReader.getField("expires_in"));
                        expiresTime = 3000;
                        if(isDebug)
                            System.out.println("Новый токен получен");
                    } catch (IOException e) {
                        throw new RuntimeException("SkypeBot.Skype: не смог получить токен! " + e.toString());
                    } finally {
                        isLoading = false;
                    }
                } else {
                    try {
                        sleep(3200 * 1000);
                        expiresTime--;
                    } catch (InterruptedException e) {
                        System.err.println("Поток токенов прекратил работу");
                    }
                }
            }
        }
    }
}

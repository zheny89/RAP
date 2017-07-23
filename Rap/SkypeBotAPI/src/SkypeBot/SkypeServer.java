package SkypeBot;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.TreeMap;

/**
 * Created by Женя on 30.06.2017.
 */
class SkypeServer {
    static private SkypeServer instance;
    private HttpServer server;
    private int port;
    private boolean isRunning;
    private TreeMap<String, ActionListener> skypeListeners;

    static public SkypeServer getInstance(){
        if(instance == null)
            instance = new SkypeServer();
        return instance;
    }

    private SkypeServer(){
        isRunning  = false;
        skypeListeners = new TreeMap<>();
        skypeListeners.put("message", (jwkReader) -> {
            System.out.println("Принято сообщение: " + jwkReader.getField("text"));
        });
    }

    public void start() throws IOException{
        if(isRunning) return;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new ServerHandler());
        server.start();
        this.port = server.getAddress().getPort();
        isRunning = true;
    }

    public void setSkypeListener(String action, ActionListener listener){
        skypeListeners.put(action, listener);
    }

    public void stop(){
        if(!isRunning) return;
        server.stop(0);
        isRunning = false;
    }

    public void setPort(int port){
        if(isRunning) return;
        this.port = port;
    }

    public int getPort(){
        return port;
    }

    public boolean isRunning(){
        return isRunning;
    }

    private void recieveAction(JSONReader jwkReader){
        ActionListener actionListener = skypeListeners.get(jwkReader.getField("type"));
        if(actionListener != null)
            actionListener.action(jwkReader);
    }

    private class ServerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), "UTF-8"));
            while (reader.ready())
                builder.append(reader.readLine()).append("\n");
            JSONReader jsonReader = new JSONReader();
            jsonReader.read(builder.toString());
            httpExchange.sendResponseHeaders(200,-1);
            if (jsonReader.getField("type").equals("contactRelationUpdate")) {
                if (jsonReader.getField("action").equals("add"))
                    Skype.getInstance().addUser(jsonReader.getField("from.name"), jsonReader.getField("from.id"));
                else if (jsonReader.getField("action").equals("remove"))
                    Skype.getInstance().removeUser(jsonReader.getField("from.id"));
            }
            recieveAction(jsonReader);
        }
    }

}

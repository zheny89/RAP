package SkypeBot;

import java.io.IOException;

/**
 * Пример
 */
public class main {
    final static String botName = "MySkypeBot";
    final static String appID = "743a6fa8-996f-46b5-b0d8-b32b062a8a4d";
    final static String appPass = "TZ3obOposwmdCwFZa4ZvGrO";

    public static void main(String args[]) throws Exception{

        //Получаем объект скайпа
        Skype skype = Skype.getInstance();
        //Вклчаем дополнительные сообщения
        skype.setDebug(true);
        //Устанавливаем базовый адрес
        skype.setBaseUrl("https://smba.trafficmanager.net/apis/");
        //Устанавливаем порт
        skype.setSkypeServerPort(2342);
        //Устанавливаем слушателя на событие: прием сообщения
        skype.setSkypeListener("message",(jsonReader -> {
            System.out.println("Сообщение: "+jsonReader.getField("text"));
            try {
                //Отправляем ответное сообщение
                skype.replayToMessage(jsonReader.getField("text"), jsonReader);
                //Выключаем скайп
                skype.disconnect();
            }catch (IOException e){

                System.err.println(e.toString());
            }
        }));
        skype.connect(appID, appPass);
        try {
            //   String conv = skype.startConversation(null, skype.getUserId("Киселев"));
            //     skype.sendMessage(conv, "Hello");
        } catch (Exception e) {
        }
    }
}

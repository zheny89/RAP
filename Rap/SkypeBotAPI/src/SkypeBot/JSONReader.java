package SkypeBot;

import java.util.HashMap;

/**
 * Считывает json строку, разбивает ее на ключ - значение и кладет в HashMap.
 */
public class JSONReader {
    private HashMap<String, String> map;

    public String getField(String key){
        return map.get(key);
    }

    public HashMap<String, String> getMap(){
        return map;
    }

    public void read(String jwkString){
        map = new HashMap<>();
        StringBuilder string = new StringBuilder(jwkString);
        StringBuilder currentKey = new StringBuilder();
        String value;
        boolean inMassive = false;
        boolean isEnum = false;
        int massCount = 0;
        if(string.charAt(0) == '{')
            string.deleteCharAt(0);

        while (string.length() > 0) {
            StringBuilder keyBuilder = new StringBuilder();
            //readKey
            int start = string.indexOf("\"");
            if(start == -1) break;
            int end = string.indexOf("\"", start + 1);
            int endDot = string.indexOf("}");
            int endMassive = string.indexOf("]");
            if(endDot != -1 && endDot < start) {
                    currentKey = new StringBuilder();
                    if(inMassive)
                        currentKey.append("[").append(massCount++).append("]").append(".");
            }
            if(endMassive != -1 && endMassive < start){
                inMassive = false;
                isEnum = false;
                massCount = 0;
                currentKey = new StringBuilder();
            }

            int nextMass = string.indexOf("[");
            if (nextMass != -1 && nextMass < start) {
                inMassive = true;
                currentKey.append("[").append(massCount++).append("]");
                int nextDot  = string.indexOf("{");
                if(nextDot != -1 && nextDot < nextMass + 3)
                    currentKey.append(".");
                else isEnum = true;
            }

            keyBuilder.append(string.substring(start + 1, end));
            string.delete(0, end + 1);

            if(!isEnum) {
                //readValue
                start = string.indexOf("\"");
                end = string.indexOf("\"", start + 1);
                int begin = string.indexOf(":");
                int endBegin = string.indexOf(",", begin + 1);
                if (endBegin != -1 && (start > endBegin || start == -1)) {
                    start = begin;
                    end = endBegin;
                    if (end == -1) {
                        int space = string.indexOf("\n");
                        int blockEnd = string.indexOf("}");
                        if (space != -1 && space < blockEnd)
                            end = space;
                        else end = blockEnd;
                    }

                }
                //massive
                nextMass = string.indexOf("[");
                if (nextMass != -1 && nextMass < start) {
                    inMassive = true;
                }
                //dopDot
                int nextDot = string.indexOf("{");
                if (nextDot != -1 && nextDot < start) {
                    if (inMassive) {
                        currentKey.append(keyBuilder.toString())
                                .append("[").append(massCount++).append("]").append(".");
                    } else
                        currentKey.append(keyBuilder.toString()).append('.');
                    continue;
                } else if (inMassive) {
                    currentKey.append(keyBuilder.toString()).append("[").append(massCount++).append("]");
                    isEnum = true;
                    string.delete(0, nextMass+1);
                    continue;
                }
                value = string.substring(start + 1, end);
                string.delete(0, end + 1);

                map.put(currentKey.toString() + keyBuilder.toString(), value);
            }else {
                map.put(currentKey.toString(),keyBuilder.toString());
            }

        }

    }

}

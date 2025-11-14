package fish.crafting.logviewer.util;

import org.jetbrains.annotations.Nullable;

//funny
public class Logs {

    public static void logMessage(String message){
        //todo
    }

    public static void logError(Exception e){
        logError(null, e);
    }

    public static void logError(@Nullable String text, Exception e){
        e.printStackTrace(); //todo
    }

}

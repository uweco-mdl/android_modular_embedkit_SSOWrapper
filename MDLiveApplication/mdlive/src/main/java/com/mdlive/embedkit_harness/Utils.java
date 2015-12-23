package com.mdlive.embedkit_harness;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils
{
    private Utils(){}

    public enum DATE_NOTATION {
        MILLI, DOT
    }

    /**
     * Generates current timestamp
     *
     * @return  current time in selected notation. E.g. dot notation "2015.09.04.14.01.33"
     */
    public static String GetCurrentTimeStamp(DATE_NOTATION notation)
    {
        String timeStamp = "";
        Date dt = new Date();

        switch(notation) {

            case DOT:
                timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(dt);
                break;
            case MILLI:
                timeStamp = String.valueOf(dt.getTime());
                break;
            default:
        }

        return(timeStamp);
    }
}

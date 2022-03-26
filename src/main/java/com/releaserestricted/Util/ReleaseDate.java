package com.releaserestricted.Util;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReleaseDate {

    private int day;
    private int year;
    private String month;
    private String date;

    public ReleaseDate(String monthPart, String yearPart){
            this.date = monthPart + " " + yearPart;
            this.year = Integer.parseInt(yearPart);
            String[] splitStr = monthPart.trim().split("\\s+");
            this.day = Integer.parseInt(splitStr[0]);
            this.month = splitStr[1];
    }

}

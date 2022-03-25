package com.releaserestricted.Util;
import lombok.Getter;

@Getter
public enum YearOptions {

    _2001("2001"),
    _2002("2002"),
    _2003("2003"),
    _2004("2004"),
    _2005("2005"),
    _2006("2006");

    private String name;

    YearOptions(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}

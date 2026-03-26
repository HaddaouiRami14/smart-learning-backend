package com.example.SmartLearning.Enum;

public enum Category {
    Programming,
    Design,
    Business,
    Marketing,
    DataScience,
    Language,
    Music,
    Photography,
    Other;

   
    public String getLabel() {
        return switch (this) {
            case Programming  -> "Programming";
            case Design       -> "Design";
            case Business     -> "Business";
            case Marketing    -> "Marketing";
            case DataScience  -> "Data Science";
            case Language     -> "Language";
            case Music        -> "Music";
            case Photography  -> "Photography";
            case Other        -> "Other";
        };
    }
}

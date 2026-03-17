package com.example.SmartLearning.Enum;
public enum ProgrammingLanguage {
    PYTHON(71, "Python", "py", "def solution():\n    # Write your code here\n    pass"),
    JAVASCRIPT(63, "JavaScript", "js", "function solution() {\n    // Write your code here\n}"),
    JAVA(62, "Java", "java", "public class Solution {\n    public static void main(String[] args) {\n        // Write your code here\n    }\n}");
    
    private final int judge0Id;
    private final String displayName;
    private final String extension;
    private final String defaultTemplate;
    
    ProgrammingLanguage(int judge0Id, String displayName, String extension, String defaultTemplate) {
        this.judge0Id = judge0Id;
        this.displayName = displayName;
        this.extension = extension;
        this.defaultTemplate = defaultTemplate;
    }
    
    public int getJudge0Id() {
        return judge0Id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getDefaultTemplate() {
        return defaultTemplate;
    }
}
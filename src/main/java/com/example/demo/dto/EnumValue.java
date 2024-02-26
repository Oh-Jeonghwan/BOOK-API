package com.example.demo.dto;

public class EnumValue {
	private String key;
    private String value;
    private String displayName;

    public EnumValue(EnumModel enumModel) {
        key = enumModel.getKey();
        value = enumModel.getValue();
        displayName = enumModel.getDisplayName();
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
    
    public String getDisplayName() {
    	return displayName;
    }
}

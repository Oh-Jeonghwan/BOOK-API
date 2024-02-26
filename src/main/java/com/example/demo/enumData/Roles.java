package com.example.demo.enumData;

import com.example.demo.dto.EnumModel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Roles implements EnumModel{
	
	ADMIN("ROLE_ADMIN", "관리자"),
    APPROVE("ROLE_APPROVE", "결재권자"),
    MEMBER("ROLE_MEMBER", "사용자");
	
	private String value;

    private String displayName;
    
    Roles(String value) { //생성자
        this.value = value;
    }
	
    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getValue() {
        return value;
    }
    
    @Override
    public String getDisplayName() {
    	return displayName;
    }

}

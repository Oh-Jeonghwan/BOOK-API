package com.example.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

import com.example.demo.enumData.Roles;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Account {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    //@ApiModelProperty(value = "로그인ID")
    @Column(unique = true)
    private String loginId;

    //@ApiModelProperty(value = "비밀번호")
    private String password;
    
    //@ApiModelProperty(value="권한ID")
	//  @OneToOne(cascade = {CascadeType.ALL})
	@Enumerated(EnumType.STRING)
	//  @JoinColumn(name = "role_id")
	private Roles role;


}

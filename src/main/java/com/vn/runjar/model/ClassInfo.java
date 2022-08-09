package com.vn.runjar.model;

import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;

@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
@Getter
@Setter
public class ClassInfo {
    private String className;
    private String methodName;
    private String tokenID;
    private String libName;
}

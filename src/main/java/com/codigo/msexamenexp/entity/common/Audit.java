package com.codigo.msexamenexp.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class Audit {
    @Column(name = "user_create",length = 45,nullable = true)
    private String userCreate;
    @Column(name = "date_create",nullable = true)
    private Timestamp dateCreate;
    @Column(name = "user_modif",length = 45,nullable = true)
    private String userModif;
    @Column(name = "date_modif",nullable = true)
    private Timestamp dateModif;
    @Column(name = "user_delete",length = 45,nullable = true)
    private String userDelete;
    @Column(name = "date_delete",nullable = true)
    private Timestamp dateDelete;
}

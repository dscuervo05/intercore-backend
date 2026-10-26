package com.app.pro.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;


@Entity
@Table(name = "configuracion_dian")
@Data
public class ConfiguracionDian {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idConfiguracion;

    @Column(name = "id_empresa_tenant", unique = true , nullable = false)
    private String idEmpresaTenant;

    private String nitEmpresa;

    @Column(name = "id_software")
    private String idSoftware;

    @Column(name = "pin_software")
    private String pinSoftware;

    @Column(name = "clave_tecnica")
    private String claveTecnica;

    @Column(name = "test_set_id")
    private String testSetId;

    @Column(name = "ruta_certificado")
    private String rutaCertificado;

    @Column(name = "clave_certificado")
    private String claveCertificado;

}

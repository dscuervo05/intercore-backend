package com.app.pro.service;

import com.app.pro.entity.ConfiguracionDian;
import com.app.pro.repository.ConfiguracionDianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfiguracionDianService {

    @Autowired
    private ConfiguracionDianRepository configuracionRepository;

    public ConfiguracionDian obtenerConfiguracion(String idEmpresa){
        return configuracionRepository.FindByIdEmpresaTenant(idEmpresa).
                orElseThrow(() -> new IllegalArgumentException("La empresa aún no tiene configurada su facturación electrónica."));
    }

    public ConfiguracionDian guardaOActualizarConfiguracion(ConfiguracionDian nuevaConfig, String idEmpresa){
        Optional<ConfiguracionDian> configExistente = configuracionRepository.FindByIdEmpresaTenant(idEmpresa);

        if (configExistente.isPresent()) {

            ConfiguracionDian actual = configExistente.get();
            actual.setNitEmpresa(nuevaConfig.getNitEmpresa());
            actual.setIdSoftware(nuevaConfig.getIdSoftware());
            actual.setPinSoftware(nuevaConfig.getPinSoftware());
            actual.setClaveTecnica(nuevaConfig.getClaveTecnica());
            actual.setTestSetId(nuevaConfig.getTestSetId());

            return configuracionRepository.save(actual);
        }else {
            nuevaConfig.setIdEmpresaTenant(idEmpresa);
            return configuracionRepository.save(nuevaConfig);
        }
    }
}

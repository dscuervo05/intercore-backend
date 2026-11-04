package com.app.pro.service;

import com.app.pro.entity.ComprobanteDiario;
import com.app.pro.entity.MovimientoContable;
import com.app.pro.repository.ComprobanteDiarioRepository;
import com.app.pro.repository.MovimientoContableRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ContabilidadService {

    @Autowired
    private ComprobanteDiarioRepository comprobanteRepository;

    @Autowired
    private MovimientoContableRepository movimientoRepository;

    @Transactional
    public ComprobanteDiario registrarAsiento(ComprobanteDiario comprobante , List<MovimientoContable> movimientos, String idEmpresa) {


        //Validar la regla de partida doble
        BigDecimal totalDebitos = BigDecimal.ZERO;
        BigDecimal totalCreditos = BigDecimal.ZERO;

        for (MovimientoContable mov : movimientos){
            totalDebitos = totalDebitos.add(mov.getDebito());
            totalCreditos = totalCreditos.add(mov.getCredito());
        }

        //Usamos compareTo() con BigDecimal. Retorna 0 si son exactamente iguales.
        if(totalDebitos.compareTo(totalCreditos)!=0){
            throw new IllegalArgumentException("Error contable: Las sumas de débitos (" + totalDebitos + ") y créditos (" + totalCreditos + ") no cuadran.");
        }
        //2. Inyección del Tenant de seguridad para la cabecera
        comprobante.setIdEmpresaTenant(idEmpresa);
        ComprobanteDiario comprobanteGuardado = comprobanteRepository.save(comprobante);

        // 3. Enlazar los movimientos al comprobante y asignarles el Tenant
        for (MovimientoContable mov : movimientos){
            mov.setComprobante(comprobanteGuardado);
            mov.setIdEmpresaTenant(idEmpresa);
            movimientoRepository.save(mov);
        }
        return comprobanteGuardado;
    }
}

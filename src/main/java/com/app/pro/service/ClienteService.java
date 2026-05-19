package com.app.pro.service;

import com.app.pro.entity.Cliente;
import com.app.pro.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    // 🎯 MODIFICADO: Ahora solo lista los clientes de la empresa que lo solicita
    public List<Cliente> listarTodos(String idEmpresa){
        return clienteRepository.findByIdEmpresaTenant(idEmpresa);
    }
    
    // 🎯 MODIFICADO: Recibe el cliente y a qué empresa pertenece
    public Cliente guardarCliente(Cliente nuevoCliente, String idEmpresa){
        
        Optional<Cliente> clienteExistente = clienteRepository.findByNumeroDocumento(nuevoCliente.getNumeroDocumento());
        if(clienteExistente.isPresent()){
            throw new IllegalArgumentException("Error: el documento " + nuevoCliente.getNumeroDocumento() + " ya está registrado en el sistema.");
        }

        // 🔒 INYECCIÓN DE SEGURIDAD SAAS
        // Le "tatuamos" la empresa al cliente antes de tocar la base de datos
        // Así evitamos que el frontend tenga que enviarlo (o que lo manipulen)
        nuevoCliente.setIdEmpresaTenant(idEmpresa);
        
        return clienteRepository.save(nuevoCliente);
    }
    
}
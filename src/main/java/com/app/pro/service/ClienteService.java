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
    
    public  List<Cliente> listarTodos(){
        return clienteRepository.findAll();
    }
    
    public Cliente guardarCliente(Cliente nuevoCliente){
        
        Optional<Cliente> clienteExistente = clienteRepository.findByNumeroDocumento(nuevoCliente.getNumeroDocumento());
        if(clienteExistente.isPresent()){
            throw new IllegalArgumentException("Error: el documento" + nuevoCliente.getNumeroDocumento() + " ya esta registradoen en el sistema.");
        }
        return clienteRepository.save(nuevoCliente);
    }
    
}

package com.app.pro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class DianSoapConfig {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // Aquí le diremos a Spring dónde estarán las clases generadas a partir del WSDL de la DIAN
        // Por ahora lo dejamos apuntando a un paquete que crearemos luego
        marshaller.setContextPath("com.app.pro.dian.wsdl");
        return marshaller;
    }

    @Bean
    public DianSoapClient dianSoapClient(Jaxb2Marshaller marshaller) {
        DianSoapClient client = new DianSoapClient();
        // URL del entorno de pruebas (Habilitación) de la DIAN
        client.setDefaultUri("https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }
}
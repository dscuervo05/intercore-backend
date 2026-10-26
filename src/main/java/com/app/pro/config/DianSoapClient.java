package com.app.pro.config;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class DianSoapClient extends WebServiceGatewaySupport {

    // Método para enviar el set de pruebas (El primer paso para que la DIAN te apruebe el software)
    public Object enviarFacturaPrueba(Object requestPayload) {

        // El SoapActionCallback indica qué operación específica de la DIAN queremos ejecutar
        String soapAction = "http://wcf.dian.colombia/IWcfDianCustomerServices/SendTestSetAsync";

        return getWebServiceTemplate().marshalSendAndReceive(
                "https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc",
                requestPayload,
                new SoapActionCallback(soapAction)
        );
    }
}
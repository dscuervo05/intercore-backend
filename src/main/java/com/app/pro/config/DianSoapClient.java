package com.app.pro.config;

import com.app.pro.dian.wsdl.GetStatus;
import com.app.pro.dian.wsdl.GetStatusResponse;
import com.app.pro.dian.wsdl.ObjectFactory;
import com.app.pro.dian.wsdl.SendTestSetAsync;
import com.app.pro.dian.wsdl.SendTestSetAsyncResponse;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import java.util.Base64;

public class DianSoapClient extends WebServiceGatewaySupport {

    /**
     * Envía una factura electrónica al ambiente de pruebas/habilitación de la DIAN
     */
    public SendTestSetAsyncResponse enviarSetDePruebas(String nombreArchivoZip, String zipBase64, String testSetId) {

        ObjectFactory factory = new ObjectFactory();
        SendTestSetAsync request = factory.createSendTestSetAsync();

        // Envolver campos usando ObjectFactory
        request.setFileName(factory.createSendTestSetAsyncFileName(nombreArchivoZip + ".zip"));

        // Decodificar Base64 a byte[] (el WSDL requiere base64Binary)
        byte[] bytesZip = Base64.getDecoder().decode(zipBase64);
        request.setContentFile(factory.createSendTestSetAsyncContentFile(bytesZip));

        request.setTestSetId(factory.createSendTestSetAsyncTestSetId(testSetId));

        String soapAction = "http://wcf.dian.colombia/IWcfDianCustomerServices/SendTestSetAsync";

        return (SendTestSetAsyncResponse) getWebServiceTemplate().marshalSendAndReceive(
                "https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc",
                request,
                new SoapActionCallback(soapAction)
        );
    }
    public GetStatusResponse consultaEstadoDocumento(String trackId){
        ObjectFactory factory = new ObjectFactory();
        GetStatus request = factory.createGetStatus();

        request.setTrackId(factory.createGetStatusTrackId(trackId));

        String soapAction = "http://wcf.dian.colombia/IWcfDianCustomerServices/GetStatus";

        return (GetStatusResponse) getWebServiceTemplate().marshalSendAndReceive(
                "https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc",
                request,
                new SoapActionCallback(soapAction)
        );
    }
}
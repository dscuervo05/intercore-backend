package com.app.pro.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FirmaDigitalService {

    /**
     * Carga el certificado digital (.p12/.pfx) y firma el XML UBL 2.1
     */
    public String firmarXml(String xmlSinFirmar, String rutaCertificado, String claveCertificado) throws Exception {

        // 1. Cargar el certificado digital en formato PKCS12
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(rutaCertificado)) {
            keyStore.load(fis, claveCertificado.toCharArray());
        }

        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, claveCertificado.toCharArray());
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

        // 2. Parsear la cadena XML a un objeto DOM Document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(
                new ByteArrayInputStream(xmlSinFirmar.getBytes(StandardCharsets.UTF_8))
        );

        // 3. Crear la fábrica de firma XML DOM
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // Transformaciones requeridas (Enveloped Signature)
        List<Transform> transforms = new ArrayList<>();
        transforms.add(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
        transforms.add(fac.newTransform(CanonicalizationMethod.INCLUSIVE, (TransformParameterSpec) null));

        Reference ref = fac.newReference(
                "",
                fac.newDigestMethod(DigestMethod.SHA256, null),
                transforms,
                null,
                null
        );

        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                Collections.singletonList(ref)
        );

        // Información pública del certificado X509
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        List<X509Certificate> x509Content = Collections.singletonList(cert);
        X509Data xd = kif.newX509Data(x509Content);
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

        // Determinar el nodo de inserción (Estructura de extensiones de la DIAN)
        NodeList extensiones = doc.getElementsByTagNameNS(
                "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2",
                "ExtensionContent"
        );

        Element targetNode = doc.getDocumentElement();
        if (extensiones.getLength() > 0) {
            targetNode = (Element) extensiones.item(0);
        }

        DOMSignContext dsc = new DOMSignContext(privateKey, targetNode);
        XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(dsc);

        // 4. Convertir el Document DOM firmado de vuelta a String
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        StringWriter writer = new StringWriter();
        trans.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }
}
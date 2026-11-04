package com.app.pro.util;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class DianZipUtil {

    /*
     * Recibe el XML de la factura, lo comprime en formato ZIP en memoria
     * y retorna el resultado convertido a una cadena Base64.
     */
    public String comprimirXmlABase64(String xmlContenido, String nombreArchivoXml) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)){
            ZipEntry entry = new ZipEntry(nombreArchivoXml + "xml");
            zos.putNextEntry(entry);
            zos.write(xmlContenido.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

}

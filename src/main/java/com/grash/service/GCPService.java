package com.grash.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobTargetOption;
import com.google.cloud.storage.Storage.PredefinedAcl;
import com.google.cloud.storage.StorageOptions;
import com.grash.exception.CustomException;
import com.grash.utils.Helper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class GCPService {
    Helper helper = new Helper();

    // get service by env var GOOGLE_APPLICATION_CREDENTIALS. Json file generated in API & Services -> Service account key
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream("Gcp.json");
    Credentials credentials = GoogleCredentials
            .fromStream(is);
    Storage storage = StorageOptions.newBuilder().setCredentials(credentials)
            .setProjectId("receipt-6becf").build().getService();

    public GCPService() throws IOException {
    }

    public String upload(MultipartFile file, String folder) {
        try {
            BlobInfo blobInfo = storage.create(
                    BlobInfo.newBuilder("sutura", folder + "/" + helper.generateString() + " " + file.getOriginalFilename()).build(), //get original file name
                    file.getBytes(), // the file
                    BlobTargetOption.predefinedAcl(PredefinedAcl.PUBLIC_READ) // Set file permission
            );
            return blobInfo.getMediaLink(); // Return file url
        } catch (IllegalStateException | IOException e) {
            throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}

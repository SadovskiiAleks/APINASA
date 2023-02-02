package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class Main {
    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();
        HttpGet request = new HttpGet("https://api.nasa.gov/planetary/apod?api_key=ZCuYVJhcodXoYrsqTObEmOPiluFeNJmfMoEHcaD4");
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        CloseableHttpResponse response = httpClient.execute(request);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        List<NasaClass> nasaList = mapper.readValue(
                response.getEntity()
                        .getContent(),
                new TypeReference<>() {});

//      В java-объекте найдите поле url и сделайте с ним еще один http-запрос с помощью уже созданного httpClient;
        request = new HttpGet(nasaList.get(0).getUrl());
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        response = httpClient.execute(request);

        //Чтение файла с сайта
        byte[] data = response.getEntity().getContent().readAllBytes();
        InputStream innn = new ByteArrayInputStream(data);
        BufferedImage image = ImageIO.read(innn);

        //Подготовка наименования файла
        String stringOfUrl = nasaList.get(0).getUrl();
        int indexOfSlash = stringOfUrl.lastIndexOf('/');
        String nameOfFile = stringOfUrl.substring(indexOfSlash + 1);

        // Запись файла
        // Вопрос к преподавателю !!! Как создать file без наменования пользователя, я пробывал %USERPROFILE%, не помогло.
        String test = System.getProperty("user.home");
        System.out.println(test + "/Desktop/" + nameOfFile);
        File file = new File(test + "/Desktop/" + nameOfFile);
        ImageIO.write(image, "jpg",file);


        response.close();
        httpClient.close();
    }
}
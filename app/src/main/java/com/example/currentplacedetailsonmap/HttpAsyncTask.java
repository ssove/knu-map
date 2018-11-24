package com.example.currentplacedetailsonmap;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpAsyncTask
        extends AsyncTask<Void, Void, ResultBody>
        implements JsonDeserializer {
    private String url = "http://ec2-18-222-114-158.us-east-2.compute.amazonaws.com:3001/";
    private String action;
    private String path;
    private Type typeToken;
    private MyCallBack callback;
    private JSONObject requestBodyJson;
    private File file;


    public HttpAsyncTask(String action, String path, JSONObject requestBodyJson,Type typeToken,
                         MyCallBack callback) {
        this.action = action;
        this.path = path;
        this.requestBodyJson = requestBodyJson;
        this.file = null;
        this.typeToken = typeToken;
        this.callback = callback;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    OkHttpClient client = new OkHttpClient();

    @Override
    protected ResultBody doInBackground(Void... params) {
        String strUrl = this.url + this.path;
        ResultBody resultBody = null;

        try {
            Request request = null;
            RequestBody requestBody = null;

            if(this.file != null){
                MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", file.getName(), RequestBody.create(MultipartBody.FORM, file));

                Iterator<String> keys = requestBodyJson.keys();
                while(keys.hasNext()){
                    String key = keys.next();
                    try{
                        String multiValueStr = "";
                        for(int i=0; i<requestBodyJson.getJSONArray(key).length(); i++){
                            multiValueStr += requestBodyJson.getJSONArray(key).get(i) + ",";
                        }
                        bodyBuilder.addFormDataPart(key, multiValueStr);
                    }catch (JSONException e){
                        bodyBuilder.addFormDataPart(key, requestBodyJson.getString(key));
                    }
                }

                requestBody = bodyBuilder.build();
            }else if(this.requestBodyJson != null){
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                requestBody = MultipartBody.create(JSON, requestBodyJson.toString());
            }



            if(this.action.equalsIgnoreCase("GET")){ //GET, 대소문자 상관X
                // 요청
                request = new Request.Builder()
                        .url(strUrl)
                        .build();
            }
            else if(this.action.equalsIgnoreCase("POST")){
                request = new Request.Builder()
                        .url(strUrl)
                        .post(requestBody)
                        .build();
            }

            else if(this.action.equalsIgnoreCase("PUT")){
                request = new Request.Builder()
                        .url(strUrl)
                        .put(requestBody)
                        .build();
            }
            else if(this.action.equalsIgnoreCase("DELETE")) {
                request = new Request.Builder()
                        .url(strUrl)
                        .delete()
                        .build();
            }

            // 응답 : header, body 정보 담겨있음
            Response response = client.newCall(request).execute();
            Gson gson = new Gson();
            String responseString = response.body().string();
            System.out.println(responseString.toString());
            resultBody = gson.fromJson(responseString, typeToken); //fromJson 사용하면 자동으로 변환
        } catch (IOException e ) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return new ResultBody<>(resultBody.getSuccess(), resultBody.getSize(), resultBody.getDatas(), resultBody.getError());
    }

    @Override
    protected void onPostExecute(ResultBody resultBody) {
        super.onPostExecute(resultBody);

        this.callback.doTask(resultBody);
    }

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
package com.smartdesk.meeting;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MeetingWindow extends Stage {

    public MeetingWindow(String roomName, String displayName) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/120.0.0.0 Safari/537.36");

        String escapedRoom = roomName.replace("'", "\\'");
        String escapedName = displayName.replace("'", "\\'");

        String jitsiHtml = "<!DOCTYPE html><html><head>"
                + "<meta charset=\"utf-8\">"
                + "<script src=\"https://meet.jit.si/external_api.js\"></script>"
                + "</head><body style='margin:0; padding:0; overflow:hidden; height:100%;'>"
                + "<div id='meet' style='width:100%; height:100%;'></div>"
                + "<script>"
                + "const domain='meet.jit.si';"
                + "const options={roomName:'" + escapedRoom + "', width:'100%', height:'100%', parentNode:document.querySelector('#meet'), userInfo:{displayName:'" + escapedName + "'}};"
                + "const api=new JitsiMeetExternalAPI(domain, options);"
                + "</script></body></html>";

        engine.loadContent(jitsiHtml, "text/html");

        this.setTitle("Meeting - " + roomName);
        this.setScene(new Scene(webView, 960, 640));
    }
}
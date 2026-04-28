package com.syndicati.services;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import java.util.function.Consumer;

public class VoiceService {
    private final WebView webView;
    private final WebEngine engine;
    private Consumer<String> onResultListener;
    private Runnable onStartListener;
    private Runnable onStopListener;
    private boolean isListening = false;

    public VoiceService() {
        this.webView = new WebView();
        this.engine = webView.getEngine();
        setupEngine();
    }

    private void setupEngine() {
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", new VoiceBridge());
            }
        });

        // HTML snippet to handle Web Speech API
        String html = "<html><body>" +
                "<script>" +
                "  var recognition = null;" +
                "  try {" +
                "    recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();" +
                "    recognition.continuous = true;" +
                "    recognition.interimResults = true;" +
                "    recognition.lang = 'fr-FR';" +
                "" +
                "    recognition.onstart = () => { javaBridge.onStart(); };" +
                "    recognition.onend = () => { javaBridge.onStop(); };" +
                "    recognition.onresult = (event) => {" +
                "      var transcript = '';" +
                "      for (var i = event.resultIndex; i < event.results.length; ++i) {" +
                "        transcript += event.results[i][0].transcript;" +
                "      }" +
                "      if (transcript) javaBridge.onResult(transcript);" +
                "    };" +
                "    recognition.onerror = (event) => { console.error('Speech error', event.error); javaBridge.onStop(); };" +
                "  } catch(e) { console.error('Recognition init failed', e); }" +
                "" +
                "  function start() { if(recognition) try { recognition.start(); } catch(e) { console.error(e); } }" +
                "  function stop() { if(recognition) try { recognition.stop(); } catch(e) { console.error(e); } }" +
                "</script></body></html>";
        
        engine.loadContent(html);
    }

    public WebView getView() {
        webView.setVisible(false);
        webView.setManaged(false);
        webView.setMaxSize(0, 0);
        return webView;
    }

    public void startListening(Consumer<String> onResult) {
        this.onResultListener = onResult;
        engine.executeScript("start()");
        isListening = true;
    }

    public void stopListening() {
        engine.executeScript("stop()");
        isListening = false;
    }

    public boolean isListening() {
        return isListening;
    }

    public void setOnStart(Runnable onStart) {
        this.onStartListener = onStart;
    }

    public void setOnStop(Runnable onStop) {
        this.onStopListener = onStop;
    }

    /**
     * Bridge class for JS to Java communication
     */
    public class VoiceBridge {
        public void onResult(String text) {
            System.out.println("[VoiceService] Text detected: " + text);
            if (onResultListener != null) {
                onResultListener.accept(text);
            }
        }
        public void onStart() {
            System.out.println("[VoiceService] Microphone is now ON");
            if (onStartListener != null) onStartListener.run();
        }
        public void onStop() {
            System.out.println("[VoiceService] Microphone is now OFF");
            if (onStopListener != null) onStopListener.run();
        }
    }
}

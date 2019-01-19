package kr.heegyu;

public interface OpenGLApp {
    String getTitle();

    void initialize() throws Exception;
    void update(float timeDelta);
    void render();
    void cleanUp();

    void onResize(int width, int height);
    void onFrameBufferSizeChanged(int width, int height);
}

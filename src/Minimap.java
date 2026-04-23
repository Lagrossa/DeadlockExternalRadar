import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.system.MemoryUtil.NULL;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Minimap {

    static boolean blueTeam = false;
    //starting point of 4 enemies
    static float[][] enemies = {

    };
    private long window;

    public void run() {
        init();
        loop();

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        window = glfwCreateWindow(1920, 1080, "Deadlock Radar", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create window");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // vsync i believe

        GL.createCapabilities();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {

            glClear(GL_COLOR_BUFFER_BIT);

            glColor3f(0.1f,0.1f,0.1f); //minimap color
            glBegin(GL_QUADS); // minimap definition start
            glVertex2f(0.5f, -0.95f); //bottom left
            glVertex2f(0.5f, 0.0f); //top left
            glVertex2f(0.95f, 0.0f); //top right
            glVertex2f(0.95f, -0.95f); //bottom right
            glEnd();

            // --- DRAW ENEMIES ---
            for (float[] enemy : enemies) {
                if(enemy[0]== 0 && enemy[1] == 0) continue;
                drawEnemy(enemy[0], enemy[1]);
            }

            glfwSwapBuffers(window); // display what we drew
            glfwPollEvents();
        }
    }

    private void drawEnemy(float x, float y) {
        float size = 0.01f;
        if (blueTeam) { y*= -1; x*=-1; } // Flip map
        // System.out.println("Drawing player at position: " + x + "," + y);

        glColor3f(1f, 0f, 0f); // red enemy

        glBegin(GL_QUADS); // size of "player/enemy" blocks
        glVertex2f(x - size, y - size);
        glVertex2f(x - size, y + size);
        glVertex2f(x + size, y + size);
        glVertex2f(x + size, y - size);
        glEnd();
    }


    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8004), 0);
        server.createContext("/dlgh", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        new Minimap().run();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream in = t.getRequestBody();
            String readLine = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            // System.out.println(br.readLine());
            // Definitely should abstract this into a method
            float[][] newenemies = new float[14][2];
            int count = 0;
            while (((readLine = br.readLine()) != null)) {
                System.out.println(readLine);
                readLine = readLine.substring(2);
                blueTeam = readLine.charAt(0) == '3';
                System.out.println("Team is. . ." + blueTeam);
                readLine = readLine.substring(1, readLine.length()-1);
                String[] splitLines = readLine.split("\\+");
                for(String strings : splitLines){
                    String[] newstring = strings.split(",");
                    newenemies[count][0] = Float.parseFloat(newstring[0])/20000;
                    newenemies[count][1] = Float.parseFloat(newstring[1])/20000;
                    count++;
                }
                enemies = newenemies;
            }
            String response = "";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

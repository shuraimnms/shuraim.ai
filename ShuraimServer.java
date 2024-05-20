import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ShuraimServer extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String text = request.getParameter("text");

        // Call Python script to generate image
        ProcessBuilder processBuilder = new ProcessBuilder("python3", "shuraim_model.py", text);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Serve the generated image
        File imageFile = new File("generated_image.png");
        response.setContentType("image/png");
        response.setContentLength((int) imageFile.length());
        try (FileInputStream fileInputStream = new FileInputStream(imageFile);
             OutputStream responseOutputStream = response.getOutputStream()) {
            int bytes;
            while ((bytes = fileInputStream.read()) != -1) {
                responseOutputStream.write(bytes);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Shuraim.ai</title></head><body>");
        out.println("<form method='POST' action='/generate'>");
        out.println("<input type='text' name='text' placeholder='Enter a description' required>");
        out.println("<button type='submit'>Generate Image</button>");
        out.println("</form>");
        out.println("</body></html>");
    }
}

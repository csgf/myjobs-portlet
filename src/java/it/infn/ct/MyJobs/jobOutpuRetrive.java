package it.infn.ct.MyJobs;

/**
 * ************************************************************************
 * Copyright (c) 2011: Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Consorzio COMETA (COMETA), Italy
 *
 * See http://www.infn.it and and http://www.consorzio-cometa.it for details on
 * the copyright holders.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * **************************************************************************
 */
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import it.infn.ct.GridEngine.Job.JSagaJobSubmission;
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author ricceri
 */
public class jobOutpuRetrive extends HttpServlet {

    public static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();
            // write to the channel, may block
            dest.write(buffer);
            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();
        }
        // EOF will leave buffer in fill state
        buffer.flip();
        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String mode = java.net.URLDecoder.decode(request.getParameter("mode"), "UTF-8");
        String path = java.net.URLDecoder.decode(request.getParameter("Path"), "UTF-8");

        String fileName = null;

        JSagaJobSubmission tmpJSaga = new JSagaJobSubmission();
        tmpJSaga.setOutputPath(path);

        if (mode.equals("all")) {
            String cn = java.net.URLDecoder.decode(request.getParameter("cn"), "UTF-8");
            UsersTrackingDBInterface tracking = new UsersTrackingDBInterface();
            fileName = tracking.createAllJobsArchive(cn, path);
        } else {

            if (mode.equals("single")) {

                String DBid = java.net.URLDecoder.decode(request.getParameter("DBid"), "UTF-8");
                String futuregateway = java.net.URLDecoder.decode(request.getParameter("futuregateway"), "UTF-8");
                if (futuregateway.equalsIgnoreCase("true")) {
                    String fgHost = java.net.URLDecoder.decode(request.getParameter("fgHost"), "UTF-8");
                    String fgPort = java.net.URLDecoder.decode(request.getParameter("fgPort"), "UTF-8");
                    String fgAPIVer = java.net.URLDecoder.decode(request.getParameter("fgAPIVer"), "UTF-8");
                    FGMyJobs fGMyJobs = new FGMyJobs(fgHost, fgPort, fgAPIVer, "");
                    fileName = fGMyJobs.downloadOutputs(DBid, path);
                } else {
                    fileName = path + tmpJSaga.getJobOutput(Integer.parseInt(DBid));
                }
            }
            if (mode.equals("set")) {
                UsersTrackingDBInterface tracking = new UsersTrackingDBInterface();
                String cn = java.net.URLDecoder.decode(request.getParameter("cn"), "UTF-8");
                fileName = tracking.createAllJobsArchive(cn, path);
            }
            if (mode.equals("collection")) {
                String DBid = java.net.URLDecoder.decode(request.getParameter("DBid"), "UTF-8");
                fileName = path + tmpJSaga.getCollectionOutput(Integer.parseInt(DBid));
            }

        }

        File file = new File(fileName);

        System.out.println("!!!!" + file.getName());

        if ((!file.isDirectory()) && file.exists()) {

            String contentType = getServletContext().getMimeType(file.getName());

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.reset();
            response.setContentType(contentType);
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "attachment;filename=\"" + file.getName() + "\"");

            final ReadableByteChannel inputChannel = Channels.newChannel(
                    new FileInputStream(fileName));

            final java.nio.channels.WritableByteChannel outputChannel = Channels.newChannel(
                    response.getOutputStream());

            fastChannelCopy(inputChannel, outputChannel);
        }
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}

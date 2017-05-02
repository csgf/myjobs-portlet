/**
 * ************************************************************************
 * Copyright (c) 2011:
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Consorzio COMETA (COMETA), Italy
 *
 * See http://www.infn.it and and http://www.consorzio-cometa.it for details on the
 * copyright holders.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Riccardo Bruno <riccardo.bruno@ct.infn.it>
 *
 ****************************************************************************
 */
package it.infn.ct.MyJobs;

import java.io.BufferedReader;
import java.util.Enumeration;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import it.infn.ct.GridEngine.UsersTracking.ActiveInteractions;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FGMyJobs class allows to merge GridEngine list of jobs with FutureGateway
 * list of tasks reported tasks will be only the ones related to an application
 * having outcome JOB
 */
public class FGMyJobs {

    private static final String PATH_SUFFIX = "jobOutput";

    private String fgHost;
    private String fgPort;
    private String fgVer;
    private String fgUser;

    private Vector<ActiveInteractions> jobList;
    private Vector<ActiveInteractions> jobListdone;

    public FGMyJobs() {
        fgHost = fgPort = fgVer = fgUser = null;
        jobList = null;
        jobListdone = null;
    }

    public FGMyJobs(String fgHost, String fgPort, String fgVer, String fgUser) {
        super();
        this.fgHost = fgHost;
        this.fgPort = fgPort;
        this.fgVer = fgVer;
        this.fgUser = fgUser;
    }

    private void _log(String mode, String message) {
        System.out.println("[" + mode + "]: " + message);
    }

    // Retrieve the list of pending jobs(tasks)
    public Vector<ActiveInteractions> getJobList() {
        return jobList;
    }

    // Retrieve the list of DONE jobs (tasks)
    public Vector<ActiveInteractions> getJobListDone() {
        return jobListdone;
    }

    // This funciton retrieves the list of jobs from the GridEngine
    public JSONObject fgJobInfoList() throws FuturegatewayException {
        String fgURL = "http://" + fgHost + ":" + fgPort + "/" + fgVer + "/tasks?user=" + fgUser;
        _log("debug", "fgURL=" + fgURL);
        return callFutureGateway(fgURL);
    }

    // This funciton retrieves the list of jobs from the GridEngine
    public JSONObject fgAppInfo(String app_id) throws FuturegatewayException {
        String fgURL = "http://" + fgHost + ":" + fgPort + "/" + fgVer + "/applications/" + app_id + "?user=" + fgUser;
        _log("debug", "fgURL=" + fgURL);
        return callFutureGateway(fgURL);
    }

    // Translate the incoming FutureGateway tasks list into a vector of
    // ActiveInteractions classes
    public void generateActiveInteractionLists() throws FuturegatewayException {
        jobList = new Vector<ActiveInteractions>();
        jobListdone = new Vector<ActiveInteractions>();
        JSONObject taskList = fgJobInfoList();
        if (taskList != null) {
            JSONArray taskArray = (JSONArray) taskList.get("tasks");
            for (int i = 0; i < taskArray.size(); i++) {
                JSONObject taskInfo = (JSONObject) taskArray.get(i);
                String status = (String) taskInfo.get("status");
                String app_id = (String) taskInfo.get("application");
                String last_change = (String) taskInfo.get("last_change");
                // Retrieve Application info
                JSONObject appInfo = fgAppInfo(app_id);
                String task_id = (String) taskInfo.get("id");
                String app_name = (String) appInfo.get("name");
                String app_desc = ((String) taskInfo.get("description")).isEmpty() ? (String) appInfo.get("name") : (String) taskInfo.get("description");
                String outcome = (String) appInfo.get("outcome");
                _log("debug",
                        "status: '" + status + "' "
                        + "outcome: '" + outcome + "' "
                        + "app_name: '" + app_name + "' "
                        + "app_desc: '" + app_desc + "' ");
                String ai_info[] = new String[6];
                ai_info[0] = task_id; //77"0"; // Collection
                ai_info[1] = "FutureGateway"; // Portal
                ai_info[2] = app_name; // Application
                ai_info[3] = app_desc; // Description
                ai_info[4] = last_change; // Timestamp
                ai_info[5] = status; // Statu
                ActiveInteractions ai = new ActiveInteractions(ai_info, null);
                if (outcome.equals("JOB")) {
                    jobList.add(ai);
                }
            }
        }
    }

    public static void main(String args[]) {
        try {
            FGMyJobs fgMyJobs = new FGMyJobs("151.97.41.48", "8888", "v1.0", "mtorrisi");
            fgMyJobs.generateActiveInteractionLists();
            System.out.println("DONE list:");
            Vector<ActiveInteractions> doneList = fgMyJobs.getJobListDone();
            for (Enumeration e = doneList.elements(); e.hasMoreElements();) {
                ActiveInteractions ai = (ActiveInteractions) e.nextElement();
                String ai_info[] = ai.getInteractionInfos();
                System.out.println("<'" + ai_info[0] + "'>"
                        + "<'" + ai_info[1] + "'>"
                        + "<'" + ai_info[2] + "'>"
                        + "<'" + ai_info[3] + "'>"
                        + "<'" + ai_info[4] + "'>"
                        + "<'" + ai_info[5] + "'>");
            }
            System.out.println("Others:");
            Vector<ActiveInteractions> othersList = fgMyJobs.getJobList();
            for (Enumeration e = othersList.elements(); e.hasMoreElements();) {
                ActiveInteractions ai = (ActiveInteractions) e.nextElement();
                String ai_info[] = ai.getInteractionInfos();
                System.out.println("<'" + ai_info[0] + "'>"
                        + "<'" + ai_info[1] + "'>"
                        + "<'" + ai_info[2] + "'>"
                        + "<'" + ai_info[3] + "'>"
                        + "<'" + ai_info[4] + "'>"
                        + "<'" + ai_info[5] + "'>");
            }
        } catch (FuturegatewayException ex) {
            Logger.getLogger(FGMyJobs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String downloadOutputs(String DBid, String path) {
        String result = null;

        try {
            long taskId = Long.parseLong(DBid);

            result = createOutputArchive(taskId, path);

        } catch (NumberFormatException ex) {
            _log("error", ex.getMessage());
        } catch (Exception ex) {
            _log("error", ex.getMessage());
        }

        return result;
    }

    private String createOutputArchive(long taskId, String path) {
        String result = null;
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        StringBuilder fgTaskInfo = new StringBuilder();
        HttpURLConnection conn;
        try {
            String fgURL = "http://" + fgHost + ":" + fgPort + "/" + fgVer + "/tasks/" + taskId;
            _log("debug", "fgURL=" + fgURL);
            URL deploymentEndpoint = new URL(fgURL);
            conn = (HttpURLConnection) deploymentEndpoint.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ln;
                while ((ln = br.readLine()) != null) {
                    fgTaskInfo.append(ln);
                }
            } else {
                _log("debug", "response code: " + conn.getResponseCode());
            }
            result = test(parser, jsonObject, fgTaskInfo, path, taskId);

        } catch (IOException ioex) {
            _log("error", ioex.getMessage());
        } catch (ParseException ex) {
            _log("error", ex.getMessage());
        } catch (InterruptedException ex) {
            _log("error", ex.getMessage());
        }

        return result;
    }

    private String test(JSONParser parser, JSONObject jsonObject, StringBuilder fgTaskInfo, String path, long taskId) throws ParseException, MalformedURLException, IOException, InterruptedException {
        jsonObject = (JSONObject) parser.parse(fgTaskInfo.toString());

        JSONArray jsonArray = (JSONArray) jsonObject.get("output_files");

        File jobOutputFolder = new File(path + File.separator + PATH_SUFFIX);
        if (!jobOutputFolder.exists()) {
            jobOutputFolder.mkdirs();
        }
        File outputArchive = new File(path + File.separator + PATH_SUFFIX + File.separator + taskId + ".tgz");
        if (!outputArchive.exists()) {
            File jobOutputDirectory = new File(jobOutputFolder + File.separator + taskId);
            if (jobOutputDirectory.mkdirs()) {
                for (Iterator it = jsonArray.iterator(); it.hasNext();) {
                    JSONObject jsonOutput = (JSONObject) it.next();
                    URL website = new URL("http://" + fgHost + ":" + fgPort + "/" + fgVer + "/" + jsonOutput.get("url").toString());
                    _log("debug", "Downloading: " + jsonOutput.get("name"));
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    FileOutputStream fos = new FileOutputStream(jobOutputDirectory + File.separator + jsonOutput.get("name").toString());
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
                Process creatingTar = Runtime.getRuntime().exec("tar czvf " + outputArchive + " --directory=" + jobOutputDirectory.getAbsolutePath() + " .");
                creatingTar.waitFor();
                Process deleteFolder = Runtime.getRuntime().exec("rm -Rf " + jobOutputDirectory.getAbsolutePath());
                deleteFolder.waitFor();
            } else {
                _log("error", "error creating " + jobOutputDirectory.getAbsolutePath());
            }
        }

        return outputArchive.getAbsolutePath();
    }

    private JSONObject callFutureGateway(String fgURL) throws FuturegatewayException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        StringBuilder fgAppInfo = new StringBuilder();
        HttpURLConnection conn;
        try {

            URL deploymentEndpoint = new URL(fgURL);
            conn = (HttpURLConnection) deploymentEndpoint.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setConnectTimeout(5000);  // Myjobs tries to call FG and wait 5s otherwise skip FG jobs
            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ln;
                while ((ln = br.readLine()) != null) {
                    fgAppInfo.append(ln);
                }
            } else {
                _log("warning", "response code: " + conn.getResponseCode());
                throw new FuturegatewayException("Futuregateway replies with: " + conn.getResponseCode());
            }
            jsonObject = (JSONObject) parser.parse(fgAppInfo.toString());
        } catch (SocketTimeoutException e) {
            _log("error", e.getMessage());
            throw new FuturegatewayException(e.getMessage());
        } catch (ParseException pex) {
            _log("error", pex.getMessage());
            throw new FuturegatewayException(pex.getMessage());
        } catch (IOException ioex) {
            _log("error", ioex.getMessage());
            throw new FuturegatewayException(ioex.getMessage());
        }
        return jsonObject;
    }

    public class FuturegatewayException extends Exception {

        public FuturegatewayException(String message) {
            super(message);
        }

    }

}

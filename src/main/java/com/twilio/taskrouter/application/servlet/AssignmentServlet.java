package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
//import org.json.JSONObject;
import java.util.Optional;
import javax.json.JsonObject;
import javax.json.Json;
import java.io.StringReader;

/**
 * Servlet for Task assignments
 */
@Singleton
public class AssignmentServlet extends HttpServlet {

    private Map<String, String> dequeueInstruction = new HashMap<String, String>();

    @Inject
    public AssignmentServlet(TwilioAppSettings twilioAppSettings) {

        dequeueInstruction.put("instruction", "dequeue");
        dequeueInstruction.put("post_work_activity_sid",
                twilioAppSettings.getPostWorkActivitySid());
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Optional<JsonObject> temp = parseAttributes("TaskAttributes", req);
        Optional<JsonObject> temp2 = parseAttributes("WorkerAttributes", req);
        System.out.println("AssignmentServlet="
                + " to=" + temp2.get().getString("contact_uri")
                + " from=" + temp.get().getString("to")
                + " TaskSid" + req.getParameter("TaskSid"));
        String toPhone = temp2 != null && temp2.isPresent()
                ? temp2.get().getString("contact_uri") : null;
        String callerPhone = temp != null && temp.isPresent()
                ? (String) temp.get().getString("to") : null;
        resp.setContentType("application/json");
        if (dequeueInstruction.get("to") != null) {
            dequeueInstruction.remove("to");
        }
        if (dequeueInstruction.get("from") != null) {
            dequeueInstruction.remove("from");
        }
        if (toPhone != null) {
            dequeueInstruction.put("to", toPhone);
        }
        if (callerPhone != null) {
            dequeueInstruction.put("from", callerPhone);
        }
        String dequeString = "{";
        dequeString += "\"instruction\":" + "\""
        + dequeueInstruction.get("instruction") + "\",";
        dequeString += "\"to\":" + "\""
        + dequeueInstruction.get("to") + "\",";
        dequeString += "\"from\":" + "\""
        + dequeueInstruction.get("from") + "\",";
        dequeString += "\"post_work_activity_sid\":" + "\""
        + dequeueInstruction.get("post_work_activity_sid") + "\"}";
        resp.getWriter().print(dequeString);
        System.out.println("dequeueInstruction="
        + dequeString);

    }

    private Optional<JsonObject> parseAttributes(String parameter, HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(parameter))
                .map(jsonRequest -> Json.createReader(
                        new StringReader(jsonRequest)).readObject());
    }

}

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
import org.json.JSONObject;

/**
 * Servlet for Task assignments
 */
@Singleton
public class AssignmentServlet extends HttpServlet {

  private Map<String, String> dequeueInstruction = new HashMap<String, String>();

  @Inject
  public AssignmentServlet(TwilioAppSettings twilioAppSettings) {

    dequeueInstruction.put("instruction", "dequeue");
    dequeueInstruction.put("post_work_activity_sid", twilioAppSettings.getPostWorkActivitySid());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    String callerPhone = req.getParameter("From");
    resp.setContentType("application/json");
    if (dequeueInstruction.get("from") != null) {
        dequeueInstruction.remove("from");
    }
    if (callerPhone != null) {
        dequeueInstruction.put("from", callerPhone);
    }
    resp.getWriter().print((new JSONObject(dequeueInstruction)).toString());
    System.out.println("dequeueInstruction=" + (new JSONObject(dequeueInstruction)).toString());

  }
}

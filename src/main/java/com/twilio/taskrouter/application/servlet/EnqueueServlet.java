package com.twilio.taskrouter.application.servlet;

import com.google.inject.persist.Transactional;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.twiml.EnqueueTask;
import com.twilio.twiml.Task;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import com.twilio.taskrouter.domain.repository.MissedCallRepository;
import com.twilio.taskrouter.domain.model.MissedCall;

/**
 * Selects a product by creating a Task on the TaskRouter Workflow
 */
@Singleton
public class EnqueueServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(EnqueueServlet.class.getName());

  private final String workflowSid;
  private final MissedCallRepository missedCallRepository;

  @Inject
  public EnqueueServlet(TwilioAppSettings twilioSettings,
          MissedCallRepository missedCallRepository) {
      this.missedCallRepository = missedCallRepository;
    this.workflowSid = twilioSettings.getWorkflowSid();
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    String selectedProduct = getSelectedProduct(req);
    Task task = new Task.Builder()
      .data(format("{\"selected_product\": \"%s\"}", selectedProduct))
      .build();
    addCall(req.getParameter("From"), selectedProduct);
    EnqueueTask enqueueTask = new EnqueueTask.Builder(task).workflowSid(workflowSid).build();

    VoiceResponse voiceResponse = new VoiceResponse.Builder().enqueue(enqueueTask).build();
    resp.setContentType("application/xml");
    try {
      resp.getWriter().print(voiceResponse.toXml());
    } catch (TwiMLException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private String getSelectedProduct(HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter("Digits"))
      .filter(x -> x.equals("1")).map((first) -> "ProgrammableSMS").orElse("ProgrammableVoice");
  }

  @Transactional
  private void addCall(String from, String selectedProduct) {
      //System.out.println("taskAttributesJson=" + taskAttributesJson.toString());
      String phoneNumber = from;

      MissedCall missedCall = new MissedCall(phoneNumber,
              selectedProduct);
      missedCallRepository.add(missedCall);
      LOG.info("Added Call: " + missedCall);

  }

}

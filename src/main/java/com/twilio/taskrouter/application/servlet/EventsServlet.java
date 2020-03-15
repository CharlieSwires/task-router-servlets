package com.twilio.taskrouter.application.servlet;

import com.google.inject.persist.Transactional;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.model.MissedCall;
import com.twilio.taskrouter.domain.repository.MissedCallRepository;
import com.twilio.type.PhoneNumber;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.twilio.twiml.Number;
import com.twilio.taskrouter.domain.model.WorkspaceFacade;
import com.twilio.twiml.Sms;
//import com.twilio.twiml.TwiMLException;
//import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.Dial;

/**
 * Servlet for Events callback for missed calls
 */
@Singleton
public class EventsServlet extends HttpServlet {

    private static final String LEAVE_MSG = "Sorry, All agents are busy. Please leave a message. "
            + "We will call you as soon as possible";

    private static final String OFFLINE_MSG = "Your status has changed to Offline. "
            + "Reply with \"On\" to get back Online";

    private static final Logger LOG = Logger.getLogger(EventsServlet.class.getName());

    private final TwilioAppSettings twilioSettings;

    private final MissedCallRepository missedCallRepository;
    private final AssignmentServlet assignmentServlet;
    private final WorkspaceFacade workspace;

    @Inject
    public EventsServlet(TwilioAppSettings twilioSettings,
            MissedCallRepository missedCallRepository,
            AssignmentServlet assignmentServlet,
            WorkspaceFacade workspace) {
        this.twilioSettings = twilioSettings;
        this.missedCallRepository = missedCallRepository;
        this.assignmentServlet = assignmentServlet;
        this.workspace = workspace;
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
    IOException {
        Optional.ofNullable(req.getParameter("EventType"))
        .ifPresent(eventName -> {
            System.out.println("eventName=" + eventName + " From=" + req.getParameter("From")
            + " from=" + req.getParameter("from"));
            switch (eventName) {
            case "workflow.timeout":
            case "task.canceled":
                parseAttributes("TaskAttributes", req)
                .ifPresent(this::addMissingCallAndLeaveMessage);
//                break;
//            case "task.wrapup":
              Optional<JsonObject> temp = parseAttributes("TaskAttributes", req);
              if (twilioSettings.getPhoneNumber().getPhoneNumber()
                      .equals(temp.get().getString("from"))) {
//              req.setAttribute("From", "+441430440375");
//                     // (temp.isPresent() ? temp.get().getString("from") : (String) null));
//              this.assignmentServlet.doPost(req, resp);
                try {
                    setIdle(req, resp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
              }
//                try {
//                    Optional<JsonObject> temp = parseAttributes("TaskAttributes", req);
//                    req.setAttribute("From", "+441430440375");
//                           // (temp.isPresent() ? temp.get().getString("from") : (String) null));
//                    this.assignmentServlet.doPost(req, resp);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                break;
            case "worker.activity.update":
                Optional.ofNullable(req.getParameter("WorkerActivityName"))
                .filter("Offline"::equals)
                .ifPresent(offlineEvent -> {
                    parseAttributes("WorkerAttributes", req)
                    .ifPresent(this::notifyOfflineStatusToWorker);
                });
                break;
            default:
            }
        });
    }


    private Optional<JsonObject> parseAttributes(String parameter, HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(parameter))
                .map(jsonRequest -> Json.createReader(new StringReader(jsonRequest)).readObject());
    }

    @Transactional
    private void addMissingCallAndLeaveMessage(JsonObject taskAttributesJson) {
        System.out.println("taskAttributesJson=" + taskAttributesJson.toString());
        String phoneNumber = taskAttributesJson.getString("from");
        String selectedProduct = taskAttributesJson.getString("selected_product");

        MissedCall missedCall = new MissedCall(phoneNumber, selectedProduct);
        missedCallRepository.add(missedCall);
        LOG.info("Added Missing Call: " + missedCall);

        String callSid = taskAttributesJson.getString("call_sid");
        twilioSettings.redirectToVoiceMail(callSid, LEAVE_MSG);
    }

    private void notifyOfflineStatusToWorker(JsonObject workerAttributesJson) {
        try {
        String workerPhone = workerAttributesJson.getString("contact_uri");
        new MessageCreator(
                new PhoneNumber(workerPhone),
                new PhoneNumber(twilioSettings.getPhoneNumber().toString()),
                OFFLINE_MSG
                ).create();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void setIdle(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
//      final VoiceResponse twimlResponse;
      final String newStatus = getNewWorkerStatus(req);
      final String workerPhone = req.getParameter("From");

      try {
          Dial.Builder dialBuilder = new Dial.Builder();

            Number number = new Number.Builder(workerPhone).build();
            dialBuilder = dialBuilder.number(number).callerId(
                    twilioSettings.getPhoneNumber().getPhoneNumber());

          Dial dial = dialBuilder.build();
        Sms responseSms = workspace.findWorkerByPhone(workerPhone).map(worker -> {
          workspace.updateWorkerStatus(worker, newStatus);
          return new Sms.Builder(String.format("Your status has changed to %s", newStatus)).build();
        }).orElseGet(() -> new Sms.Builder("You are not a valid worker").build());

        //twimlResponse = new VoiceResponse.Builder().sms(responseSms).build();
//        twimlResponse = new VoiceResponse.Builder().dial(dial).build();
//
//        resp.setContentType("application/xml");
//        resp.getWriter().print(twimlResponse.toXml());

      } catch (/*TwiML*/ Exception e) {
        LOG.log(Level.SEVERE, "Error while providing answer to a workers' sms", e);
      }

    }

    private String getNewWorkerStatus(HttpServletRequest request) {
      return "Idle";
    }

}

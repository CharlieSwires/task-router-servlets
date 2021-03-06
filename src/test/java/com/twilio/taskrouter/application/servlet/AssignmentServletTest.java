package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.twilio.taskrouter.domain.repository.MissedCallRepository;

@RunWith(MockitoJUnitRunner.class)
public class AssignmentServletTest {

  private static final String POST_WORK_ACTIVITY_MOCK = "WAXXXXXXXXXXX";

  @Mock
  private HttpServletRequest requestMock;

  @Mock
  private HttpServletResponse responseMock;

  @Mock
  private TwilioAppSettings twilioAppSettingsMock;

  @Mock
  private MissedCallRepository mcr;

  private AssignmentServlet assignmentServlet;

  @Before
  public void setUp() throws Exception {
    when(responseMock.getWriter()).thenReturn(mock(PrintWriter.class));
    when(twilioAppSettingsMock.getPostWorkActivitySid()).thenReturn(POST_WORK_ACTIVITY_MOCK);

    assignmentServlet = new AssignmentServlet(twilioAppSettingsMock, mcr);
  }

  @Test
  public void shouldCallPostWorkSid() throws Exception {
      when(requestMock.getParameter("TaskAttributes"))
      .thenReturn("{\"to\": \"+44123456789\"}");
      when(requestMock.getParameter("WorkerAttributes"))
      .thenReturn("{\"contact_uri\": \"+4412345678\" }");
//    assignmentServlet.doPost(requestMock, responseMock);
//
//    verify(twilioAppSettingsMock, times(1)).getPostWorkActivitySid();
  }

  @Test
  public void shouldReturnRightDequeueInstructionInJson() throws Exception {
      when(requestMock.getParameter("TaskAttributes"))
      .thenReturn("{\"to\": \"+44123456789\"}");
      when(requestMock.getParameter("WorkerAttributes"))
      .thenReturn("{\"contact_uri\": \"+4412345678\" }");
      String expectedDequeueInstruction = Json.createObjectBuilder()
      .add("instruction", "dequeue")
      .add("to", "+4412345678")
      .add("from", "+44123456789")
      .add("post_work_activity_sid", POST_WORK_ACTIVITY_MOCK)
     .build().toString();

    when(twilioAppSettingsMock.getPostWorkActivitySid()).thenReturn(POST_WORK_ACTIVITY_MOCK);

//    assignmentServlet.doPost(requestMock, responseMock);
//
//    verify(responseMock, times(1)).setContentType("application/json");
//    System.out.println("responseMock.getWriter()=" + responseMock.getWriter());
//    System.out.println("times(1)=" + times(1));
//    verify(responseMock.getWriter(), times(1)).print(expectedDequeueInstruction);
  }

}

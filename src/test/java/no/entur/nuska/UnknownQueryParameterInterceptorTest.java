package no.entur.nuska;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import no.entur.nuska.config.WebMvcConfig;
import no.entur.nuska.model.NetexDsjVariant;
import no.entur.nuska.security.NuskaAuthorizationService;
import no.entur.nuska.service.NisabaBlobStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.handler.MappedInterceptor;

class UnknownQueryParameterInterceptorTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    NisabaBlobStoreService blobStoreService = mock(
      NisabaBlobStoreService.class
    );
    when(blobStoreService.getLatestBlob(eq("rut"), any(NetexDsjVariant.class)))
      .thenReturn(
        new ByteArrayResource(new byte[0]) {
          @Override
          public String getFilename() {
            return "rut.zip";
          }
        }
      );
    NuskaController controller = new NuskaController(
      mock(NuskaAuthorizationService.class),
      blobStoreService
    );
    mockMvc =
      MockMvcBuilders
        .standaloneSetup(controller)
        .addInterceptors(
          new MappedInterceptor(
            WebMvcConfig.QUERY_PARAMETER_CHECK_INCLUDED_PATHS,
            WebMvcConfig.QUERY_PARAMETER_CHECK_EXCLUDED_PATHS,
            new UnknownQueryParameterInterceptor()
          )
        )
        .build();
  }

  @Test
  void testDeclaredParameterIsAccepted() throws Exception {
    mockMvc
      .perform(
        get("/timetable-data/datasets/rut/latest")
          .param("dsjcompatibility", "new")
      )
      .andExpect(status().isOk());
  }

  @Test
  void testNoParameterIsAccepted() throws Exception {
    mockMvc
      .perform(get("/timetable-data/datasets/rut/latest"))
      .andExpect(status().isOk());
  }

  @Test
  void testParameterWithWrongCaseIsRejected() throws Exception {
    mockMvc
      .perform(
        get("/timetable-data/datasets/rut/latest")
          .param("dsjCompatibility", "new")
      )
      .andExpect(status().isBadRequest())
      .andExpect(result ->
        assertEquals(
          "Unknown query parameter: 'dsjCompatibility'",
          result.getResolvedException().getMessage()
        )
      );
  }

  @Test
  void testUnknownParameterIsRejected() throws Exception {
    mockMvc
      .perform(get("/timetable-data/datasets/rut/versions").param("limit", "5"))
      .andExpect(status().isBadRequest());
  }

  @Test
  void testOpenApiSpecAcceptsAnyParameter() throws Exception {
    mockMvc
      .perform(get("/timetable-data/openapi.yaml").param("v", "123"))
      .andExpect(status().isOk());
  }
}

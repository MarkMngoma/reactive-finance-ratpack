package za.co.ratpack.finance.reactive.functions.handlers.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.CountryCode;
import jakarta.validation.Validation;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ratpack.func.Action;
import ratpack.http.MediaType;
import ratpack.http.Status;
import ratpack.jackson.internal.DefaultJsonRender;
import ratpack.test.handling.HandlingResult;
import ratpack.test.handling.RequestFixture;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.CommandCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel;
import za.co.ratpack.finance.reactive.domain.mybatis.model.mapper.BatchObjectMapper;
import za.co.ratpack.finance.reactive.functions.handlers.ThrowableHandler;
import za.co.ratpack.finance.reactive.functions.helpers.HttpContentHelper;
import za.co.ratpack.finance.reactive.rest.v1.dto.CurrencyRequest;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WriteCurrencyResourceHandlerTest {

  @InjectMocks
  WriteCurrencyResourceHandler handlerUnderTest;

  @Mock
  HttpContentHelper httpContentHelper;

  @Mock
  ThrowableHandler throwableHandler;

  @Mock
  CommandCurrencyDao commandCurrencyDao;

  @Mock
  BatchObjectMapper<CurrencyEntityModel, CurrencyRequest> batchObjectMapper;

  private ObjectMapper objectMapper;


  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();
    batchObjectMapper = new BatchObjectMapper<>(new ModelMapper());
    httpContentHelper = new HttpContentHelper();
    handlerUnderTest = new WriteCurrencyResourceHandler(httpContentHelper, throwableHandler, commandCurrencyDao, batchObjectMapper);
  }

  @Test
  void givenRequestPayloadSuppliedWhenClientRequestingCurrencyCreationThenVerifySuccessResult() throws Exception {
    // Given -- requesting user is supplying correct request
    var requestPayload = this.constructDummyCurrencyRequest();
    requestPayload.setCurrencyId(710);

    // When -- handler processes the request
    HandlingResult handlingResult = RequestFixture.handle(handlerUnderTest, this.givenCurrencyRequest(objectMapper.writeValueAsString(requestPayload)));
    var handlerResponse = handlingResult.getBodyText();

    // Then -- handler should return a success status code and a response body
    assertEquals(Status.CREATED, handlingResult.getStatus());
    assertEquals("{\"success\": true}", handlerResponse);
  }

  @Test
  void givenRequestPayloadSuppliedWhenClientRequestingCurrencyBadRequestThenVerifyFailureResult() throws Exception {
    // Given -- requesting user is supplying incorrect request
    var requestPayload = this.constructDummyCurrencyRequest();

    // When -- handler processes the request
    HandlingResult handlingResult = RequestFixture.handle(handlerUnderTest, this.givenCurrencyRequest(objectMapper.writeValueAsString(requestPayload)));
    var handlerResponse = (Map<String, Object>) handlingResult.rendered(DefaultJsonRender.class).getObject();

    // Then -- handler should return a failure status code and a response body
    assertEquals(Status.BAD_REQUEST, handlingResult.getStatus());
    assertEquals("Currency id is a required field", handlerResponse.get("currencyId"));
  }

  private Action<RequestFixture> givenCurrencyRequest(String request) {
    return requestFixture ->
      requestFixture
        .uri("/v1/WriteCurrencyResource")
        .body(request, MediaType.APPLICATION_JSON)
        .getRegistry()
        .add(objectMapper)
        .add(Validation.byDefaultProvider()
          .configure()
          .messageInterpolator(new ParameterMessageInterpolator())
          .buildValidatorFactory()
          .getValidator()
        )
        .add(request);
  }

  private CurrencyRequest constructDummyCurrencyRequest() {
    return new CurrencyRequest()
      .setCurrencyCode(CountryCode.ZA.getCurrency().getCurrencyCode())
      .setCurrencyFlag("🇿🇦")
      .setCurrencySymbol(CountryCode.ZA.getCurrency().getSymbol())
      .setCurrencyName(CountryCode.ZA.getCurrency().getDisplayName());
  }
}

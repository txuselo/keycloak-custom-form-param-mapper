package org.skyc.keycloak.authentication;

import org.junit.jupiter.api.Test;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.FullNameMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.mockito.Mockito;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CustomFormParamMapperTest {

  static final String CLAIM_NAME = "exampleClaimName";
  static final String PARAM_NAME = "exampleParamName";

  @Test
  public void shouldTokenMapperDisplayCategory() {
    final String tokenMapperDisplayCategory = new FullNameMapper().getDisplayCategory();
    assertThat(new CustomFormParamMapper().getDisplayCategory()).isEqualTo(tokenMapperDisplayCategory);
  }

  @Test
  public void shouldHaveDisplayType() {
    assertThat(new CustomFormParamMapper().getDisplayType()).isNotBlank();
  }

  @Test
  public void shouldHaveHelpText() {
    assertThat(new CustomFormParamMapper().getHelpText()).isNotBlank();
  }

  @Test
  public void shouldHaveIdId() {
    assertThat(new CustomFormParamMapper().getId()).isNotBlank();
  }

  @Test
  public void shouldHaveProperties() {
    final List<String> configPropertyNames = new CustomFormParamMapper().getConfigProperties().stream()
        .map(ProviderConfigProperty::getName)
        .collect(Collectors.toList());
    assertThat(configPropertyNames).containsExactly(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME,
        OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN,
        OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, CustomFormParamMapper.CONFIG_FORM_PARAM_NAME);
  }

  @Test
  public void shouldAddClaim() {
    final UserSessionModel userSession = givenUserSession();
    final KeycloakSession keycloakSession = givenKeycloakSession();

    final AccessToken accessToken = transformAccessToken(userSession, keycloakSession);
    System.out.println(accessToken.getOtherClaims().toString());
    assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isEqualTo("exampleValue");
  }

  private UserSessionModel givenUserSession() {
    UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
    UserModel user = Mockito.mock(UserModel.class);
    when(userSession.getUser()).thenReturn(user);
    return userSession;
  }

  private KeycloakSession givenKeycloakSession() {
    KeycloakSession session = Mockito.mock(KeycloakSession.class);
    KeycloakContext keycloakContext = Mockito.mock(KeycloakContext.class);
    HttpRequest httpRequest = Mockito.mock(HttpRequest.class);
    // Set up the mocks for the Keycloak context and HTTP request
    when(session.getContext()).thenReturn(keycloakContext);
    when(keycloakContext.getHttpRequest()).thenReturn(httpRequest);

    // Mock the form parameters that will be returned by getDecodedFormParameters
    MultivaluedMap<String, String> formParameters = new MultivaluedHashMap<>();
    formParameters.putSingle(PARAM_NAME, "exampleValue");

    // When getDecodedFormParameters is called, return the mock form parameters
    when(httpRequest.getDecodedFormParameters()).thenReturn(formParameters);

    return session;
  }

  private AccessToken transformAccessToken(UserSessionModel userSessionModel, KeycloakSession keycloakSession) {
    final ProtocolMapperModel mappingModel = new ProtocolMapperModel();
    mappingModel.setConfig(createConfig());
    return new CustomFormParamMapper().transformAccessToken(new AccessToken(), mappingModel, keycloakSession, userSessionModel,
        null);
  }

  private Map<String, String> createConfig() {
    final Map<String, String> result = new HashMap<>();
    result.put("access.token.claim", "true");
    result.put("claim.name", CLAIM_NAME);
    result.put(CustomFormParamMapper.CONFIG_FORM_PARAM_NAME, PARAM_NAME);
    return result;
  }
}
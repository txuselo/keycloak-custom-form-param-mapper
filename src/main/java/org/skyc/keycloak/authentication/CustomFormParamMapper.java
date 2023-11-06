package org.skyc.keycloak.authentication;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;


import org.jboss.logging.Logger;


public class CustomFormParamMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper  {
  private static final Logger logger = Logger.getLogger(CustomFormParamMapper.class);

  public static final String TOKEN_MAPPER_CATEGORY = "Token mapper";

  public static final String PROVIDER_ID = "custom-form-param-mapper";

  public static final String CONFIG_FORM_PARAM_NAME = "form.param.name";

  /*
   * A config which keycloak uses to display a generic dialog to configure the
   * token.
   */
  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
    OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomFormParamMapper.class);

    ProviderConfigProperty formParamName = new ProviderConfigProperty();
    formParamName.setName(CustomFormParamMapper.CONFIG_FORM_PARAM_NAME);
    formParamName.setLabel("HTTP Form Param Name");
    formParamName.setType(ProviderConfigProperty.STRING_TYPE);
    formParamName.setHelpText("Name of the HTTP form param to map as a token claim.");
    configProperties.add(formParamName);

  }

  @Override
  public String getDisplayCategory() {
    return TOKEN_MAPPER_CATEGORY;
  }

  @Override
  public String getDisplayType() {
    return "Request Form Param Mapper";
  }

  @Override
  public String getHelpText() {
    return "Maps a request form param to a token claim.";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }

  @Override
  public String getProtocol() {
    return OIDCLoginProtocol.LOGIN_PROTOCOL;
  }

  @Override
  public void close() {

  }

  @Override
  public void init(Config.Scope config) {
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  protected void setClaim(final IDToken token,
      final ProtocolMapperModel mappingModel,
      final UserSessionModel userSession,
      final KeycloakSession keycloakSession,
      final ClientSessionContext clientSessionCtx) {

    String formParamName = mappingModel.getConfig().get(CustomFormParamMapper.CONFIG_FORM_PARAM_NAME);

    if (formParamName == null) {
      logger.warn("HTTP Form Param Name is not configured for the mapper.");
      return;
    }
    
    String formParamValue = keycloakSession.getContext().getHttpRequest().getDecodedFormParameters().getFirst(formParamName);
    if (formParamValue != null && !formParamValue.isEmpty()) {
      logger.debugv("Mapping form param {0} with value {1} to {2} token claim.", formParamName, formParamValue, formParamName);
      OIDCAttributeMapperHelper.mapClaim(token, mappingModel, formParamValue);
    }

  }

}


package ai.chat2db.server.web.api.controller.config;

import java.util.Objects;

import ai.chat2db.server.domain.api.enums.AiSqlSourceEnum;
import ai.chat2db.server.domain.api.model.AIConfig;
import ai.chat2db.server.domain.api.model.ChatGptConfig;
import ai.chat2db.server.domain.api.model.Config;
import ai.chat2db.server.domain.api.param.SystemConfigParam;
import ai.chat2db.server.domain.api.service.ConfigService;
import ai.chat2db.server.tools.base.wrapper.result.ActionResult;
import ai.chat2db.server.tools.base.wrapper.result.DataResult;
import ai.chat2db.server.web.api.aspect.ConnectionInfoAspect;
import ai.chat2db.server.web.api.controller.ai.azure.client.AzureOpenAIClient;
import ai.chat2db.server.web.api.controller.config.request.AIConfigCreateRequest;
import ai.chat2db.server.web.api.controller.config.request.AISystemConfigRequest;
import ai.chat2db.server.web.api.controller.config.request.SystemConfigRequest;
import ai.chat2db.server.web.api.util.OpenAIClient;
import ai.chat2db.server.web.api.controller.ai.rest.client.RestAIClient;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jipengfei
 * @version : ConfigController.java
 */
@ConnectionInfoAspect
@RequestMapping("/api/config")
@RestController
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @PostMapping("/system_config")
    public ActionResult systemConfig(@RequestBody SystemConfigRequest request) {
        SystemConfigParam param = SystemConfigParam.builder().code(request.getCode()).content(request.getContent())
            .build();
        configService.createOrUpdate(param);
        if (OpenAIClient.OPENAI_KEY.equals(request.getCode())) {
            OpenAIClient.refresh();
        }
        return ActionResult.isSuccess();
    }

    /**
     * save ai config
     *
     * @param request
     * @return
     */
    @PostMapping("/system_config/chatgpt")
    public ActionResult addAiSystemConfig(@RequestBody AISystemConfigRequest request) {
        String sqlSource = StringUtils.isNotBlank(request.getAiSqlSource()) ? request.getAiSqlSource()
            : AiSqlSourceEnum.CHAT2DBAI.getCode();
        AiSqlSourceEnum aiSqlSourceEnum = AiSqlSourceEnum.getByName(sqlSource);
        if (Objects.isNull(aiSqlSourceEnum)) {
            aiSqlSourceEnum = AiSqlSourceEnum.CHAT2DBAI;
            sqlSource = AiSqlSourceEnum.CHAT2DBAI.getCode();
        }
        SystemConfigParam param = SystemConfigParam.builder().code(RestAIClient.AI_SQL_SOURCE).content(sqlSource)
            .build();
        configService.createOrUpdate(param);

        switch (Objects.requireNonNull(aiSqlSourceEnum)) {
            case OPENAI :
                saveOpenAIConfig(request);
                break;
            case CHAT2DBAI:
                saveChat2dbAIConfig(request);
                break;
            case RESTAI :
                saveRestAIConfig(request);
                break;
            case AZUREAI :
                saveAzureAIConfig(request);
                break;
        }
        return ActionResult.isSuccess();
    }

    /**
     * 保存ChatGPT相关配置
     *
     * @param request
     * @return
     */
    @PostMapping("/system_config/ai")
    public ActionResult addChatGptSystemConfig(@RequestBody AIConfigCreateRequest request) {
        String sqlSource = request.getAiSqlSource();
        AiSqlSourceEnum aiSqlSourceEnum = AiSqlSourceEnum.getByName(sqlSource);
        if (Objects.isNull(aiSqlSourceEnum)) {
            sqlSource = AiSqlSourceEnum.CHAT2DBAI.getCode();
            aiSqlSourceEnum = AiSqlSourceEnum.CHAT2DBAI;
        }
        SystemConfigParam param = SystemConfigParam.builder().code(RestAIClient.AI_SQL_SOURCE).content(sqlSource)
            .build();
        configService.createOrUpdate(param);

        switch (Objects.requireNonNull(aiSqlSourceEnum)) {
            case OPENAI :
                saveOpenAIConfig(request);
                break;
            case CHAT2DBAI:
                saveChat2dbAIConfig(request);
                break;
            case RESTAI :
                saveRestAIConfig(request);
                break;
            case AZUREAI :
                saveAzureAIConfig(request);
                break;
        }
        return ActionResult.isSuccess();
    }

    /**
     * save chat2db ai config
     *
     * @param request
     */
    private void saveChat2dbAIConfig(AIConfigCreateRequest request) {
        SystemConfigParam param = SystemConfigParam.builder().code(OpenAIClient.OPENAI_KEY).content(
            request.getApiKey()).build();
        configService.createOrUpdate(param);
        SystemConfigParam hostParam = SystemConfigParam.builder().code(OpenAIClient.OPENAI_HOST).content(
            request.getApiHost()).build();
        configService.createOrUpdate(hostParam);
        OpenAIClient.refresh();
    }

    /**
     * save open ai config
     *
     * @param request
     */
    private void saveOpenAIConfig(AIConfigCreateRequest request) {
        SystemConfigParam param = SystemConfigParam.builder().code(OpenAIClient.OPENAI_KEY).content(
            request.getApiKey()).build();
        configService.createOrUpdate(param);
        SystemConfigParam hostParam = SystemConfigParam.builder().code(OpenAIClient.OPENAI_HOST).content(
            request.getApiHost()).build();
        configService.createOrUpdate(hostParam);
        SystemConfigParam httpProxyHostParam = SystemConfigParam.builder().code(OpenAIClient.PROXY_HOST).content(
            request.getHttpProxyHost()).build();
        configService.createOrUpdate(httpProxyHostParam);
        SystemConfigParam httpProxyPortParam = SystemConfigParam.builder().code(OpenAIClient.PROXY_PORT).content(
            request.getHttpProxyPort()).build();
        configService.createOrUpdate(httpProxyPortParam);
        OpenAIClient.refresh();
    }

    /**
     * save rest ai config
     *
     * @param request
     */
    private void saveRestAIConfig(AIConfigCreateRequest request) {
        SystemConfigParam restParam = SystemConfigParam.builder().code(RestAIClient.REST_AI_URL).content(
                request.getApiHost()).build();
        configService.createOrUpdate(restParam);
        SystemConfigParam methodParam = SystemConfigParam.builder().code(RestAIClient.REST_AI_STREAM_OUT).content(
            request.getStream().toString()).build();
        configService.createOrUpdate(methodParam);
        RestAIClient.refresh();
    }

    /**
     * save azure config
     *
     * @param request
     */
    private void saveAzureAIConfig(AIConfigCreateRequest request) {
        SystemConfigParam apikeyParam = SystemConfigParam.builder().code(AzureOpenAIClient.AZURE_CHATGPT_API_KEY).content(
            request.getApiKey()).build();
        configService.createOrUpdate(apikeyParam);
        SystemConfigParam endpointParam = SystemConfigParam.builder().code(AzureOpenAIClient.AZURE_CHATGPT_ENDPOINT).content(
            request.getApiHost()).build();
        configService.createOrUpdate(endpointParam);
        SystemConfigParam modelParam = SystemConfigParam.builder().code(AzureOpenAIClient.AZURE_CHATGPT_DEPLOYMENT_ID).content(
            request.getModel()).build();
        configService.createOrUpdate(modelParam);
        AzureOpenAIClient.refresh();
    }

    /**
     * 保存OPENAI相关配置
     *
     * @param request
     */
    private void saveChat2dbAIConfig(AISystemConfigRequest request) {
        SystemConfigParam param = SystemConfigParam.builder().code(OpenAIClient.OPENAI_KEY).content(
                request.getChat2dbApiKey()).build();
        configService.createOrUpdate(param);
        SystemConfigParam hostParam = SystemConfigParam.builder().code(OpenAIClient.OPENAI_HOST).content(
                request.getChat2dbApiHost()).build();
        configService.createOrUpdate(hostParam);
        OpenAIClient.refresh();
    }

    /**
     * 保存OPENAI相关配置
     *
     * @param request
     */
    private void saveOpenAIConfig(AISystemConfigRequest request) {
        SystemConfigParam param = SystemConfigParam.builder().code(OpenAIClient.OPENAI_KEY).content(
                request.getApiKey()).build();
        configService.createOrUpdate(param);
        SystemConfigParam hostParam = SystemConfigParam.builder().code(OpenAIClient.OPENAI_HOST).content(
                request.getApiHost()).build();
        configService.createOrUpdate(hostParam);
        SystemConfigParam httpProxyHostParam = SystemConfigParam.builder().code(OpenAIClient.PROXY_HOST).content(
            request.getHttpProxyHost()).build();
        configService.createOrUpdate(httpProxyHostParam);
        SystemConfigParam httpProxyPortParam = SystemConfigParam.builder().code(OpenAIClient.PROXY_PORT).content(
            request.getHttpProxyPort()).build();
        configService.createOrUpdate(httpProxyPortParam);
        OpenAIClient.refresh();
    }

    /**
     * 保存RESTAI接口相关配置
     *
     * @param request
     */
    private void saveRestAIConfig(AISystemConfigRequest request) {
        SystemConfigParam restParam = SystemConfigParam.builder().code(RestAIClient.REST_AI_URL).content(
                request.getRestAiUrl())
            .build();
        configService.createOrUpdate(restParam);
        SystemConfigParam methodParam = SystemConfigParam.builder().code(RestAIClient.REST_AI_STREAM_OUT).content(
            request.getRestAiStream().toString()).build();
        configService.createOrUpdate(methodParam);
        RestAIClient.refresh();
    }

    /**
     * 保存azure配置
     *
     * @param request
     */
    private void saveAzureAIConfig(AISystemConfigRequest request) {
        SystemConfigParam apikeyParam = SystemConfigParam.builder().code(AzureOpenAIClient.AZURE_CHATGPT_API_KEY).content(
            request.getAzureApiKey()).build();
        configService.createOrUpdate(apikeyParam);
        SystemConfigParam endpointParam = SystemConfigParam.builder().code(AzureOpenAIClient.AZURE_CHATGPT_ENDPOINT).content(
            request.getAzureEndpoint()).build();
        configService.createOrUpdate(endpointParam);
        SystemConfigParam modelParam = SystemConfigParam.builder().code(AzureOpenAIClient.AZURE_CHATGPT_DEPLOYMENT_ID).content(
            request.getAzureDeploymentId()).build();
        configService.createOrUpdate(modelParam);
        AzureOpenAIClient.refresh();
    }

    @GetMapping("/system_config/{code}")
    public DataResult<Config> getSystemConfig(@PathVariable("code") String code) {
        DataResult<Config> result = configService.find(code);
        return DataResult.of(result.getData());
    }

    /**
     * 查询ChatGPT相关配置
     *
     * @return
     */
    @GetMapping("/system_config/ai")
    public DataResult<AIConfig> getChatAiSystemConfig(String aiSqlSource) {
        DataResult<Config> dbSqlSource = configService.find(RestAIClient.AI_SQL_SOURCE);
        if (StringUtils.isBlank(aiSqlSource)) {
            if (Objects.nonNull(dbSqlSource.getData())) {
                aiSqlSource = dbSqlSource.getData().getContent();
            }
        }
        AIConfig config = new AIConfig();
        AiSqlSourceEnum aiSqlSourceEnum = AiSqlSourceEnum.getByName(aiSqlSource);
        if (Objects.isNull(aiSqlSourceEnum)) {
            aiSqlSource = AiSqlSourceEnum.CHAT2DBAI.getCode();
            config.setAiSqlSource(aiSqlSource);
            return DataResult.of(config);
        }
        config.setAiSqlSource(aiSqlSource);
        switch (Objects.requireNonNull(aiSqlSourceEnum)) {
            case OPENAI :
                if (!StringUtils.equals(dbSqlSource.getData().getContent(), AiSqlSourceEnum.CHAT2DBAI.getCode())) {
                    DataResult<Config> apiKey = configService.find(OpenAIClient.OPENAI_KEY);
                    DataResult<Config> apiHost = configService.find(OpenAIClient.OPENAI_HOST);
                    DataResult<Config> httpProxyHost = configService.find(OpenAIClient.PROXY_HOST);
                    DataResult<Config> httpProxyPort = configService.find(OpenAIClient.PROXY_PORT);
                    config.setApiKey(Objects.nonNull(apiKey.getData()) ? apiKey.getData().getContent() : "");
                    config.setApiHost(Objects.nonNull(apiHost.getData()) ? apiHost.getData().getContent() : "");
                    config.setHttpProxyHost(Objects.nonNull(httpProxyHost.getData()) ? httpProxyHost.getData().getContent() : "");
                    config.setHttpProxyPort(Objects.nonNull(httpProxyPort.getData()) ? httpProxyPort.getData().getContent() : "");
                }
                break;
            case CHAT2DBAI:
                if (!StringUtils.equals(dbSqlSource.getData().getContent(), AiSqlSourceEnum.OPENAI.getCode())) {
                    DataResult<Config> apiKey = configService.find(OpenAIClient.OPENAI_KEY);
                    DataResult<Config> apiHost = configService.find(OpenAIClient.OPENAI_HOST);
                    config.setApiKey(Objects.nonNull(apiKey.getData()) ? apiKey.getData().getContent() : "");
                    config.setApiHost(Objects.nonNull(apiHost.getData()) ? apiHost.getData().getContent() : "");
                }
                break;
            case AZUREAI:
                DataResult<Config> azureApiKey = configService.find(AzureOpenAIClient.AZURE_CHATGPT_API_KEY);
                DataResult<Config> azureEndpoint = configService.find(AzureOpenAIClient.AZURE_CHATGPT_ENDPOINT);
                DataResult<Config> azureDeployId = configService.find(AzureOpenAIClient.AZURE_CHATGPT_DEPLOYMENT_ID);
                config.setApiKey(Objects.nonNull(azureApiKey.getData()) ? azureApiKey.getData().getContent() : "");
                config.setApiHost(Objects.nonNull(azureEndpoint.getData()) ? azureEndpoint.getData().getContent() : "");
                config.setModel(Objects.nonNull(azureDeployId.getData()) ? azureDeployId.getData().getContent() : "");
                break;
            case RESTAI:
                DataResult<Config> restAiUrl = configService.find(RestAIClient.REST_AI_URL);
                DataResult<Config> restAiHttpMethod = configService.find(RestAIClient.REST_AI_STREAM_OUT);
                config.setApiHost(Objects.nonNull(restAiUrl.getData()) ? restAiUrl.getData().getContent() : "");
                config.setStream(Objects.nonNull(restAiHttpMethod.getData()) ? Boolean.valueOf(
                    restAiHttpMethod.getData().getContent()) : Boolean.TRUE);
                break;
            default:
                break;
        }

        return DataResult.of(config);
    }

    /**
     * 查询ChatGPT相关配置
     *
     * @return
     */
    @GetMapping("/system_config/chatgpt")
    public DataResult<ChatGptConfig> getChatGptSystemConfig() {
        DataResult<Config> apiKey = configService.find(OpenAIClient.OPENAI_KEY);
        DataResult<Config> apiHost = configService.find(OpenAIClient.OPENAI_HOST);
        DataResult<Config> httpProxyHost = configService.find(OpenAIClient.PROXY_HOST);
        DataResult<Config> httpProxyPort = configService.find(OpenAIClient.PROXY_PORT);
        DataResult<Config> aiSqlSource = configService.find(RestAIClient.AI_SQL_SOURCE);
        DataResult<Config> restAiUrl = configService.find(RestAIClient.REST_AI_URL);
        DataResult<Config> restAiHttpMethod = configService.find(RestAIClient.REST_AI_STREAM_OUT);
        DataResult<Config> azureApiKey = configService.find(AzureOpenAIClient.AZURE_CHATGPT_API_KEY);
        DataResult<Config> azureEndpoint = configService.find(AzureOpenAIClient.AZURE_CHATGPT_ENDPOINT);
        DataResult<Config> azureDeployId = configService.find(AzureOpenAIClient.AZURE_CHATGPT_DEPLOYMENT_ID);
        ChatGptConfig config = new ChatGptConfig();

        String sqlSource = Objects.nonNull(aiSqlSource.getData()) ? aiSqlSource.getData().getContent() : AiSqlSourceEnum.CHAT2DBAI.getCode();
        AiSqlSourceEnum aiSqlSourceEnum = AiSqlSourceEnum.getByName(sqlSource);
        if (Objects.isNull(aiSqlSourceEnum)) {
            aiSqlSourceEnum = AiSqlSourceEnum.CHAT2DBAI;
            sqlSource = AiSqlSourceEnum.CHAT2DBAI.getCode();
        }
        config.setAiSqlSource(sqlSource);
        switch (Objects.requireNonNull(aiSqlSourceEnum)) {
            case OPENAI :
                config.setApiKey(Objects.nonNull(apiKey.getData()) ? apiKey.getData().getContent() : null);
                config.setApiHost(Objects.nonNull(apiHost.getData()) ? apiHost.getData().getContent() : null);
                config.setChat2dbApiKey("");
                config.setChat2dbApiHost("");
                break;
            case CHAT2DBAI:
                config.setApiKey("");
                config.setApiHost("");
                config.setChat2dbApiKey(Objects.nonNull(apiKey.getData()) ? apiKey.getData().getContent() : null);
                config.setChat2dbApiHost(Objects.nonNull(apiHost.getData()) ? apiHost.getData().getContent() : null);
                break;
        }
        config.setRestAiUrl(Objects.nonNull(restAiUrl.getData()) ? restAiUrl.getData().getContent() : null);
        config.setRestAiStream(Objects.nonNull(restAiHttpMethod.getData()) ? Boolean.valueOf(
            restAiHttpMethod.getData().getContent()) : Boolean.TRUE);
        config.setHttpProxyHost(Objects.nonNull(httpProxyHost.getData()) ? httpProxyHost.getData().getContent() : null);
        config.setHttpProxyPort(Objects.nonNull(httpProxyPort.getData()) ? httpProxyPort.getData().getContent() : null);
        config.setAzureApiKey(Objects.nonNull(azureApiKey.getData()) ? azureApiKey.getData().getContent() : null);
        config.setAzureEndpoint(Objects.nonNull(azureEndpoint.getData()) ? azureEndpoint.getData().getContent() : null);
        config.setAzureDeploymentId(Objects.nonNull(azureDeployId.getData()) ? azureDeployId.getData().getContent() : null);
        return DataResult.of(config);
    }
}

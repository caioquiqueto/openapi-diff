package com.qdesrame.openapi.diff.output;

import com.qdesrame.openapi.diff.model.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONRender implements Render {

    @Override
    public String render(ChangedOpenApi diff) {

        JSONObject jsonRender = new JSONObject();

        if (diff.isUnchanged())
        {
            jsonRender.put("Results", "Dont have any changes");
        }
        else
        {
            JSONArray newRender = new JSONArray();
            JSONArray missingRender = new JSONArray();
            JSONArray changedRender = new JSONArray();

            List<Endpoint> newEndpoints = diff.getNewEndpoints();
            newEndpoints.forEach(newEndpoint -> newRender.put(newEndpoint));

            List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
            missingEndpoints.forEach(missingEndpoint -> missingRender.put(missingEndpoint));

            List<ChangedOperation> changedOperations = diff.getChangedOperations();
            changedOperations.forEach(changedOperation ->{
                JSONObject operationRender = new JSONObject();
                operationRender.put("pathURL",changedOperation.getPathUrl());
                operationRender.put("httpMethod",changedOperation.getHttpMethod());
                operationRender.put("description",changedOperation.getDescription());
                operationRender.put("parameters",list_param(changedOperation.getParameters()));
                operationRender.put("request",list_content(changedOperation.getRequestBody()));
                operationRender.put("returnType",list_response(changedOperation.getApiResponses()));
                operationRender.put("isBackwardCompatible",changedOperation.isCompatible());
                changedRender.put(operationRender);
            });

            jsonRender.put("added", newRender);
            jsonRender.put("deleted", missingRender);
            jsonRender.put("changed", changedRender);
            jsonRender.put("isBackwardCompatible",diff.isCompatible());

        }

        return jsonRender.toString();
    }

    private JSONObject list_param(ChangedParameters changedParameters) {
        if (changedParameters == null){
            return null;
        }

        List<Parameter> addParameters = changedParameters.getIncreased();
        List<Parameter> deleteParameters = changedParameters.getMissing();
        List<ChangedParameter> changed = changedParameters.getChanged();

        JSONArray addParametersJSON= new JSONArray();
        JSONArray deleteParametersJSON = new JSONArray();
        JSONArray changedParametersJSON = new JSONArray();


        JSONObject jsonReturn = new JSONObject();

        for (Parameter param : addParameters) {
            addParametersJSON.put(itemParam(param));
        }
        jsonReturn.put("added",addParametersJSON);

        for (Parameter param : deleteParameters) {
            deleteParametersJSON.put(itemParam(param));
        }
        jsonReturn.put("deleted",deleteParametersJSON);

        for (ChangedParameter param : changed) {
            changedParametersJSON.put(list_changedParam(param));
        }
        jsonReturn.put("changed",changedParametersJSON);

        jsonReturn.put("isBackwardCompatible",changedParameters.isCompatible());

        return jsonReturn;
    }

    private JSONObject itemParam(Parameter param) {
        JSONObject JSONReturn = new JSONObject();
        JSONReturn.put("name",param.getName());
        JSONReturn.put("in",param.getIn());
        return JSONReturn;
    }

    private JSONObject list_changedParam(ChangedParameter changeParam) {
        JSONObject jsonReturn = new JSONObject();

        if (changeParam.isDeprecated()) {
            jsonReturn.put("deprecated",itemParam(changeParam.getNewParameter()));
        } else {
            jsonReturn.put("changed",itemParam(changeParam.getNewParameter()));
        }
        return jsonReturn;
    }

    private JSONObject list_content(ChangedRequestBody changedRequestBody) {
        //Protection to not access a property of a null
        if (changedRequestBody == null) {
            return null;
        }
        return list_content(changedRequestBody.getContent());
    }

    private JSONObject list_content(ChangedContent changedContent) {

        JSONObject jsonReturn = new JSONObject();

        if (changedContent == null) {
            return jsonReturn;
        }
        for (String propName : changedContent.getIncreased().keySet()) {
            jsonReturn.put("added",propName);
        }
        for (String propName : changedContent.getMissing().keySet()) {
            jsonReturn.put("deleted",propName);
        }
        for (String propName : changedContent.getChanged().keySet()) {
            jsonReturn.put("changed",
                    itemContent( propName, changedContent.getChanged().get(propName)));
        }

        jsonReturn.put("isBackwardCompatible",changedContent.isCompatible());
        return jsonReturn;
    }

    private JSONObject itemContent(
            String contentType, ChangedMediaType changedMediaType) {
        JSONObject JSONReturn = new JSONObject();

        JSONReturn.put("contentType",contentType);
        JSONReturn.put("IsBackwardCompatible",changedMediaType.isCompatible());

        return JSONReturn;
    }

    private JSONObject list_response(ChangedApiResponse changedApiResponse) {
        if (changedApiResponse == null) {
            return null;
        }
        Map<String, ApiResponse> addResponses = changedApiResponse.getIncreased();
        Map<String, ApiResponse> deleteResponses = changedApiResponse.getMissing();
        Map<String, ChangedResponse> changedResponses = changedApiResponse.getChanged();

        JSONArray addResponsesJSON = new JSONArray();
        JSONArray deleteResponsesJSON = new JSONArray();
        JSONArray changedResponsesJSON = new JSONArray();

        JSONObject jsonReturn = new JSONObject();

        for (String propName : addResponses.keySet()) {
            addResponsesJSON.put(propName);
        }
        for (String propName : deleteResponses.keySet()) {
            deleteResponsesJSON.put(propName);
        }
        for (String propName : changedResponses.keySet()) {
            changedResponsesJSON.put(itemChangedResponse( propName, changedResponses.get(propName)));
        }

        jsonReturn.put("added",addResponsesJSON);
        jsonReturn.put("deleted",deleteResponsesJSON);
        jsonReturn.put("changed",changedResponsesJSON);
        jsonReturn.put("isBackwardCompatiple",changedApiResponse.isCompatible());

        return jsonReturn;
    }

    private JSONObject itemChangedResponse( String contentType, ChangedResponse response) {
        JSONObject jsonReturn = new JSONObject();

        jsonReturn.put("contentType", contentType);
        jsonReturn.put("mediaType", list_content(response.getContent()));
        return jsonReturn;
    }
}
package com.sofi.swagger.codegen;

import io.swagger.codegen.*;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class Java2Generator extends AbstractJavaCodegen implements BeanValidationFeatures {

	@SuppressWarnings("hiding")
    private static final Logger LOGGER = LoggerFactory.getLogger(Java2Generator.class);

    public static final String CLIENT_NAME = "clientName";
    public static final String CONSUL_NAME = "consulName";

    protected boolean useBeanValidation = false;

    public Java2Generator() {
        super();
        outputFolder = "generated-code" + File.separator + "java";
        embeddedTemplateDir = templateDir = "java2";
        invokerPackage = "io.swagger.client";
        artifactId = "swagger-java-client";
        apiPackage = "io.swagger.client.api";
        modelPackage = "io.swagger.client.model";

        cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));
        cliOptions.add(new CliOption(CLIENT_NAME, "sets the name of the ApiClient file."));
        cliOptions.add(new CliOption(CONSUL_NAME, "sets the name of the consul lookup."));

        supportedLibraries.put("feign", "HTTP client: Netflix Feign 8.16.0. JSON processing: Jackson 2.8.x");

        CliOption libraryOption = new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");
        libraryOption.setEnum(supportedLibraries);
        // set feign as the default
        libraryOption.setDefault("feign");
        cliOptions.add(libraryOption);
        setLibrary("feign");
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "java2";
    }

    @Override
    public String getHelp() {
        return "Generates a Java client library.";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            boolean useBeanValidationProp = Boolean.valueOf(additionalProperties.get(USE_BEANVALIDATION).toString());
            this.setUseBeanValidation(useBeanValidationProp);

            // write back as boolean
            additionalProperties.put(USE_BEANVALIDATION, useBeanValidationProp);
        }

        final String invokerFolder = (sourceFolder + '/' + invokerPackage).replace(".", "/");

        if (additionalProperties.containsKey(CLIENT_NAME)) {
            this.clientName = additionalProperties.get(CLIENT_NAME).toString();
            setReservedWordsLowerCase(Collections.singletonList(this.clientName));
        }

        if (additionalProperties.containsKey(CONSUL_NAME)) {
            this.consulName = additionalProperties.get(CONSUL_NAME).toString();
        }

        //Common files
        writeOptional(outputFolder, new SupportingFile("build.gradle.mustache", "", "build.gradle"));
        writeOptional(outputFolder, new SupportingFile("settings.gradle.mustache", "", "settings.gradle"));
        supportingFiles.add(new SupportingFile("ApiClient.mustache", invokerFolder, this.clientName + ".java"));
        supportingFiles.add(new SupportingFile("StringUtil.mustache", invokerFolder, "StringUtil.java"));
        supportingFiles.add(new SupportingFile("FormAwareEncoder.mustache", invokerFolder, "FormAwareEncoder.java"));

        if ("feign".equals(getLibrary())) {
            modelDocTemplateFiles.remove("model_doc.mustache");
            apiDocTemplateFiles.remove("api_doc.mustache");
            additionalProperties.put("jackson", "true");
        } else {
            LOGGER.error("Unknown library option (-l/--library): " + getLibrary());
        }
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        super.postProcessOperations(objs);
        return objs;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        if(!BooleanUtils.toBoolean(model.isEnum)) {
            //final String lib = getLibrary();
            //Needed imports for Jackson based libraries
            if(additionalProperties.containsKey("jackson")) {
                model.imports.add("JsonProperty");
            }
        } else { // enum class
            //Needed imports for Jackson's JsonCreator
            if(additionalProperties.containsKey("jackson")) {
                model.imports.add("JsonCreator");
            }
        }
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);
        return objs;
    }

    public void setUseBeanValidation(boolean useBeanValidation) {
        this.useBeanValidation = useBeanValidation;
    }
}

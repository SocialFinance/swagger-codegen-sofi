package com.sofi.swagger.codegen;

public interface BeanValidationFeatures {

    // Language supports generating BeanValidation-Annotations
    public static final String USE_BEANVALIDATION = "useBeanValidation";
        
    public void setUseBeanValidation(boolean useBeanValidation);
    
}

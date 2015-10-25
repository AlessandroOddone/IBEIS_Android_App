package edu.uic.ibeis_tourist.ibeis;

import edu.uic.ibeis_java_api.api.IbeisAnnotation;

public class AnnotationDbElement {

    private IbeisAnnotation annotation;
    private double isGiraffeThreshold;
    private double recognitionThreshold;

    public AnnotationDbElement(IbeisAnnotation annotation, double isGiraffeThreshold, double recognitionThreshold) {
        this.annotation = annotation;
        this.isGiraffeThreshold = isGiraffeThreshold;
        this.recognitionThreshold = recognitionThreshold;
    }

    public IbeisAnnotation getAnnotation() {
        return annotation;
    }

    public double getRecognitionThreshold() {
        return recognitionThreshold;
    }

    public double getIsGiraffeThreshold() {
        return isGiraffeThreshold;
    }
}

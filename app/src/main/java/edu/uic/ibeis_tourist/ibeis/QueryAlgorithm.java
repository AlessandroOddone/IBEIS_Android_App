package edu.uic.ibeis_tourist.ibeis;

import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.uic.ibeis_java_api.api.Ibeis;
import edu.uic.ibeis_java_api.api.IbeisAnnotation;
import edu.uic.ibeis_java_api.api.IbeisIndividual;
import edu.uic.ibeis_java_api.api.IbeisQueryResult;
import edu.uic.ibeis_java_api.api.IbeisQueryScore;
import edu.uic.ibeis_java_api.exceptions.BadHttpRequestException;
import edu.uic.ibeis_java_api.exceptions.UnsuccessfulHttpRequestException;
import edu.uic.ibeis_tourist.model.SpeciesEnum;

public class QueryAlgorithm {

    public class AnnotationDbElement {
        private IbeisAnnotation annotation;
        private IbeisIndividual individual;
        private double isGiraffeThreshold;
        private double recognitionThreshold;

        public AnnotationDbElement(IbeisAnnotation annotation, IbeisIndividual individual, double isGiraffeThreshold,
                                   double recognitionThreshold) {
            this.annotation = annotation;
            this.individual = individual;
            this.isGiraffeThreshold = isGiraffeThreshold;
            this.recognitionThreshold = recognitionThreshold;
        }

        public IbeisAnnotation getAnnotation() {
            return annotation;
        }

        public IbeisIndividual getIndividual() {
            return individual;
        }

        public double getIsGiraffeThreshold() {
            return isGiraffeThreshold;
        }

        public double getRecognitionThreshold() {
            return recognitionThreshold;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof AnnotationDbElement) {
                if(annotation.getId() == ((AnnotationDbElement) obj).getAnnotation().getId() &&
                        individual.getId() == ((AnnotationDbElement) obj).getIndividual().getId() &&
                        isGiraffeThreshold == ((AnnotationDbElement) obj).isGiraffeThreshold &&
                        recognitionThreshold == ((AnnotationDbElement) obj).recognitionThreshold) {
                    return true;
                }
            }
            return false;
        }
    }

    public class AnnotationDbElementsMap {

        private HashMap<Long,AnnotationDbElement> annotationDbElementHashMap = new HashMap<>();

        public HashMap<Long,AnnotationDbElement> getElements() {
            return annotationDbElementHashMap;
        }
    }

    private Ibeis ibeis = new Ibeis();
    private HashMap<Long, AnnotationDbElement> annotationDbElementsHashMap;

    public QueryAlgorithm(InputStream annotationDbElementsJsonFileInputStream) {
        readAnnotationDbElementsMapFromJsonFile(annotationDbElementsJsonFileInputStream);
    }

    public QueryAlgorithmResult query(IbeisAnnotation queryAnnotation) throws IOException, BadHttpRequestException, UnsuccessfulHttpRequestException {
        IbeisQueryResult queryResult = ibeis.query(queryAnnotation, getDbAnnotations());
        List<IbeisQueryScore> queryScores = queryResult.getScores();
        System.out.println("QUERY RESULT: " + queryResult);

        boolean isGiraffe = false;
        for (IbeisQueryScore queryScore : queryScores) {
            double score = queryScore.getScore();
            AnnotationDbElement annotationDbElement = annotationDbElementsHashMap.get(queryScore.getDbAnnotation().getId());
            if (score >= annotationDbElement.getRecognitionThreshold()) {
                return new QueryAlgorithmResult(queryScore.getDbAnnotation().getIndividual(),
                        SpeciesEnum.GIRAFFE);
            }
            if (!isGiraffe) {
                if (score >= annotationDbElement.getIsGiraffeThreshold()) {
                    isGiraffe = true;
                }
            }
        }
        if (isGiraffe) {
            return new QueryAlgorithmResult(SpeciesEnum.GIRAFFE);
        }
        return new QueryAlgorithmResult(SpeciesEnum.UNKNOWN);
    }

    private void readAnnotationDbElementsMapFromJsonFile(InputStream annotationDbElementsJsonFileInputStream) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(annotationDbElementsJsonFileInputStream));
            annotationDbElementsHashMap = new GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().create().
                    fromJson(reader.readLine(), AnnotationDbElementsMap.class).getElements();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<IbeisAnnotation> getDbAnnotations() throws IOException, BadHttpRequestException, UnsuccessfulHttpRequestException {
        List<IbeisAnnotation> dbAnnotations = new ArrayList<>();
        for(AnnotationDbElement e : annotationDbElementsHashMap.values()) {
            dbAnnotations.add(e.getAnnotation());
        }
        return dbAnnotations;
    }
}

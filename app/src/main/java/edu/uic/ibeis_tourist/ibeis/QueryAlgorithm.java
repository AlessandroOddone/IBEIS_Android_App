package edu.uic.ibeis_tourist.ibeis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.uic.ibeis_java_api.api.Ibeis;
import edu.uic.ibeis_java_api.api.IbeisAnnotation;
import edu.uic.ibeis_java_api.api.IbeisImage;
import edu.uic.ibeis_java_api.api.IbeisIndividual;
import edu.uic.ibeis_java_api.api.IbeisQueryResult;
import edu.uic.ibeis_java_api.api.IbeisQueryScore;
import edu.uic.ibeis_java_api.exceptions.BadHttpRequestException;
import edu.uic.ibeis_java_api.exceptions.InvalidEncounterIdException;
import edu.uic.ibeis_java_api.exceptions.UnsuccessfulHttpRequestException;
import edu.uic.ibeis_tourist.model.SpeciesEnum;

public class QueryAlgorithm {

    private static final int BROOKFIELD_ZOO_GIRAFFES_ENCOUNTER_ID = 1;

    private static final QueryAlgorithmType DEFAULT_ALGORITHM = QueryAlgorithmType.BEST_SCORE;
    private static final double IS_A_GIRAFFE_THRESHOLD = 0.1;
    private static final double RECOGNITION_THRESHOLD = 12;

    private Ibeis ibeis = new Ibeis();

    private IbeisAnnotation queryAnnotation;
    private QueryAlgorithmType selectedAlgorithm = DEFAULT_ALGORITHM;

    public QueryAlgorithm(IbeisAnnotation queryAnnotation) {
        this.queryAnnotation = queryAnnotation;
    }

    public QueryAlgorithm(IbeisAnnotation queryAnnotation, QueryAlgorithmType selectedAlgorithm) {
        this.queryAnnotation = queryAnnotation;
        this.selectedAlgorithm = selectedAlgorithm;
    }

    public QueryAlgorithmResult execute() throws IOException, BadHttpRequestException, UnsuccessfulHttpRequestException, InvalidEncounterIdException {
        switch (selectedAlgorithm) {
            case BEST_SCORE:
                return executeBestScore();
            case THRESHOLDS:
                return executeThresholds();
        }
        return null;
    }

    private QueryAlgorithmResult executeBestScore() throws IOException, BadHttpRequestException, UnsuccessfulHttpRequestException, InvalidEncounterIdException {
        IbeisQueryResult queryResult = ibeis.query(queryAnnotation, getAllDbAnnotations());
        List<IbeisQueryScore> queryScores = queryResult.getScores();
        //System.out.println("QUERY RESULT: " + queryResult);

        //sort query scores from the highest to the lowest
        Collections.sort(queryScores, Collections.reverseOrder());
        //get the highest score
        IbeisQueryScore highestScore = queryScores.get(0);
        //System.out.println("HIGHEST SCORE: " + highestScore);

        return highestScore.getScore() >= IS_A_GIRAFFE_THRESHOLD ? new QueryAlgorithmResult(highestScore.getDbAnnotation().getIndividual(), SpeciesEnum.GIRAFFE) : null;
    }

    private QueryAlgorithmResult executeThresholds() throws IOException, BadHttpRequestException, UnsuccessfulHttpRequestException, InvalidEncounterIdException {
        List<List<IbeisAnnotation>> dbAnnotationsByIndividual = getAnnotationsByIndividual();

        int numIndividuals = dbAnnotationsByIndividual.size();
        int[] curAnnotIndex = new int[numIndividuals]; //for each individual, the index of the next annotation to be computed
        boolean[] examined = new boolean[numIndividuals]; //for each individual, true if the individual has already been examined, false otherwise

        //INIT
        for (int i=0; i<numIndividuals; i++) {
            curAnnotIndex[i] = 0;
            examined[i] = false;
        }

        //LOOP
        int i = 0; //individual index
        boolean isGiraffe = false;
        boolean allExamined = false;
        do {
            IbeisQueryResult queryResult = ibeis.query(queryAnnotation, Arrays.asList(dbAnnotationsByIndividual.get(i).get(curAnnotIndex[i])));
            IbeisIndividual dbIndividual = queryResult.getScores().get(0).getDbAnnotation().getIndividual();
            double score = queryResult.getScores().get(0).getScore();

            if (score >= IS_A_GIRAFFE_THRESHOLD) {
                isGiraffe = true;
            }
            if (score >= RECOGNITION_THRESHOLD) {
                return new QueryAlgorithmResult(dbIndividual, SpeciesEnum.GIRAFFE);
            }
            else {
                if (curAnnotIndex[i]++ >= dbAnnotationsByIndividual.get(i).size()) {//individual examined
                    examined[i] = true;
                    for (int j=0; j<examined.length; j++) {
                        if (examined[j] != true) {
                            break;
                        }
                        if (j == examined.length-1) {
                            allExamined = true;
                        }
                    }
                }
                if (!allExamined) {
                    //switch individual
                    do {
                        if (i < numIndividuals-1) {
                                i++;
                        }
                        else {
                            i = 0;
                        }
                    } while (examined[i] == true);
                }
            }
        } while (!allExamined);

        if (isGiraffe) {
            return new QueryAlgorithmResult(SpeciesEnum.GIRAFFE);
        } else {
            return new QueryAlgorithmResult(SpeciesEnum.UNKNOWN);
        }
    }

    private List<IbeisAnnotation> getAllDbAnnotations() throws IOException, BadHttpRequestException, UnsuccessfulHttpRequestException, InvalidEncounterIdException {
        List<IbeisAnnotation> dbAnnotations = new ArrayList<>();
        for(IbeisImage i : ibeis.getEncounterById(BROOKFIELD_ZOO_GIRAFFES_ENCOUNTER_ID).getImages()) {
            dbAnnotations.addAll(i.getAnnotations());
        }
        return dbAnnotations;
    }

    private List<List<IbeisAnnotation>> getAnnotationsByIndividual() throws IOException, BadHttpRequestException, UnsuccessfulHttpRequestException, InvalidEncounterIdException {
        List<List<IbeisAnnotation>> dbAnnotationsByIndividual = new ArrayList<>();
        for (IbeisIndividual i : ibeis.getEncounterById(BROOKFIELD_ZOO_GIRAFFES_ENCOUNTER_ID).getIndividuals()) {
            dbAnnotationsByIndividual.add(i.getAnnotations());
        }
        return dbAnnotationsByIndividual;
    }
}

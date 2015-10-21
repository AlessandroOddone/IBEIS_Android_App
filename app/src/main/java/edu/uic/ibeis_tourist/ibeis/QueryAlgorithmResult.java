package edu.uic.ibeis_tourist.ibeis;

import edu.uic.ibeis_java_api.api.IbeisIndividual;
import edu.uic.ibeis_tourist.model.SpeciesEnum;

public class QueryAlgorithmResult {

    private IbeisIndividual individual;
    private SpeciesEnum species;

    public QueryAlgorithmResult(SpeciesEnum species) {
        this.individual = null;
        this.species = species;
    }

    public QueryAlgorithmResult(IbeisIndividual individual, SpeciesEnum species) {
        this.individual = individual;
        this.species = species;
    }

    public IbeisIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(IbeisIndividual individual) {
        this.individual = individual;
    }

    public SpeciesEnum getSpecies() {
        return species;
    }

    public void setSpecies(SpeciesEnum species) {
        this.species = species;
    }
}

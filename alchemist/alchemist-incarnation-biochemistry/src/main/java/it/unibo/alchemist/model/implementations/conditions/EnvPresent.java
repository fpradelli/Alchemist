package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * 
 *
 */
public class EnvPresent extends AbstractCondition<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = -389543469391097488L;
    private final Environment<Double> environment;

    /**
     * 
     * @param node 
     * @param env 
     */
    public EnvPresent(final Node<Double> node, final Environment<Double> env) {
        super(node);
        environment = env;
    }

    @Override
    public Condition<Double> cloneOnNewNode(final Node<Double> n) {
        return new EnvPresent(n, environment);
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityConditioning() {
        return environment.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode)
                .findAny()
                .isPresent() ? 1d : 0d;
    }

    @Override
    public boolean isValid() {
        return environment.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode)
                .findAny()
                .isPresent();
    }

}
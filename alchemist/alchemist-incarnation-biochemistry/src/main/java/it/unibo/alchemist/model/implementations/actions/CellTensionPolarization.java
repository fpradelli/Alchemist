package it.unibo.alchemist.model.implementations.actions;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 *
 */
public class CellTensionPolarization extends AbstractAction<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 4007473626119435396L;
    private final EnvironmentSupportingDeformableCells env;

    /**
     * 
     * @param node 
     * @param env 
     */
    public CellTensionPolarization(final Node<Double> node, final Environment<Double> env) {
        super(node);
        if (!(node instanceof CircularDeformableCell)) {
            throw new IllegalArgumentException("This Condition can only be setted in a CircularDeformableCell");
        } 
        if (env instanceof EnvironmentSupportingDeformableCells) {
            this.env = (EnvironmentSupportingDeformableCells) env;
        } else {
            throw new IllegalArgumentException("This Condition can only be supported in an EnironmentSupportingDeformableCells");
        }
    }

    @Override
    public CellTensionPolarization cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new CellTensionPolarization(n, env);
    }

    @Override
    public void execute() {
        double[] resVersor = new double[2];
        final double[] nodePos = env.getPosition(getNode()).getCartesianCoordinates();
        final List<Position> pushForces = env.getNodesWithinRange(getNode(), env.getMaxDiameterAmongDeformableCells()).stream()
                .parallel()
                .filter(n -> {
                    if (n instanceof CellWithCircularArea) {
                        double maxDist;
                        if (n instanceof CircularDeformableCell) {
                             maxDist = (((CircularDeformableCell) getNode()).getMaxRadius() + ((CircularDeformableCell) n).getMaxRadius());
                        } else {
                             maxDist = (((CircularDeformableCell) getNode()).getMaxRadius() + ((CellWithCircularArea) n).getRadius());
                        }
                        return env.getDistanceBetweenNodes(getNode(), n) < maxDist;
                    } else {
                        return false;
                    }
                })
                .map(n -> {
                    final double[] nPos =  env.getPosition(n).getCartesianCoordinates();
                    final double maxRn;
                    final double minRn;
                    final double maxRN = ((CircularDeformableCell) getNode()).getMaxRadius();
                    final double minRN = ((CircularDeformableCell) getNode()).getRadius();
                    final double intensity;
                    if (n instanceof CircularDeformableCell) {
                        maxRn = ((CircularDeformableCell) n).getMaxRadius();
                        minRn = ((CircularDeformableCell) n).getRadius();
                    } else {
                        maxRn = ((CellWithCircularArea) n).getRadius();
                        minRn = maxRn;
                    }
                    if (maxRn == minRn && maxRN == minRN) {
                        intensity = 1;
                    } else {
                        intensity = ((maxRn + maxRN) - env.getDistanceBetweenNodes(n, getNode())) / ((maxRn + maxRN) - (minRn + minRN));
                    }
                    if (intensity != 0) {
                        double[] propensityVect = new double[]{nodePos[0] - nPos[0], nodePos[1] - nPos[1]};
                        final double module = FastMath.sqrt(FastMath.pow(propensityVect[0], 2) + FastMath.pow(propensityVect[1], 2));
                        if (module == 0) {
                            return new Continuous2DEuclidean(0, 0);
                        }
                        propensityVect = new double[]{intensity * (propensityVect[0] / module), intensity * (propensityVect[1] / module)};
                        return new Continuous2DEuclidean(propensityVect[0], propensityVect[1]);
                    } else {
                        return new Continuous2DEuclidean(0, 0);
                    } 
                })
                .collect(Collectors.toList());
        if (pushForces.isEmpty()) {
            ((CellNode) getNode()).addPolarization(new Continuous2DEuclidean(0, 0));
        } else {
            for (final Position p : pushForces) {
                resVersor[0] = resVersor[0] + p.getCoordinate(0);
                resVersor[1] = resVersor[1] + p.getCoordinate(1);
            }
            final double module = FastMath.sqrt(FastMath.pow(resVersor[0], 2) + FastMath.pow(resVersor[1], 2));
            if (module == 0) {
                ((CellNode) getNode()).addPolarization(new Continuous2DEuclidean(0, 0));
            } else {
                ((CellNode) getNode()).addPolarization(new Continuous2DEuclidean(resVersor[0] / module, resVersor[1] / module));
            }
        }
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

}
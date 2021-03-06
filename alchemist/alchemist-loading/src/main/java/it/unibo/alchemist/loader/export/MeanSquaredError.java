package it.unibo.alchemist.loader.export;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;

import com.google.common.collect.Lists;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Exports the Mean Squared Error for the concentration of some molecule, given
 * another molecule that carries the expected result. The expected value is
 * extracted from every node, then the provided {@link UnivariateStatistic} is
 * applied to get the actual reference value. Then, the actual value is
 * extracted from every node, its value is compared to the reference, it gets
 * squared, and then logged.
 * 
 * @param <T>
 */
public class MeanSquaredError<T> implements Extractor {

    private final Incarnation<T> incarnation;
    private final String pReference;
    private final Molecule mReference;
    private final String pActual;
    private final Molecule mActual;
    private final List<String> name;
    private final UnivariateStatistic statistic;

    /**
     * @param molRef
     *            expected value {@link Molecule}
     * @param propRef
     *            expected value property name
     * @param stat
     *            the {@link UnivariateStatistic} to apply
     * @param molActual
     *            the target {@link Molecule}
     * @param propActual
     *            the target property
     * @param incarnation
     *            the {@link Incarnation} to use
     */
    public MeanSquaredError(final String molRef, final String propRef, final String stat, final String molActual,
            final String propActual, final Incarnation<T> incarnation) {
        final Optional<UnivariateStatistic> statOpt = StatUtil.makeUnivariateStatistic(stat);
        if (!statOpt.isPresent()) {
            throw new IllegalArgumentException("Could not create univariate statistic " + stat);
        }
        statistic = statOpt.get();
        this.incarnation = incarnation;
        this.mReference = incarnation.createMolecule(molRef);
        this.pReference = propRef == null ? "" : propRef;
        this.pActual = propActual == null ? "" : propActual;
        this.mActual = incarnation.createMolecule(molActual);
        final StringBuilder mse = new StringBuilder("MSE(");
        mse.append(stat);
        mse.append('(');
        if (!pReference.isEmpty()) {
            mse.append(pReference);
            mse.append('@');
        }
        mse.append(molRef);
        mse.append("),");
        if (!pActual.isEmpty()) {
            mse.append(pActual);
            mse.append('@');
        }
        mse.append(molActual);
        mse.append(')');
        name = Collections.unmodifiableList(Lists.newArrayList(mse.toString()));
    }

    @Override
    public double[] extractData(final Environment<?> env, final Reaction<?> r, final Time time, final long step) {
        @SuppressWarnings("unchecked")
        final Environment<T> environment = (Environment<T>) env;
        final double value = statistic.evaluate(
                environment.getNodes().parallelStream()
                    .mapToDouble(n -> incarnation.getProperty(n, mReference, pReference))
                    .toArray());
        final double mse = environment.getNodes().parallelStream()
                .mapToDouble(n -> incarnation.getProperty(n, mActual, pActual) - value)
                .map(v -> v * v)
                .average()
                .orElseGet(() -> Double.NaN);
        return new double[]{mse};
    }

    @Override
    public List<String> getNames() {
        return name;
    }

}

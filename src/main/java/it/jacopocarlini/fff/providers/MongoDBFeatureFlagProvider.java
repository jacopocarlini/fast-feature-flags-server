package it.jacopocarlini.fff.providers;

import dev.openfeature.sdk.*;
import it.jacopocarlini.fff.entity.AssignedTarget;
import it.jacopocarlini.fff.entity.Flag;
import it.jacopocarlini.fff.entity.Target;
import it.jacopocarlini.fff.repository.AssignedTargetRepository;
import it.jacopocarlini.fff.repository.FlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Service
public class MongoDBFeatureFlagProvider extends EventProvider {

    private static final Random RANDOM = new Random();
    private static final String DEFAULT_VALUE = "default_value";
    private static final String FLAG_NOT_FOUND = "flag not found";
    private static final String FLAG_DISABLED = "flag is disabled";
    private static final String OUTSIDE_TIME_WINDOW = "outside time window";
    private static final String TARGET_MATCHED = "targeting key matched";
    private static final String ALREADY_ASSIGNED = "already assigned";
    private static final String ROLLOUT = "rollout";

    @Autowired
    private FlagRepository flagRepository;

    @Autowired
    private AssignedTargetRepository assignedTargetRepository;

    @Override
    public Metadata getMetadata() {
        return () -> "MongoDBFeatureFlagProvider";
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String flagKey, String defaultValue, EvaluationContext ctx) {
        return evaluation(flagKey, defaultValue, ctx, String.class);
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String flagKey, Boolean defaultValue, EvaluationContext ctx) {
        return evaluation(flagKey, defaultValue, ctx, Boolean.class);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String flagKey, Integer defaultValue, EvaluationContext ctx) {
        return evaluation(flagKey, defaultValue, ctx, Integer.class);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String flagKey, Double defaultValue, EvaluationContext ctx) {
        return evaluation(flagKey, defaultValue, ctx, Double.class);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String flagKey, Value defaultValue, EvaluationContext ctx) {
        return evaluation(flagKey, defaultValue, ctx, Value.class);
    }

    private <T> ProviderEvaluation<T> evaluation(String flagKey, T defaultValue, EvaluationContext ctx, Class<T> valueType) {
        try {
            Optional<Flag> optionalFlag = flagRepository.findFirstByFlagKey(flagKey);

            if (optionalFlag.isEmpty()) {
                return createDefaultEvaluation(defaultValue, FLAG_NOT_FOUND);
            }

            Flag flag = optionalFlag.get();

            // check if the flag is enabled
            if (!Boolean.TRUE.equals(flag.getEnabled())) {
                return createDefaultEvaluation(defaultValue, FLAG_DISABLED);
            }

            // check time window
            if (isOutsideTimeWindow(flag)) {
                return createDefaultEvaluation(defaultValue, OUTSIDE_TIME_WINDOW);
            }

            // check the target
            Optional<ProviderEvaluation<T>> targetMatch = checkTargetMatch(flag, ctx, valueType);
            if (targetMatch.isPresent()) {
                return targetMatch.get();
            }

            // handle rollout
            if (flag.getRolloutPercentage() != null) {
                return handleRollout(flag, ctx, valueType);
            }

            // base case: return default variant
            return ProviderEvaluation.<T>builder()
                    .value(convertValue(flag.getVariants().get(flag.getDefaultVariant()), valueType))
                    .variant(flag.getDefaultVariant())
                    .reason("default variant")
                    .build();

        } catch (ClassCastException e) {
            throw new FeatureFlagEvaluationException("Value type mismatch for flag: " + flagKey, e);
        } catch (Exception e) {
            throw new FeatureFlagEvaluationException("Error evaluating flag: " + flagKey, e);
        }
    }

    private <T> ProviderEvaluation<T> createDefaultEvaluation(T defaultValue, String reason) {
        return ProviderEvaluation.<T>builder()
                .value(defaultValue)
                .reason(reason)
                .variant(DEFAULT_VALUE)
                .build();
    }

    private boolean isOutsideTimeWindow(Flag flag) {
        if (flag.getTimeWindowStart() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(flag.getTimeWindowStart()) || now.isAfter(flag.getTimeWindowEnd());
    }

    private <T> Optional<ProviderEvaluation<T>> checkTargetMatch(Flag flag, EvaluationContext ctx, Class<T> valueType) {
        if (flag.getTarget() == null) {
            return Optional.empty();
        }

        for (Target target : flag.getTarget()) {
            Pattern pattern = Pattern.compile(target.getFilter());
            if (pattern.matcher(ctx.getTargetingKey()).find()) {
                Object value = flag.getVariants().get(target.getVariant());

                return Optional.of(ProviderEvaluation.<T>builder()
                        .value(convertValue(value, valueType))
                        .reason(TARGET_MATCHED)
                        .variant(target.getVariant())
                        .build());
            }
        }

        return Optional.empty();
    }

    private <T> ProviderEvaluation<T> handleRollout(Flag flag, EvaluationContext ctx, Class<T> valueType) {
        String targetKey = ctx.getTargetingKey();

        // check if the user is already assigned to a variant
        Optional<AssignedTarget> optionalAssignedTarget =
                assignedTargetRepository.findFirstByFlagKeyAndTargetKey(flag.getFlagKey(), targetKey);

        if (optionalAssignedTarget.isPresent()) {
            AssignedTarget assignedTarget = optionalAssignedTarget.get();
            return ProviderEvaluation.<T>builder()
                    .value(convertValue(flag.getVariants().get(assignedTarget.getVariant()), valueType))
                    .variant(assignedTarget.getVariant())
                    .reason(ALREADY_ASSIGNED)
                    .build();
        }

        // assign the user to the variant
        String variant = determineVariantForRollout(flag);
        assignedTargetRepository.save(new AssignedTarget(flag.getFlagKey(), targetKey, variant));

        return ProviderEvaluation.<T>builder()
                .value(convertValue(flag.getVariants().get(variant), valueType))
                .reason(ROLLOUT)
                .variant(variant)
                .build();
    }

    private String determineVariantForRollout(Flag flag) {
        int randomValue = RANDOM.nextInt(100);
        String variant = flag.getDefaultVariant();
        int cumulativePercentage = 0;

        for (Map.Entry<String, Integer> entry : flag.getRolloutPercentage().entrySet()) {
            cumulativePercentage += entry.getValue();
            if (randomValue < cumulativePercentage) {
                variant = entry.getKey();
                break;
            }
        }

        return variant;
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return (T) value;
        }

        throw new ClassCastException("Cannot convert value of type " +
                value.getClass().getName() + " to " + targetType.getName());
    }

    // Eccezione personalizzata per gli errori di valutazione dei feature flag
    public static class FeatureFlagEvaluationException extends RuntimeException {
        public FeatureFlagEvaluationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
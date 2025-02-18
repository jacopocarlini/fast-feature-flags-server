package it.jacopocarlini.fff.service;

import com.github.jacopocarlini.fffp.entity.Flag;
import com.github.jacopocarlini.fffp.entity.Target;
import com.github.jacopocarlini.fffp.providers.MongoDBFeatureFlagProviderExtended;
import dev.openfeature.sdk.MutableContext;
import it.jacopocarlini.fff.models.FlagDetails;
import it.jacopocarlini.fff.models.Variant;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlagService {

    private final MongoDBFeatureFlagProviderExtended mongoDBFeatureFlagProvider;

    public FlagService(@Value("${connection_string}") String connectionString) {
        this.mongoDBFeatureFlagProvider = new MongoDBFeatureFlagProviderExtended(connectionString);
    }

    private static Flag mapDetailsToEntity(FlagDetails flagDetails) {
        return Flag.builder()
                .flagKey(flagDetails.getFlagKey())
                .enabled(flagDetails.getEnabled())
                .defaultVariant(flagDetails.getVariants().stream()
                        .filter(Variant::getDefaultVariant)
                        .findFirst()
                        .map(Variant::getName)
                        .orElse(null)
                )
                .variants(flagDetails.getVariants().stream()
                        .map(elem -> Map.entry(elem.getName(), elem.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                )
                .rolloutPercentage(flagDetails.getVariants().stream()
                        .filter(elem -> elem.getPercentage() != null)
                        .map(elem -> Map.entry(elem.getName(), elem.getPercentage()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .target(flagDetails.getVariants().stream()
                        .filter(elem -> elem.getTarget() != null)
                        .map(elem -> Target.builder()
                                .variant(elem.getName())
                                .filter(elem.getTarget())
                                .build())
                        .toList())
                .timeWindowStart(flagDetails.getTimeWindowStart())
                .timeWindowEnd(flagDetails.getTimeWindowEnd())
                .build();
    }

    public List<FlagDetails> getFlags() {
        return mongoDBFeatureFlagProvider.getFlags().stream()
                .map(this::mapEntityToDetails)
                .toList();
    }

    @SneakyThrows
    public FlagDetails getFlag(String flagKey) {
        Flag flag = mongoDBFeatureFlagProvider.getFlag(flagKey);
        return mapEntityToDetails(flag);
    }

    @SneakyThrows
    public void crateFlag(FlagDetails flagDetails) {
        Flag flag = mapDetailsToEntity(flagDetails);
        mongoDBFeatureFlagProvider.crateFlag(flag);
    }

    @SneakyThrows
    public void deleteFlag(String flagKey) {
        mongoDBFeatureFlagProvider.deleteFlag(flagKey);
    }

    @SneakyThrows
    public void updateFlag(String flagKey, FlagDetails newFlagDetails) {
        var newFlag = mapDetailsToEntity(newFlagDetails);
        mongoDBFeatureFlagProvider.updateFlag(flagKey, newFlag);
    }

    private FlagDetails mapEntityToDetails(Flag flag) {
        return FlagDetails.builder()
                .flagKey(flag.getFlagKey())
                .enabled(flag.getEnabled())
                .timeWindowStart(flag.getTimeWindowStart())
                .timeWindowEnd(flag.getTimeWindowEnd())
                .variants(buildVariants(flag))
                .build();
    }

    private List<Variant> buildVariants(Flag flag) {
        List<Variant> variantList = new ArrayList<>();
        flag.getVariants().forEach((key, value) -> variantList.add(Variant.builder()
                .value(value)
                .name(key)
                .defaultVariant(flag.getDefaultVariant().equals(key))
                .percentage(Optional.ofNullable(flag.getRolloutPercentage()).map(elem -> elem.get(key)).orElse(null))
                .target(Optional.ofNullable(flag.getTarget())
                        .flatMap(targets -> targets.stream()
                                .filter(elem -> elem.getVariant().equals(key))
                                .findFirst()
                                .map(Target::getFilter))
                        .orElse(null)
                )
                .build()));
        return variantList;
    }


    public String getStringEvaluation(String key, String defaultValue, MutableContext mutableContext) {
        return mongoDBFeatureFlagProvider.getStringEvaluation(key, defaultValue, mutableContext).getValue();
    }
}
